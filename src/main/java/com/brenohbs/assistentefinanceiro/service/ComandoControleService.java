package com.brenohbs.assistentefinanceiro.service;

import com.brenohbs.assistentefinanceiro.util.ValorMonetario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Comandos que usam a aba "Controle":
 *   /saldo <valor>  -> grava o saldo da conta e responde o resumo
 *   /fatura <valor> -> grava a fatura do cartão e responde o resumo
 *   /posso          -> só responde o resumo
 *
 * Só atende os chat ids listados em telegram.allowed-chat-ids
 * (variável de ambiente TELEGRAM_ALLOWED_CHAT_IDS). Lista vazia = ninguém.
 */
@Service
public class ComandoControleService {

    private static final Set<String> COMANDOS = Set.of("/saldo", "/fatura", "/posso");

    private final ControleService controleService;
    private final Set<Long> chatIdsPermitidos;

    public ComandoControleService(ControleService controleService,
                                  @Value("${telegram.allowed-chat-ids:}") String chatIdsPermitidos) {
        this.controleService = controleService;
        this.chatIdsPermitidos = Arrays.stream(chatIdsPermitidos.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toUnmodifiableSet());
    }

    /** Diz se o texto é um dos comandos tratados aqui (ex: "/saldo 100", "/posso@MeuBot"). */
    public boolean ehComando(String texto) {
        return COMANDOS.contains(comandoDe(texto));
    }

    public boolean chatPermitido(long chatId) {
        return chatIdsPermitidos.contains(chatId);
    }

    public String processar(String texto) {
        String comando = comandoDe(texto);
        String argumento = argumentoDe(texto);

        try {
            switch (comando) {
                case "/saldo" -> {
                    Optional<BigDecimal> valor = ValorMonetario.parse(argumento);
                    if (valor.isEmpty()) {
                        return usoComValor("/saldo", "saldo da conta");
                    }
                    controleService.gravarSaldo(valor.get());
                    return "✅ Saldo atualizado: " + ValorMonetario.formatar(valor.get()) + "\n\n" + resumo();
                }
                case "/fatura" -> {
                    Optional<BigDecimal> valor = ValorMonetario.parse(argumento);
                    if (valor.isEmpty()) {
                        return usoComValor("/fatura", "fatura do cartão");
                    }
                    controleService.gravarFatura(valor.get());
                    return "✅ Fatura atualizada: " + ValorMonetario.formatar(valor.get()) + "\n\n" + resumo();
                }
                default -> {
                    return resumo();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Ops, deu erro ao acessar a planilha. Tenta de novo daqui a pouco.";
        }
    }

    private String resumo() throws Exception {
        ControleService.Resumo r = controleService.lerResumo();
        return """
                💰 Dinheiro livre: %s
                🎉 Disponível no Lazer: %s
                🛒 Posso gastar agora: %s"""
                .formatted(
                        ValorMonetario.formatar(r.dinheiroLivre()),
                        ValorMonetario.formatar(r.disponivelLazer()),
                        ValorMonetario.formatar(r.possoGastar()));
    }

    private static String usoComValor(String comando, String descricao) {
        return """
                Valor inválido. Uso: %s <valor> (%s)
                Exemplos: %s 1156,26 · %s 1.156,26 · %s 1156.26"""
                .formatted(comando, descricao, comando, comando, comando);
    }

    private static String comandoDe(String texto) {
        String primeiro = texto.trim().split("\\s+", 2)[0].toLowerCase();
        int arroba = primeiro.indexOf('@'); // "/saldo@NomeDoBot" em grupos
        return arroba >= 0 ? primeiro.substring(0, arroba) : primeiro;
    }

    private static String argumentoDe(String texto) {
        String[] partes = texto.trim().split("\\s+", 2);
        return partes.length > 1 ? partes[1] : "";
    }
}

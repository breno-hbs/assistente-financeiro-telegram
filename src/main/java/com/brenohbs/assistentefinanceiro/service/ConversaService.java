package com.brenohbs.assistentefinanceiro.service;

import com.brenohbs.assistentefinanceiro.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controla o fluxo da conversa por número de telefone:
 * mensagem qualquer -> escolher categoria -> escolher item -> informar valor -> confirmação.
 */
@Service
public class ConversaService {

    private final Map<String, SessaoUsuario> sessoes = new ConcurrentHashMap<>();
    private final GoogleSheetsService sheetsService;

    public ConversaService(GoogleSheetsService sheetsService) {
        this.sheetsService = sheetsService;
    }

    public String processarMensagem(String telefone, String textoRecebido) {
        SessaoUsuario sessao = sessoes.computeIfAbsent(telefone, t -> new SessaoUsuario());
        String texto = textoRecebido == null ? "" : textoRecebido.trim();

        // Comando de reinício, disponível em qualquer etapa
        if (texto.equalsIgnoreCase("cancelar")) {
            sessao.reiniciar();
            return "Ok, cancelado. Manda \"gasto\" quando quiser lançar outro.";
        }

        return switch (sessao.getEstado()) {
            case AGUARDANDO_INICIO -> iniciarFluxo(sessao);
            case AGUARDANDO_CATEGORIA -> tratarEscolhaCategoria(sessao, texto);
            case AGUARDANDO_ITEM -> tratarEscolhaItem(sessao, texto);
            case AGUARDANDO_VALOR -> tratarValor(sessao, texto);
        };
    }

    private String iniciarFluxo(SessaoUsuario sessao) {
        sessao.setEstado(EstadoConversa.AGUARDANDO_CATEGORIA);
        return "Qual categoria?\n" + montarMenuCategorias();
    }

    private String montarMenuCategorias() {
        StringBuilder sb = new StringBuilder();
        List<Categoria> categorias = CatalogoGastos.CATEGORIAS;
        for (int i = 0; i < categorias.size(); i++) {
            sb.append("[%d] %s\n".formatted(i + 1, categorias.get(i).nome()));
        }
        sb.append("\n(a qualquer momento, mande \"cancelar\" para recomeçar)");
        return sb.toString();
    }

    private String tratarEscolhaCategoria(SessaoUsuario sessao, String texto) {
        Integer indice = paraIndiceValido(texto, CatalogoGastos.CATEGORIAS.size());
        if (indice == null) {
            return "Não entendi. Manda só o número da categoria:\n" + montarMenuCategorias();
        }

        Categoria categoria = CatalogoGastos.CATEGORIAS.get(indice);
        sessao.setCategoriaEscolhida(categoria);
        sessao.setEstado(EstadoConversa.AGUARDANDO_ITEM);
        return "Qual item?\n" + montarMenuItens(categoria);
    }

    private String montarMenuItens(Categoria categoria) {
        StringBuilder sb = new StringBuilder();
        List<ItemGasto> itens = categoria.itens();
        for (int i = 0; i < itens.size(); i++) {
            sb.append("[%d] %s\n".formatted(i + 1, itens.get(i).nome()));
        }
        return sb.toString();
    }

    private String tratarEscolhaItem(SessaoUsuario sessao, String texto) {
        List<ItemGasto> itens = sessao.getCategoriaEscolhida().itens();
        Integer indice = paraIndiceValido(texto, itens.size());
        if (indice == null) {
            return "Não entendi. Manda só o número do item:\n" + montarMenuItens(sessao.getCategoriaEscolhida());
        }

        ItemGasto item = itens.get(indice);
        sessao.setItemEscolhido(item);
        sessao.setEstado(EstadoConversa.AGUARDANDO_VALOR);
        return "Quanto foi (em reais)?";
    }

    private String tratarValor(SessaoUsuario sessao, String texto) {
        Double valor = paraValorMonetario(texto);
        if (valor == null) {
            return "Não entendi o valor. Manda só o número, tipo: 45.90";
        }

        try {
            double novoTotal = sheetsService.registrarGasto(sessao.getItemEscolhido().linha(), valor);
            String nomeItem = sessao.getItemEscolhido().nome();
            sessao.reiniciar();
            return "✅ Registrado: %s R$ %.2f\nTotal do mês em '%s': R$ %.2f"
                    .formatted(nomeItem, valor, nomeItem, novoTotal);
        } catch (Exception e) {
            sessao.reiniciar();
            return "Ops, deu erro ao gravar na planilha. Tenta de novo mandando \"gasto\".";
        }
    }

    private Integer paraIndiceValido(String texto, int tamanhoLista) {
        try {
            int numero = Integer.parseInt(texto.trim());
            if (numero >= 1 && numero <= tamanhoLista) {
                return numero - 1;
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private Double paraValorMonetario(String texto) {
        try {
            String normalizado = texto.trim().replace(",", ".");
            double valor = Double.parseDouble(normalizado);
            return valor > 0 ? valor : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

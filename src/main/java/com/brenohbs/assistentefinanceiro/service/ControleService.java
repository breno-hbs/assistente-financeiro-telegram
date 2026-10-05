package com.brenohbs.assistentefinanceiro.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lê e escreve na aba "Controle" da planilha (saldo da conta, fatura do
 * cartão e os indicadores calculados pela própria planilha).
 *
 * Endereços das células ficam em application.properties (controle.*).
 */
@Service
public class ControleService {

    private final Sheets sheetsService;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    @Value("${controle.sheet-name}")
    private String sheetName;

    @Value("${controle.celula.saldo}")
    private String celulaSaldo;

    @Value("${controle.celula.fatura}")
    private String celulaFatura;

    @Value("${controle.celula.dinheiro-livre}")
    private String celulaDinheiroLivre;

    @Value("${controle.celula.disponivel-lazer}")
    private String celulaDisponivelLazer;

    @Value("${controle.celula.posso-gastar}")
    private String celulaPossoGastar;

    public ControleService(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    public record Resumo(BigDecimal dinheiroLivre, BigDecimal disponivelLazer, BigDecimal possoGastar) {
    }

    public void gravarSaldo(BigDecimal valor) throws Exception {
        escreverNumero(celulaSaldo, valor);
    }

    public void gravarFatura(BigDecimal valor) throws Exception {
        escreverNumero(celulaFatura, valor);
    }

    public Resumo lerResumo() throws Exception {
        List<ValueRange> ranges = sheetsService.spreadsheets().values()
                .batchGet(spreadsheetId)
                .setRanges(List.of(
                        range(celulaDinheiroLivre),
                        range(celulaDisponivelLazer),
                        range(celulaPossoGastar)))
                // Valor bruto (número), sem a formatação regional da planilha
                .setValueRenderOption("UNFORMATTED_VALUE")
                .execute()
                .getValueRanges();

        return new Resumo(
                paraNumero(ranges.get(0)),
                paraNumero(ranges.get(1)),
                paraNumero(ranges.get(2)));
    }

    private void escreverNumero(String celula, BigDecimal valor) throws Exception {
        ValueRange body = new ValueRange().setValues(List.of(List.of(valor.doubleValue())));

        // RAW grava o número como número, sem passar pelo parser de texto
        // da planilha (que depende do locale dela).
        sheetsService.spreadsheets().values()
                .update(spreadsheetId, range(celula), body)
                .setValueInputOption("RAW")
                .execute();
    }

    private String range(String celula) {
        return "'" + sheetName + "'!" + celula;
    }

    private static BigDecimal paraNumero(ValueRange valueRange) {
        List<List<Object>> valores = valueRange.getValues();
        if (valores == null || valores.isEmpty() || valores.get(0).isEmpty()) {
            return BigDecimal.ZERO;
        }
        Object bruto = valores.get(0).get(0);
        if (bruto instanceof Number numero) {
            return new BigDecimal(numero.toString());
        }
        String texto = bruto.toString().trim();
        if (texto.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(texto);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Célula %s não contém número: %s".formatted(valueRange.getRange(), texto), e);
        }
    }
}

package com.brenohbs.assistentefinanceiro.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Lê e escreve valores de gastos na aba "Acompanhamento" da planilha.
 *
 * Convenção da planilha: coluna E = Janeiro, F = Fevereiro, ... P = Dezembro
 * (12 colunas seguidas, uma por mês). O mês é sempre o mês atual do
 * sistema — se quiser lançar um gasto de um mês passado, ajuste
 * manualmente na planilha por enquanto.
 */
@Service
public class GoogleSheetsService {

    private final Sheets sheetsService;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    @Value("${google.sheets.sheet-name}")
    private String sheetName;

    public GoogleSheetsService(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    /**
     * Soma um valor ao gasto já lançado na linha informada, na coluna do
     * mês atual. Se a célula estiver vazia, passa a valer o próprio valor
     * lançado agora.
     *
     * @param linha número da linha do item na planilha (ver CatalogoGastos)
     * @param valor valor do gasto a somar (ex: 45.90)
     * @return o novo total acumulado naquela célula, após a soma
     */
    public double registrarGasto(int linha, double valor) throws Exception {
        String coluna = colunaDoMesAtual();
        String celula = coluna + linha;

        double valorAtual = lerValorNumerico(celula);
        double novoValor = valorAtual + valor;

        escreverValor(celula, novoValor);
        return novoValor;
    }

    /**
     * Lê o valor atual (já lançado) de uma célula específica, tratando
     * célula vazia como zero.
     */
    public double lerValorNumerico(String celula) throws Exception {
        String range = "'" + sheetName + "'!" + celula;
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> valores = response.getValues();
        if (valores == null || valores.isEmpty() || valores.get(0).isEmpty()) {
            return 0.0;
        }

        String bruto = valores.get(0).get(0).toString()
                .replace("R$", "")
                .replace(".", "")   // separador de milhar (padrão BR)
                .replace(",", ".")  // separador decimal (padrão BR) -> ponto
                .trim();

        if (bruto.isEmpty() || bruto.equals("-")) {
            return 0.0;
        }
        return Double.parseDouble(bruto);
    }

    /**
     * Escreve um valor numérico numa célula específica (sobrescreve).
     */
    public void escreverValor(String celula, double valor) throws Exception {
        String range = "'" + sheetName + "'!" + celula;
        ValueRange body = new ValueRange().setValues(List.of(List.of(valor)));

        sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    /**
     * Retorna a letra da coluna correspondente ao mês atual do sistema.
     * Janeiro = E, Fevereiro = F, ..., Dezembro = P.
     */
    public String colunaDoMesAtual() {
        int mes = LocalDate.now().getMonthValue(); // 1 = Janeiro ... 12 = Dezembro
        char letra = (char) ('E' + (mes - 1));
        return String.valueOf(letra);
    }
}

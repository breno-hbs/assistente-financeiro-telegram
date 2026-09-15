package com.brenohbs.assistentefinanceiro.model;

/**
 * Representa uma linha específica de gasto dentro da aba "Acompanhamento".
 *
 * @param nome  nome exibido no menu do bot (ex: "Internet", "Gasolina")
 * @param linha número da linha na planilha onde esse item fica (ex: 19)
 */
public record ItemGasto(String nome, int linha) {
}

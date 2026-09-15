package com.brenohbs.assistentefinanceiro.model;

import java.util.List;

/**
 * Uma categoria do orçamento (ex: "Despesas Essenciais", "Lazer") e os
 * itens de gasto que pertencem a ela.
 *
 * @param nome  nome exibido no menu do bot
 * @param itens itens de gasto dentro dessa categoria
 */
public record Categoria(String nome, List<ItemGasto> itens) {
}

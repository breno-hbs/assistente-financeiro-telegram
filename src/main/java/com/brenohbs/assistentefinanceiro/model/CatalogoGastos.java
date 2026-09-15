package com.brenohbs.assistentefinanceiro.model;

import java.util.List;

/**
 * Catálogo fixo das categorias e itens de gasto, na mesma ordem em que
 * aparecem na aba "Acompanhamento" da planilha.
 *
 * Linhas confirmadas diretamente na planilha do Breno (setembro/2026).
 */
public class CatalogoGastos {

    public static final List<Categoria> CATEGORIAS = List.of(

            new Categoria("Despesas Essenciais", List.of(
                    new ItemGasto("Garagem", 13),
                    new ItemGasto("Aluguel", 14),
                    new ItemGasto("Figma", 15),
                    new ItemGasto("Suplementação", 16),
                    new ItemGasto("Luz", 17),
                    new ItemGasto("Dentista", 18),
                    new ItemGasto("Internet", 19),
                    new ItemGasto("Academia", 20),
                    new ItemGasto("Outro", 21),
                    new ItemGasto("Cartão de Crédito", 22),
                    new ItemGasto("Farmácia", 23),
                    new ItemGasto("Lovable", 24),
                    new ItemGasto("Acordo Nubank", 25),
                    new ItemGasto("Celular", 26),
                    new ItemGasto("Claude PRO", 27),
                    new ItemGasto("Supermercado", 28),
                    new ItemGasto("Gasolina", 29),
                    new ItemGasto("TotalPass", 30),
                    new ItemGasto("Transporte", 31),
                    new ItemGasto("Cabelo", 32)
                    // linha 33 = "Necessidades Básicas" (total da categoria)
            )),

            new Categoria("Lazer", List.of(
                    new ItemGasto("Alimentação (Gastos extras)", 38),
                    new ItemGasto("Limpeza de Pele", 39),
                    new ItemGasto("Cabelo", 40),
                    new ItemGasto("Atrasos", 41),
                    new ItemGasto("Ajuste", 42),
                    new ItemGasto("Uber", 43),
                    new ItemGasto("Presentes", 44),
                    new ItemGasto("Tatuagem", 45)
                    // linha 46 = "Despesas Não Essenciais" (total da categoria)
            )),

            new Categoria("Educação", List.of(
                    new ItemGasto("Educação", 51)
            )),

            new Categoria("Reserva de Emergência", List.of(
                    new ItemGasto("Reserva de Emergência", 53)
            ))

            // "Liberdade financeira" (linha 52) fica de fora do menu por enquanto,
            // já que a meta atual é focar 100% em Reserva de Emergência.
    );
}
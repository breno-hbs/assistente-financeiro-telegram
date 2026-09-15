package com.brenohbs.assistentefinanceiro.model;

/**
 * Guarda em que ponto da conversa um número de telefone está.
 * Fica só em memória (perde tudo se a aplicação reiniciar) — é suficiente
 * pra um bot de uso pessoal, sem precisar de banco de dados.
 */
public class SessaoUsuario {

    private EstadoConversa estado = EstadoConversa.AGUARDANDO_INICIO;
    private Categoria categoriaEscolhida;
    private ItemGasto itemEscolhido;

    public EstadoConversa getEstado() {
        return estado;
    }

    public void setEstado(EstadoConversa estado) {
        this.estado = estado;
    }

    public Categoria getCategoriaEscolhida() {
        return categoriaEscolhida;
    }

    public void setCategoriaEscolhida(Categoria categoriaEscolhida) {
        this.categoriaEscolhida = categoriaEscolhida;
    }

    public ItemGasto getItemEscolhido() {
        return itemEscolhido;
    }

    public void setItemEscolhido(ItemGasto itemEscolhido) {
        this.itemEscolhido = itemEscolhido;
    }

    public void reiniciar() {
        this.estado = EstadoConversa.AGUARDANDO_INICIO;
        this.categoriaEscolhida = null;
        this.itemEscolhido = null;
    }
}

package com.ifsp.gamificacao.core.domain.model;

public enum TipoAcaoGamificacao {
    CRIAR_POST(-100L),
    CRIAR_COMUNIDADE(-1500L),
    CRIAR_COMENTARIO(5L),
    VOTAR(1L),
    RECEBER_UPVOTE(2L),
    RECEBER_DOWNVOTE(-1L),
    RECEBER_COMENTARIO(5L),
    RECEBER_POST_EM_COMUNIDADE(10L);

    private final long pontos;

    TipoAcaoGamificacao(long pontos) {
        this.pontos = pontos;
    }

    public long getPontos() {
        return pontos;
    }
}
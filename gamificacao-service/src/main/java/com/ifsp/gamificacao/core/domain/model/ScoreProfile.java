package com.ifsp.gamificacao.core.domain.model;

import com.ifsp.gamificacao.core.domain.exception.BusinessRuleException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScoreProfile {

    private Long userId;
    private Long pontosSaldo;
    private Long pontosRecorde;
    private NivelEngajamento nivel;
    private RankEngajamento rank;

    
    public void adicionarPontos(long quantidade) {
        if (quantidade < 0 && (this.pontosSaldo + quantidade < 0)) {
            throw new BusinessRuleException("Saldo de pontos insuficiente para realizar esta ação.");
        }

        this.pontosSaldo += quantidade;

        if (quantidade > 0 && this.pontosSaldo > this.pontosRecorde) {
            this.pontosRecorde = this.pontosSaldo;
        }

        atualizarNivel();
    }

    private void atualizarNivel() {
        if (this.pontosRecorde >= 1500) {
            this.nivel = NivelEngajamento.GOLD;
        } else if (this.pontosRecorde >= 500) {
            this.nivel = NivelEngajamento.PRATA;
        } else {
            this.nivel = NivelEngajamento.BRONZE;
        }
    }
}
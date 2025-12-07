package com.ifsp.gamificacao.core.usecases;

import com.ifsp.gamificacao.core.domain.model.RankEngajamento;
import com.ifsp.gamificacao.core.domain.model.ScoreProfile;
import com.ifsp.gamificacao.core.ports.ScoreRepositoryPort;

import java.util.List;

public class AtualizarRanksUseCase {

    private final ScoreRepositoryPort repository;
    
    private static final int TOP_DIAMANTE = 10;
    private static final int TOP_PLATINA = 100;

    public AtualizarRanksUseCase(ScoreRepositoryPort repository) {
        this.repository = repository;
    }

    public void executar() {
        List<ScoreProfile> topUsers = repository.buscarTopUsers(TOP_PLATINA);

        for (int i = 0; i < topUsers.size(); i++) {
            ScoreProfile profile = topUsers.get(i);
            
            if (i < TOP_DIAMANTE) {
                profile.setRank(RankEngajamento.DIAMANTE);
            } else {
                profile.setRank(RankEngajamento.PLATINA);
            }
        }

        repository.salvarTodos(topUsers);
        
    }
}
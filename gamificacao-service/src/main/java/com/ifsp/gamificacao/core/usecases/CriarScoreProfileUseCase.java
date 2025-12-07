package com.ifsp.gamificacao.core.usecases;

import com.ifsp.gamificacao.core.domain.model.NivelEngajamento;
import com.ifsp.gamificacao.core.domain.model.RankEngajamento;
import com.ifsp.gamificacao.core.domain.model.ScoreProfile;
import com.ifsp.gamificacao.core.ports.ScoreRepositoryPort;

public class CriarScoreProfileUseCase {
    private final ScoreRepositoryPort repository;

    public CriarScoreProfileUseCase(ScoreRepositoryPort repository) {
        this.repository = repository;
    }

    public ScoreProfile executar(Long userId) {
        return repository.buscarPorUserId(userId)
                .orElseGet(() -> repository.salvar(
                    new ScoreProfile(userId, 300L, 300L, NivelEngajamento.BRONZE, RankEngajamento.NENHUM)
                ));
    }
}
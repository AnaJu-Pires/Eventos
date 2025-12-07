package com.ifsp.gamificacao.core.usecases;

import com.ifsp.gamificacao.core.domain.model.ScoreProfile;
import com.ifsp.gamificacao.core.ports.ScoreRepositoryPort;
import java.util.Optional;

public class ConsultarScoreUseCase {
    private final ScoreRepositoryPort repository;

    public ConsultarScoreUseCase(ScoreRepositoryPort repository) {
        this.repository = repository;
    }

    public Optional<ScoreProfile> executar(Long userId) {
        return repository.buscarPorUserId(userId);
    }
}
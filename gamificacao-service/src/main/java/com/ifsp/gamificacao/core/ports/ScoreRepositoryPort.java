package com.ifsp.gamificacao.core.ports;

import com.ifsp.gamificacao.core.domain.model.ScoreProfile;
import java.util.List;
import java.util.Optional;

public interface ScoreRepositoryPort {
    Optional<ScoreProfile> buscarPorUserId(Long userId);
    ScoreProfile salvar(ScoreProfile scoreProfile);
    List<ScoreProfile> buscarTopUsers(int limite);
    void salvarTodos(List<ScoreProfile> profiles);
}
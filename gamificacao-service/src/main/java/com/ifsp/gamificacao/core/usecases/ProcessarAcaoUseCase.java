package com.ifsp.gamificacao.core.usecases;

import com.ifsp.gamificacao.core.domain.model.NivelEngajamento;
import com.ifsp.gamificacao.core.domain.model.RankEngajamento;
import com.ifsp.gamificacao.core.domain.model.ScoreProfile;
import com.ifsp.gamificacao.core.domain.model.TipoAcaoGamificacao;
import com.ifsp.gamificacao.core.ports.ScoreRepositoryPort;

public class ProcessarAcaoUseCase {

    private final ScoreRepositoryPort repository;

    public ProcessarAcaoUseCase(ScoreRepositoryPort repository) {
        this.repository = repository;
    }

    public ScoreProfile executar(Long userId, TipoAcaoGamificacao acao) {
        
        ScoreProfile profile = repository.buscarPorUserId(userId)
            .orElse(new ScoreProfile(userId, 0L, 0L, NivelEngajamento.BRONZE, RankEngajamento.NENHUM));

        long pontos = acao.getPontos();

        profile.adicionarPontos(pontos);

        return repository.salvar(profile);
    }
}
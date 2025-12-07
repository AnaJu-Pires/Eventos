package br.ifsp.events.service;

import br.ifsp.events.dto.gamification.GamificationScoreDTO;
import br.ifsp.events.model.TipoAcaoGamificacao;
import br.ifsp.events.model.User;

public interface GamificationService {
    /**
     * @param usuario
     * @param tipo
     */
    void registrarAcao(User usuario, TipoAcaoGamificacao tipo);

    /**
     * @param userId
     */
    void inicializarPerfil(Long userId);

    /**
     * @param userId
     * @return
     */
    GamificationScoreDTO buscarPontuacao(Long userId);
}


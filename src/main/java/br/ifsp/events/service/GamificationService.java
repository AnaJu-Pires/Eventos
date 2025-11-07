package br.ifsp.events.service;

import br.ifsp.events.model.TipoAcaoGamificacao;
import br.ifsp.events.model.User;

public interface GamificationService {

    /**
     * @param usuario O usuário que terá o saldo alterado.
     * @param tipo Ação que está sendo realizada (para buscar os pontos).
     */
    void registrarAcao(User usuario, TipoAcaoGamificacao tipo);

    /**
     * @param usuario O usuário.
     * @param tipo Ação com custo (ex: CRIAR_COMUNIDADE).
     */
    void checarPermissao(User usuario, TipoAcaoGamificacao tipo);
}
package br.ifsp.events.repository;

import br.ifsp.events.model.Convite;
import br.ifsp.events.model.StatusConvite;
import br.ifsp.events.model.Time;
import br.ifsp.events.model.User;

import java.util.List;
import java.util.Optional;

public interface ConviteRepository {

    /**
     * @param convite
     * @return
     */
    Convite save(Convite convite);

    /**
     * @param id
     * @return
     */
    Optional<Convite> findById(Long id);

    /**
     * @param usuarioLogado
     * @param usuario
     * @return
     */
    Optional<Convite> findByIdAndUsuarioConvidado(Long id, User usuario);


    /**
     * @param usuario
     * @param status
     * @return
     */
    List<Convite> findByUsuarioConvidadoAndStatus(User usuario, StatusConvite status);

    /**
     * @param time
     * @param usuario
     * @param status
     * @return
     */
    boolean existsByTimeAndUsuarioConvidadoAndStatus(Time time, User usuario, StatusConvite status);

    /**
     * @param convite
     */
    void delete(Convite convite);
}

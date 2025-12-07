package br.ifsp.events.repository;

import br.ifsp.events.model.Convite;
import br.ifsp.events.model.StatusConvite;
import br.ifsp.events.model.Time;
import br.ifsp.events.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConviteRepository extends JpaRepository<Convite, Long> {

    /**
     * @param id
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
     * @param status
     * @param agora
     * @return
     */
    List<Convite> findByStatusAndDataExpiracaoBefore(StatusConvite status, LocalDateTime agora);
}
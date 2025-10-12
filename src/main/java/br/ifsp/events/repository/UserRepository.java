package br.ifsp.events.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.User;

public interface UserRepository {
    /**
     * @param user 
     * @return 
     */
    User save(User user);

    /**
     * @param email
     * @return
     */
    boolean existsByEmail(String email);

    /**
     * @param token
     * @return 
     */
    Optional<User> findByTokenConfirmacao(String token);

    /**
     * @param status
     * @param now
     * @return
     */
    List<User> findAllByStatusUserAndDataExpiracaoTokenConfirmacaoBefore(StatusUser status, LocalDateTime now);
    
    //deleta a lista de usuarios(usado para remover usuarios inativos(nao confirmaram o token))
    /**
     * @param users
     */
    void deleteAll(List<User> users);

    Optional<User> findByEmail(String email);
}

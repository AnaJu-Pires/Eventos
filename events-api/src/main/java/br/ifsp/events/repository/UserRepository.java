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
     * @return
     */
    List<User> findAll();

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
    
    /**
     * @param id
     */
    void deleteById(Long id);

    /**
     * @param users
     */
    void deleteAll(List<User> users);

    /**
     * @param email
     * @return
     */
    Optional<User> findByEmail(String email);

    /**
     * @param id
     * @return
     */
    Optional<User> findById(Long id);

    /**
     * @return
     */
    long count();

}
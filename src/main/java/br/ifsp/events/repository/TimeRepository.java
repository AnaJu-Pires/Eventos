package br.ifsp.events.repository;

import br.ifsp.events.model.Time;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TimeRepository {

    /**
     * @param time
     * @return
     */
    Time save(Time time);

    /**
     * @param id
     * @return
     */
    Optional<Time> findById(Long id);

    /**
     * @param id
     */
    void deleteById(Long id);

    /**
     * @param pageable
     * @return
     */
    Page<Time> findAll(Pageable pageable);
}
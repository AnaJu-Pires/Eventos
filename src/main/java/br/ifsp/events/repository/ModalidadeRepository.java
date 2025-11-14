package br.ifsp.events.repository;

import br.ifsp.events.model.Modalidade;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.Set;

public interface ModalidadeRepository extends JpaRepository<Modalidade, Long> {

    Optional<Modalidade> findByNome(String nome);
    Set<Modalidade> findByIdIn(Set<Long> ids);
}
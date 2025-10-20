package br.ifsp.events.repository;

import br.ifsp.events.model.Modalidade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Set;

public interface ModalidadeRepository extends JpaRepository<Modalidade, Long> {
    Set<Modalidade> findByIdIn(Set<Long> ids);
}
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModalidadeRepository extends JpaRepository<Modalidade, Long> {

    // Novo método para buscar uma modalidade pelo nome
    Optional<Modalidade> findByNome(String nome);
    
}

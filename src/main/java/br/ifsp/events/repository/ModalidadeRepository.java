package br.ifsp.events.repository;

import br.ifsp.events.model.Modalidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModalidadeRepository extends JpaRepository<Modalidade, Long> {

    // Novo método para buscar uma modalidade pelo nome
    Optional<Modalidade> findByNome(String nome);
    
}
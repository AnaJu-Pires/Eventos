package br.ifsp.events.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.ifsp.events.model.EventoModalidade;

@Repository
public interface EventoModalidadeRepository extends JpaRepository<EventoModalidade, Long> {
}

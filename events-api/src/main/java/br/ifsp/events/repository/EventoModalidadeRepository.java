package br.ifsp.events.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.ifsp.events.model.EventoModalidade;
import br.ifsp.events.model.FormatoEventoModalidade;
import java.util.List;

@Repository
public interface EventoModalidadeRepository extends JpaRepository<EventoModalidade, Long> {

    List<EventoModalidade> findAllByEvento_IdAndFormatoEventoModalidade(Long eventoId, FormatoEventoModalidade formato);
}
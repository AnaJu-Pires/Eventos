package br.ifsp.events.repository;

import java.time.LocalDate;
import java.util.List;

import br.ifsp.events.model.Evento;
import br.ifsp.events.model.StatusEvento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, Long> {

	List<Evento> findByStatusAndDataInicioLessThanEqual(StatusEvento status, LocalDate data);

	List<Evento> findByStatusAndDataFimLessThanEqual(StatusEvento status, LocalDate data);

}

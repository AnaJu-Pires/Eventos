package br.ifsp.events.repository;

import java.time.LocalDate;
import java.util.List;

import br.ifsp.events.model.Evento;
import br.ifsp.events.model.StatusEvento;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório para entidade Evento.
 * Fornece operações de persistência e consulta para eventos.
 */
public interface EventoRepository extends JpaRepository<Evento, Long> {

    /**
     * Encontra eventos com status específico e data de início menor ou igual à data de referência.
     * 
     * @param statusAtual status atual do evento
     * @param dataReferencia data de referência para comparação
     * @return lista de eventos que atendem aos critérios
     */
    List<Evento> findByStatusAndDataInicioLessThanEqual(StatusEvento statusAtual, LocalDate dataReferencia);

    /**
     * Encontra eventos com status específico e data de fim menor ou igual à data de referência.
     * 
     * @param statusAtual status atual do evento
     * @param dataReferencia data de referência para comparação
     * @return lista de eventos que atendem aos critérios
     */
    List<Evento> findByStatusAndDataFimLessThanEqual(StatusEvento statusAtual, LocalDate dataReferencia);

}

package br.ifsp.events.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.events.model.Evento;
import br.ifsp.events.model.StatusEvento;
import br.ifsp.events.repository.EventoRepository;

@Service
public class EventStatusScheduler {

    private final EventoRepository eventoRepository;

    public EventStatusScheduler(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    // Executa diariamente à meia-noite. Ajuste conforme necessário.
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateEventStatuses() {
        LocalDate today = LocalDate.now();

        // PLANEJADO -> EM_ANDAMENTO quando dataInicio <= hoje
        List<Evento> toStart = eventoRepository.findByStatusAndDataInicioLessThanEqual(StatusEvento.PLANEJADO, today);
        toStart.forEach(e -> e.setStatus(StatusEvento.EM_ANDAMENTO));
        if (!toStart.isEmpty()) {
            eventoRepository.saveAll(toStart);
        }

        // EM_ANDAMENTO -> FINALIZADO quando dataFim < hoje (evento já terminou)
        List<Evento> toFinish = eventoRepository.findByStatusAndDataFimLessThanEqual(StatusEvento.EM_ANDAMENTO, today);
        toFinish.forEach(e -> e.setStatus(StatusEvento.FINALIZADO));
        if (!toFinish.isEmpty()) {
            eventoRepository.saveAll(toFinish);
        }
    }

}

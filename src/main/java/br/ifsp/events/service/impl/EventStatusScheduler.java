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

    /**
     * Atualiza statuses de eventos periodicamente.
     * - PLANEJADO -> EM_ANDAMENTO quando dataInicio <= hoje
     * - EM_ANDAMENTO -> FINALIZADO quando dataFim <= hoje
     *
     * Roda todo dia à meia-noite. Ajuste a expressão cron caso queira outra frequência.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateEventStatuses() {
        LocalDate today = LocalDate.now();

        // Transição: PLANEJADO -> EM_ANDAMENTO
        List<Evento> toStart = eventoRepository.findByStatusAndDataInicioLessThanEqual(StatusEvento.PLANEJADO, today);
        if (!toStart.isEmpty()) {
            toStart.forEach(e -> e.setStatus(StatusEvento.EM_ANDAMENTO));
            eventoRepository.saveAll(toStart);
        }

        // Transição: EM_ANDAMENTO -> FINALIZADO
        List<Evento> toFinish = eventoRepository.findByStatusAndDataFimLessThanEqual(StatusEvento.EM_ANDAMENTO, today);
        if (!toFinish.isEmpty()) {
            toFinish.forEach(e -> e.setStatus(StatusEvento.FINALIZADO));
            eventoRepository.saveAll(toFinish);
        }
    }
}

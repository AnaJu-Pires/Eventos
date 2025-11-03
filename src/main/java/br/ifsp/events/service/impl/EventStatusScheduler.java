package br.ifsp.events.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.events.model.Evento;
import br.ifsp.events.model.StatusEvento;
import br.ifsp.events.repository.EventoRepository;

/**
 * Serviço responsável por atualizar automaticamente o status dos eventos
 * com base em suas datas de início e fim.
 * 
 * Este serviço executa diariamente à meia-noite para:
 * - Iniciar eventos planejados cuja data de início já chegou
 * - Finalizar eventos em andamento cuja data de fim já passou
 */
@Service
public class EventStatusScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(EventStatusScheduler.class);
    private final EventoRepository eventoRepository;

    public EventStatusScheduler(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    /**
     * Atualiza o status dos eventos com base em suas datas.
     * Executa diariamente à meia-noite para manter os status sincronizados.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateEventStatuses() {
        LocalDate today = LocalDate.now();
        updateEventosParaIniciar(today);
        updateEventosParaFinalizar(today);
    }

    /**
     * Atualiza eventos planejados que devem iniciar hoje.
     */
    private void updateEventosParaIniciar(LocalDate dataReferencia) {
        List<Evento> eventosParaIniciar = eventoRepository.findByStatusAndDataInicioLessThanEqual(
            StatusEvento.PLANEJADO, 
            dataReferencia
        );

        if (!eventosParaIniciar.isEmpty()) {
            eventosParaIniciar.forEach(evento -> {
                evento.setStatus(StatusEvento.EM_ANDAMENTO);
                logger.info("Evento {} iniciado automaticamente", evento.getId());
            });
            eventoRepository.saveAll(eventosParaIniciar);
            logger.info("{} eventos atualizados para EM_ANDAMENTO", eventosParaIniciar.size());
        }
    }

    /**
     * Atualiza eventos em andamento que devem ser finalizados hoje.
     */
    private void updateEventosParaFinalizar(LocalDate dataReferencia) {
        List<Evento> eventosParaFinalizar = eventoRepository.findByStatusAndDataFimLessThanEqual(
            StatusEvento.EM_ANDAMENTO, 
            dataReferencia
        );

        if (!eventosParaFinalizar.isEmpty()) {
            eventosParaFinalizar.forEach(evento -> {
                evento.setStatus(StatusEvento.FINALIZADO);
                logger.info("Evento {} finalizado automaticamente", evento.getId());
            });
            eventoRepository.saveAll(eventosParaFinalizar);
            logger.info("{} eventos atualizados para FINALIZADO", eventosParaFinalizar.size());
        }
    }

}

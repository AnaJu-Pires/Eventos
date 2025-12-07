package br.ifsp.events.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import br.ifsp.events.service.ConviteService;

@Component
public class ConviteScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ConviteScheduler.class);
    private final ConviteService conviteService;

    public ConviteScheduler(ConviteService conviteService) {
        this.conviteService = conviteService;
    }
    @Scheduled(cron = "0 0 * * * ?")
    public void executarExpiracaoDeConvites() {
        logger.info("Iniciando tarefa agendada: Verificação de convites expirados...");
        try {
            conviteService.expirarConvitesPendentes();
            logger.info("Tarefa agendada finalizada: Verificação de convites expirados.");
        } catch (Exception e) {
            logger.error("Falha ao executar a tarefa agendada de expiração de convites.", e);
        }
    }
}
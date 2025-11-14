package br.ifsp.events.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.ifsp.events.service.GamificationService;

@Component
public class RankScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RankScheduler.class);
    private final GamificationService gamificationService;

    public RankScheduler(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    /**
     * Executa o cálculo de ranks (Top 10/100)
     * de hora em hora.
     * Cron: "0 0 * * * ?" = no minuto 0, de cada hora.
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void executarAtualizacaoDeRanks() {
        logger.info("Iniciando tarefa agendada: Atualização de Ranks de Gamificação...");
        try {
            gamificationService.atualizarRanks();
            logger.info("Tarefa agendada finalizada: Atualização de Ranks de Gamificação.");
        } catch (Exception e) {
            logger.error("Falha ao executar a tarefa agendada de atualização de ranks.", e);
        }
    }   
}
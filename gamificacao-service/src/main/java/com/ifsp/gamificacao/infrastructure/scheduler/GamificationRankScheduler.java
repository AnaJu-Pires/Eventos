package com.ifsp.gamificacao.infrastructure.scheduler;

import com.ifsp.gamificacao.core.usecases.AtualizarRanksUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GamificationRankScheduler {

    private final AtualizarRanksUseCase atualizarRanksUseCase;
    
    @Scheduled(cron = "0 0 * * * ?") 
    public void atualizarRanks() {
        System.out.println(">>> [Gamification Service] Iniciando atualização de ranks...");
        atualizarRanksUseCase.executar();
        System.out.println(">>> [Gamification Service] Ranks atualizados com sucesso.");
    }
}
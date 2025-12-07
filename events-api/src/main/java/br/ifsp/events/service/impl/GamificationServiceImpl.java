package br.ifsp.events.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import br.ifsp.events.dto.gamification.GamificationAcaoDTO;
import br.ifsp.events.dto.gamification.GamificationScoreDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.model.TipoAcaoGamificacao;
import br.ifsp.events.model.User;
import br.ifsp.events.service.GamificationService;
import reactor.core.publisher.Mono;

@Service
public class GamificationServiceImpl implements GamificationService {

    private final WebClient webClient;

    public GamificationServiceImpl(WebClient gamificationWebClient) {
        this.webClient = gamificationWebClient;
    }

    @Override
    public void registrarAcao(User usuario, TipoAcaoGamificacao tipo) {
        // Cria o DTO para mandar para o microsserviço
        GamificationAcaoDTO dto = new GamificationAcaoDTO(usuario.getId(), tipo);

        try {
            // Faz a chamada POST para http://gamificacao-app:8085/api/v1/gamification/actions
            webClient.post()
                .uri("/api/v1/gamification/actions")
                .bodyValue(dto)
                .retrieve()
                // Tratamento de erro: Se o microserviço devolver 4xx (ex: saldo insuficiente)
                .onStatus(status -> status.is4xxClientError(), response -> 
                    response.bodyToMono(String.class).flatMap(errorBody -> {
                        return Mono.error(new BusinessRuleException("Erro na gamificação: " + errorBody));
                    })
                )
                .bodyToMono(Void.class)
                .block(); // Espera a resposta (Síncrono) para garantir que salvou

        } catch (Exception e) {
            // Se for erro de regra de negócio (ex: sem saldo), paramos tudo e avisamos o front
            if (e instanceof BusinessRuleException) {
                throw e;
            }
            // Se for erro técnico (microserviço fora do ar), apenas logamos e deixamos o fluxo seguir
            // Assim o usuário consegue postar mesmo se a gamificação cair (Resiliência simples)
            System.err.println("ALERTA: Falha ao comunicar com Gamification Service: " + e.getMessage());
        }
    }

    // MÉTODOS checarPermissao E atualizarRanks FORAM APAGADOS
    // Pois não existem mais na interface GamificationService

    @Override
    public void inicializarPerfil(Long userId) {
        try {
            // POST http://gamificacao-app:8085/api/v1/gamification/users/{userId}
            webClient.post()
                .uri("/api/v1/gamification/users/" + userId)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(); // Subscribe = Assíncrono (Fire and Forget)
                // Usamos subscribe aqui para não travar o cadastro do usuário se a gamificação estiver lenta
        } catch (Exception e) {
            System.err.println("Erro ao inicializar gamificação para o usuário " + userId + ": " + e.getMessage());
        }
    }

    @Override
    public GamificationScoreDTO buscarPontuacao(Long userId) {
        try {
            // GET http://gamificacao-app:8085/api/v1/gamification/scores/{userId}
            return webClient.get()
                .uri("/api/v1/gamification/scores/" + userId)
                .retrieve()
                .bodyToMono(GamificationScoreDTO.class)
                .block(); // Síncrono: precisamos dos dados AGORA para responder o front
                
        } catch (Exception e) {
            // Fallback (Resiliência): Se o microsserviço cair, retornamos zerado
            // para não travar o app do usuário.
            System.err.println("Erro ao buscar score: " + e.getMessage());
            return new GamificationScoreDTO(userId, 0L, 0L, "Indisponível", "Indisponível");
        }
    }
}
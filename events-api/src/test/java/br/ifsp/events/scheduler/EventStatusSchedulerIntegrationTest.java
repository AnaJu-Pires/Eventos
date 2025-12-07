package br.ifsp.events.scheduler;

import br.ifsp.events.model.Evento;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusEvento;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.EventoRepository;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.EmailService;
import br.ifsp.events.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Teste de Integração para o Agendador de Status de Eventos.
 * * Este teste não espera o Cron disparar (o que demoraria). 
 * Ele invoca diretamente o método updateEventStatuses() para garantir
 * que a lógica de transição de estados no banco de dados está correta.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE) // Não precisa subir servidor web aqui
@ActiveProfiles("test")
@Transactional
class EventStatusSchedulerIntegrationTest {

    @Autowired
    private EventStatusScheduler eventStatusScheduler;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UserRepository userRepository;

    // Mockamos serviços de notificação para não tentar enviar emails durante o teste do scheduler
    @MockBean
    private EmailService emailService;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("Scheduler: Deve iniciar eventos cuja data de início chegou (PLANEJADO -> EM_ANDAMENTO)")
    void deveIniciarEventosAutomaticamente() {
        // --- ARRANGE ---
        User organizador = criarOrganizador();

        // Caso 1: Evento começa hoje (Deve mudar)
        Evento eventoHoje = criarEvento(organizador, "Começa Hoje", 
                LocalDate.now(), LocalDate.now().plusDays(5), StatusEvento.PLANEJADO);

        // Caso 2: Evento começou ontem mas ainda estava planejado (Deve mudar - correção de atraso)
        Evento eventoAtrasado = criarEvento(organizador, "Começou Ontem", 
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(5), StatusEvento.PLANEJADO);

        // Caso 3: Evento começa amanhã (NÃO deve mudar)
        Evento eventoFuturo = criarEvento(organizador, "Futuro", 
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), StatusEvento.PLANEJADO);

        // --- ACT ---
        // Força a execução da tarefa agendada
        eventStatusScheduler.updateEventStatuses();

        // --- ASSERT ---
        assertEquals(StatusEvento.EM_ANDAMENTO, buscarStatusAtual(eventoHoje.getId()), 
            "Evento que começa hoje deveria ter mudado para EM_ANDAMENTO");
        
        assertEquals(StatusEvento.EM_ANDAMENTO, buscarStatusAtual(eventoAtrasado.getId()), 
            "Evento atrasado deveria ter mudado para EM_ANDAMENTO");
        
        assertEquals(StatusEvento.PLANEJADO, buscarStatusAtual(eventoFuturo.getId()), 
            "Evento futuro deve permanecer PLANEJADO");
    }

    @Test
    @DisplayName("Scheduler: Deve finalizar eventos cuja data de fim passou (EM_ANDAMENTO -> FINALIZADO)")
    void deveFinalizarEventosAutomaticamente() {
        // --- ARRANGE ---
        User organizador = criarOrganizador();

        // Caso 1: Evento terminou ontem (Deve mudar)
        // Nota: A lógica do seu scheduler usa DataFim <= DataReferencia(Hoje). 
        // Se a regra for "termina no final do dia", eventos de hoje ainda não finalizam.
        // Se a regra for "terminou dia anterior", dataFim < hoje.
        // Baseado no seu código: findByStatusAndDataFimLessThanEqual(EM_ANDAMENTO, today)
        // Isso significa que se DataFim é HOJE, ele finaliza.
        
        Evento eventoTerminouOntem = criarEvento(organizador, "Acabou Ontem", 
                LocalDate.now().minusDays(5), LocalDate.now().minusDays(1), StatusEvento.EM_ANDAMENTO);

        Evento eventoTerminaHoje = criarEvento(organizador, "Acaba Hoje", 
                LocalDate.now().minusDays(5), LocalDate.now(), StatusEvento.EM_ANDAMENTO);

        Evento eventoContinua = criarEvento(organizador, "Continua", 
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(1), StatusEvento.EM_ANDAMENTO);

        // --- ACT ---
        eventStatusScheduler.updateEventStatuses();

        // --- ASSERT ---
        assertEquals(StatusEvento.FINALIZADO, buscarStatusAtual(eventoTerminouOntem.getId()), 
            "Evento com data fim no passado deve ser FINALIZADO");

        assertEquals(StatusEvento.FINALIZADO, buscarStatusAtual(eventoTerminaHoje.getId()), 
            "Evento com data fim hoje deve ser FINALIZADO (conforme lógica LessThanEqual)");

        assertEquals(StatusEvento.EM_ANDAMENTO, buscarStatusAtual(eventoContinua.getId()), 
            "Evento que termina amanhã deve continuar EM_ANDAMENTO");
    }

    // --- Helpers ---

    private User criarOrganizador() {
        User u = new User();
        u.setNome("Organizador Scheduler");
        u.setEmail("scheduler@ifsp.edu.br");
        u.setSenha("123");
        u.setPerfilUser(PerfilUser.GESTOR_EVENTOS);
        u.setStatusUser(StatusUser.ATIVO);
        return userRepository.save(u);
    }

    private Evento criarEvento(User org, String nome, LocalDate inicio, LocalDate fim, StatusEvento status) {
        Evento e = new Evento();
        e.setNome(nome);
        e.setDescricao("Teste Scheduler");
        e.setDataInicio(inicio);
        e.setDataFim(fim);
        e.setStatus(status);
        e.setOrganizador(org);
        e.setEventoModalidades(new HashSet<>()); // Evita NullPointer se o listener tentar acessar
        return eventoRepository.save(e);
    }

    private StatusEvento buscarStatusAtual(Long id) {
        // Busca direto do banco para garantir que o commit/flush funcionou na lógica do scheduler
        return eventoRepository.findById(id).orElseThrow().getStatus();
    }
}
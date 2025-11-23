package br.ifsp.events.scheduler;

import br.ifsp.events.model.*;
import br.ifsp.events.repository.*;
import br.ifsp.events.service.EmailService;
import br.ifsp.events.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Teste de Integração para o Agendador de Convites.
 * Garante que convites não respondidos dentro do prazo sejam
 * marcados automaticamente como EXPIRADO.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class ConviteSchedulerIntegrationTest {

    @Autowired
    private ConviteScheduler conviteScheduler;

    @Autowired
    private ConviteRepository conviteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private ModalidadeRepository modalidadeRepository;

    // Mockamos serviços de notificação para focar apenas na lógica do banco
    @MockBean
    private EmailService emailService;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("Scheduler: Deve expirar convites pendentes cuja data limite passou")
    void deveExpirarConvitesVencidos() {
        // --- ARRANGE ---
        User capitao = criarUsuario("Capitao", "cap@test.com");
        User convidado1 = criarUsuario("Convidado 1", "c1@test.com");
        User convidado2 = criarUsuario("Convidado 2", "c2@test.com");
        
        Time time = criarTime(capitao, "Time Teste");

        // Caso 1: Convite VENCIDO (Data expirou 1 minuto atrás)
        Convite conviteVencido = criarConvite(time, convidado1, StatusConvite.PENDENTE, LocalDateTime.now().minusMinutes(1));

        // Caso 2: Convite VÁLIDO (Data expira amanhã)
        Convite conviteValido = criarConvite(time, convidado2, StatusConvite.PENDENTE, LocalDateTime.now().plusDays(1));

        // --- ACT ---
        // Força a execução da tarefa agendada
        conviteScheduler.executarExpiracaoDeConvites();

        // --- ASSERT ---
        // Busca do banco para confirmar persistência
        StatusConvite statusVencido = conviteRepository.findById(conviteVencido.getId()).orElseThrow().getStatus();
        StatusConvite statusValido = conviteRepository.findById(conviteValido.getId()).orElseThrow().getStatus();

        assertEquals(StatusConvite.EXPIRADO, statusVencido, "Convite com data passada deve virar EXPIRADO");
        assertEquals(StatusConvite.PENDENTE, statusValido, "Convite no prazo deve continuar PENDENTE");
    }

    @Test
    @DisplayName("Scheduler: Não deve alterar convites que já foram respondidos (ACEITO/RECUSADO)")
    void naoDeveAlterarConvitesRespondidos() {
        // --- ARRANGE ---
        User capitao = criarUsuario("Capitao 2", "cap2@test.com");
        User membro = criarUsuario("Membro", "membro@test.com");
        Time time = criarTime(capitao, "Time Respondido");

        // Convite que foi ACEITO mas a data de expiração (original) já passou
        Convite conviteAceito = criarConvite(time, membro, StatusConvite.ACEITO, LocalDateTime.now().minusDays(1));

        // --- ACT ---
        conviteScheduler.executarExpiracaoDeConvites();

        // --- ASSERT ---
        StatusConvite statusAtual = conviteRepository.findById(conviteAceito.getId()).orElseThrow().getStatus();
        assertEquals(StatusConvite.ACEITO, statusAtual, "Convite já ACEITO não deve ser alterado pelo scheduler");
    }

    // --- Helpers ---

    private User criarUsuario(String nome, String email) {
        User u = new User();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha("123");
        u.setPerfilUser(PerfilUser.ALUNO);
        u.setStatusUser(StatusUser.ATIVO);
        return userRepository.save(u);
    }

    private Time criarTime(User capitao, String nomeTime) {
        Modalidade m = new Modalidade();
        m.setNome("Mod " + nomeTime); // Nome único para a modalidade
        modalidadeRepository.save(m);

        Time t = new Time();
        t.setNome(nomeTime);
        t.setCapitao(capitao);
        t.setModalidade(m);
        t.setMembros(new HashSet<>(Set.of(capitao)));
        return timeRepository.save(t);
    }

    private Convite criarConvite(Time time, User convidado, StatusConvite status, LocalDateTime expiracao) {
        Convite c = new Convite();
        c.setTime(time);
        c.setUsuarioConvidado(convidado);
        c.setStatus(status);
        c.setDataExpiracao(expiracao);
        return conviteRepository.save(c);
    }
}
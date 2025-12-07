package br.ifsp.events.controller;

import br.ifsp.events.model.Evento;
import br.ifsp.events.model.EventoModalidade;
import br.ifsp.events.model.FormatoEventoModalidade;
import br.ifsp.events.model.Inscricao;
import br.ifsp.events.model.Modalidade;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusEvento;
import br.ifsp.events.model.StatusInscricao;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.Time;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.EventoModalidadeRepository;
import br.ifsp.events.repository.EventoRepository;
import br.ifsp.events.repository.InscricaoRepository;
import br.ifsp.events.repository.ModalidadeRepository;
import br.ifsp.events.repository.TimeRepository;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.EmailService;
import br.ifsp.events.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InscricaoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private ModalidadeRepository modalidadeRepository;

    @Autowired
    private EventoModalidadeRepository eventoModalidadeRepository;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private InscricaoRepository inscricaoRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("Integração: Deve aprovar inscrição com sucesso (200 OK)")
    void deveAprovarInscricao() throws Exception {
        // --- ARRANGE ---
        User gestor = criarUsuario("Gestor Aprova", "gestor.aprova@ifsp.edu.br", PerfilUser.GESTOR_EVENTOS);
        Evento evento = criarEvento(gestor);
        Modalidade modalidade = criarModalidade("Basquete");
        EventoModalidade em = criarEventoModalidade(evento, modalidade);
        User capitao = criarUsuario("Capitão Time", "capitao.time@ifsp.edu.br", PerfilUser.ALUNO);
        Time time = criarTime(capitao, modalidade);

        Inscricao inscricao = new Inscricao();
        inscricao.setTime(time);
        inscricao.setEventoModalidade(em);
        inscricao.setStatusInscricao(StatusInscricao.PENDENTE);
        inscricao = inscricaoRepository.save(inscricao);

        // 2. Autenticação Manual (Gestor)
        Authentication auth = new UsernamePasswordAuthenticationToken(gestor, null, gestor.getAuthorities());

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/inscricoes/{id}/aprovar", inscricao.getId())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusInscricao").value("APROVADA"));
    }

    @Test
    @DisplayName("Integração: Deve rejeitar inscrição com sucesso (200 OK)")
    void deveRejeitarInscricao() throws Exception {
        // --- ARRANGE ---
        User gestor = criarUsuario("Gestor Rejeita", "gestor.rejeita@ifsp.edu.br", PerfilUser.GESTOR_EVENTOS);
        Evento evento = criarEvento(gestor);
        Modalidade modalidade = criarModalidade("Handebol");
        EventoModalidade em = criarEventoModalidade(evento, modalidade);
        User capitao = criarUsuario("Capitão Time 2", "capitao2@ifsp.edu.br", PerfilUser.ALUNO);
        Time time = criarTime(capitao, modalidade);

        Inscricao inscricao = new Inscricao();
        inscricao.setTime(time);
        inscricao.setEventoModalidade(em);
        inscricao.setStatusInscricao(StatusInscricao.PENDENTE);
        inscricao = inscricaoRepository.save(inscricao);

        Authentication auth = new UsernamePasswordAuthenticationToken(gestor, null, gestor.getAuthorities());

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/inscricoes/{id}/rejeitar", inscricao.getId())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusInscricao").value("REJEITADA"));
    }

    @Test
    @DisplayName("Integração: Deve falhar se usuário não for o dono do evento (400/403)")
    void deveFalharSeNaoForDono() throws Exception {
        // --- ARRANGE ---
        User donoReal = criarUsuario("Dono Real", "dono@ifsp.edu.br", PerfilUser.GESTOR_EVENTOS);
        User intruso = criarUsuario("Intruso", "intruso@ifsp.edu.br", PerfilUser.GESTOR_EVENTOS);
        
        Evento evento = criarEvento(donoReal); 
        // CORREÇÃO: Mudado nome da modalidade para não conflitar com o DataLoader (que cria "Xadrez")
        Modalidade modalidade = criarModalidade("Xadrez Teste"); 
        EventoModalidade em = criarEventoModalidade(evento, modalidade);
        User capitao = criarUsuario("Capitão 3", "cap3@ifsp.edu.br", PerfilUser.ALUNO);
        Time time = criarTime(capitao, modalidade);

        Inscricao inscricao = new Inscricao();
        inscricao.setTime(time);
        inscricao.setEventoModalidade(em);
        inscricao.setStatusInscricao(StatusInscricao.PENDENTE);
        inscricaoRepository.save(inscricao);

        // Autentica como INTRUSO (que tem permissão de Gestor, mas não é o dono deste evento)
        Authentication auth = new UsernamePasswordAuthenticationToken(intruso, null, intruso.getAuthorities());

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/inscricoes/{id}/aprovar", inscricao.getId())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); 
    }

    // --- Helper Methods para facilitar a criação de dados ---

    private User criarUsuario(String nome, String email, PerfilUser perfil) {
        User user = new User();
        user.setNome(nome);
        user.setEmail(email);
        user.setSenha("123");
        user.setPerfilUser(perfil);
        user.setStatusUser(StatusUser.ATIVO);
        return userRepository.save(user);
    }

    private Evento criarEvento(User organizador) {
        Evento evento = new Evento();
        evento.setNome("Evento Teste");
        evento.setDescricao("Desc");
        evento.setDataInicio(LocalDate.now().plusDays(10));
        evento.setDataFim(LocalDate.now().plusDays(12));
        evento.setStatus(StatusEvento.PLANEJADO);
        evento.setOrganizador(organizador);
        evento.setEventoModalidades(new HashSet<>());
        return eventoRepository.save(evento);
    }

    private Modalidade criarModalidade(String nome) {
        Modalidade m = new Modalidade();
        m.setNome(nome);
        return modalidadeRepository.save(m);
    }

    private EventoModalidade criarEventoModalidade(Evento evt, Modalidade mod) {
        EventoModalidade em = new EventoModalidade();
        em.setEvento(evt);
        em.setModalidade(mod);
        em.setFormatoEventoModalidade(FormatoEventoModalidade.MATA_MATA);
        em.setMaxTimes(8);
        em.setDataFimInscricao(LocalDate.now().plusDays(5));
        return eventoModalidadeRepository.save(em);
    }

    private Time criarTime(User capitao, Modalidade modalidade) {
        Time t = new Time();
        t.setNome("Time " + modalidade.getNome());
        t.setCapitao(capitao);
        t.setModalidade(modalidade);
        t.setMembros(new HashSet<>());
        t.getMembros().add(capitao);
        return timeRepository.save(t);
    }
}
package br.ifsp.events.controller;

import br.ifsp.events.dto.inscricao.InscricaoRequestDTO;
import br.ifsp.events.model.*;
import br.ifsp.events.repository.*;
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
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EventInscricaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("Integração: Capitão deve inscrever time com sucesso (Status PENDENTE)")
    void deveInscreverTimeComSucesso() throws Exception {
        // --- ARRANGE ---
        // 1. Cenário: Evento com inscrições abertas
        User organizador = criarUsuario("Organizador", "org@test.com");
        Evento evento = criarEvento(organizador);
        Modalidade futsal = criarModalidade("Futsal Inscrição");
        
        // Configura modalidade no evento com data limite futura
        // O método auxiliar agora atualiza o objeto 'evento' para evitar o erro 400
        EventoModalidade em = criarEventoModalidade(evento, futsal, 8, LocalDate.now().plusDays(5));

        // 2. Capitão e Time
        User capitao = criarUsuario("Capitão", "cap@test.com");
        Time time = criarTime(capitao, futsal);

        // 3. Autenticação do Capitão
        Authentication auth = new UsernamePasswordAuthenticationToken(capitao, null, capitao.getAuthorities());

        InscricaoRequestDTO requestDTO = new InscricaoRequestDTO(time.getId());

        // --- ACT ---
        mockMvc.perform(post("/events/{id}/inscrever", evento.getId())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                
        // --- ASSERT ---
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusInscricao").value("PENDENTE"))
                .andExpect(jsonPath("$.nomeTime").value(time.getNome()));
    }

    @Test
    @DisplayName("Integração: Não deve inscrever se data de inscrição expirou (400 Bad Request)")
    void naoDeveInscreverComDataExpirada() throws Exception {
        // --- ARRANGE ---
        User organizador = criarUsuario("Organizador", "org2@test.com");
        Evento evento = criarEvento(organizador);
        Modalidade volei = criarModalidade("Vôlei");
        
        // Data fim inscrição foi ontem
        criarEventoModalidade(evento, volei, 8, LocalDate.now().minusDays(1));

        User capitao = criarUsuario("Capitão Atrasado", "atrasado@test.com");
        Time time = criarTime(capitao, volei);

        Authentication auth = new UsernamePasswordAuthenticationToken(capitao, null, capitao.getAuthorities());
        InscricaoRequestDTO requestDTO = new InscricaoRequestDTO(time.getId());

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/events/{id}/inscrever", evento.getId())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest()); // Espera erro de regra de negócio
    }

    @Test
    @DisplayName("Integração: Não deve inscrever se limite de times foi atingido (400 Bad Request)")
    void naoDeveInscreverSeLotado() throws Exception {
        // --- ARRANGE ---
        User organizador = criarUsuario("Organizador", "org3@test.com");
        Evento evento = criarEvento(organizador);
        Modalidade basquete = criarModalidade("Basquete");
        
        // Limite de 1 time apenas
        EventoModalidade em = criarEventoModalidade(evento, basquete, 1, LocalDate.now().plusDays(5));

        // Ocupa a vaga existente
        User rival = criarUsuario("Rival", "rival@test.com");
        Time timeRival = criarTime(rival, basquete);
        criarInscricao(timeRival, em, StatusInscricao.APROVADA);

        // Capitão tentando a 2ª vaga (que não existe)
        User capitao = criarUsuario("Capitão Sem Vaga", "semvaga@test.com");
        Time time = criarTime(capitao, basquete);

        Authentication auth = new UsernamePasswordAuthenticationToken(capitao, null, capitao.getAuthorities());
        InscricaoRequestDTO requestDTO = new InscricaoRequestDTO(time.getId());

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/events/{id}/inscrever", evento.getId())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Integração: Apenas o capitão pode inscrever o time (400 Bad Request)")
    void naoDevePermitirMembroInscrever() throws Exception {
        // --- ARRANGE ---
        User organizador = criarUsuario("Organizador", "org4@test.com");
        Evento evento = criarEvento(organizador);
        Modalidade handebol = criarModalidade("Handebol");
        criarEventoModalidade(evento, handebol, 8, LocalDate.now().plusDays(5));

        User capitao = criarUsuario("Capitão Real", "real@test.com");
        User impostor = criarUsuario("Impostor", "impostor@test.com"); // É membro, mas não capitão
        
        Time time = criarTime(capitao, handebol);
        time.getMembros().add(impostor); // Adiciona impostor como membro
        timeRepository.save(time);

        // Autentica como Impostor
        Authentication auth = new UsernamePasswordAuthenticationToken(impostor, null, impostor.getAuthorities());
        InscricaoRequestDTO requestDTO = new InscricaoRequestDTO(time.getId());

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/events/{id}/inscrever", evento.getId())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest()); // Regra: "Apenas o capitão..."
    }

    // --- Helpers ---

    private User criarUsuario(String nome, String email) {
        User user = new User();
        user.setNome(nome);
        user.setEmail(email);
        user.setSenha("123");
        user.setPerfilUser(PerfilUser.ALUNO);
        user.setStatusUser(StatusUser.ATIVO);
        return userRepository.save(user);
    }

    private Evento criarEvento(User organizador) {
        Evento evento = new Evento();
        evento.setNome("Evento Teste");
        evento.setDescricao("Desc");
        evento.setDataInicio(LocalDate.now().plusDays(10));
        evento.setDataFim(LocalDate.now().plusDays(15));
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

    // --- CORREÇÃO IMPORTANTE NESTE MÉTODO ---
    private EventoModalidade criarEventoModalidade(Evento evt, Modalidade mod, int maxTimes, LocalDate dataFim) {
        EventoModalidade em = new EventoModalidade();
        em.setEvento(evt);
        em.setModalidade(mod);
        em.setFormatoEventoModalidade(FormatoEventoModalidade.MATA_MATA);
        em.setMaxTimes(maxTimes);
        em.setDataFimInscricao(dataFim);
        
        EventoModalidade saved = eventoModalidadeRepository.save(em);
        
        // Atualiza o relacionamento bidirecional no objeto 'evt' que está na memória/cache.
        // Isso garante que evt.getEventoModalidades() não esteja vazio quando o Service for consultá-lo.
        evt.getEventoModalidades().add(saved);
        
        return saved;
    }

    private Time criarTime(User capitao, Modalidade modalidade) {
        Time t = new Time();
        t.setNome("Time " + capitao.getNome());
        t.setCapitao(capitao);
        t.setModalidade(modalidade);
        t.setMembros(new HashSet<>(Set.of(capitao)));
        return timeRepository.save(t);
    }

    private void criarInscricao(Time time, EventoModalidade em, StatusInscricao status) {
        Inscricao i = new Inscricao();
        i.setTime(time);
        i.setEventoModalidade(em);
        i.setStatusInscricao(status);
        inscricaoRepository.save(i);
    }
}
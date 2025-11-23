package br.ifsp.events.controller;

import br.ifsp.events.dto.event.GerarChaveRequestDTO;
import br.ifsp.events.dto.partida.PartidaResultadoRequestDTO;
import br.ifsp.events.model.Evento;
import br.ifsp.events.model.EventoModalidade;
import br.ifsp.events.model.FormatoEventoModalidade;
import br.ifsp.events.model.Inscricao;
import br.ifsp.events.model.Modalidade;
import br.ifsp.events.model.Partida;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusEvento;
import br.ifsp.events.model.StatusInscricao;
import br.ifsp.events.model.StatusPartida;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.Time;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.EventoModalidadeRepository;
import br.ifsp.events.repository.EventoRepository;
import br.ifsp.events.repository.InscricaoRepository;
import br.ifsp.events.repository.ModalidadeRepository;
import br.ifsp.events.repository.PartidaRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PartidaFluxoIntegrationTest {

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
    @Autowired
    private PartidaRepository partidaRepository;

    @MockBean
    private EmailService emailService;
    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("Integração: Deve gerar chave Mata-Mata e atualizar resultado da partida")
    void deveGerarChaveEAtualizarResultado() throws Exception {
        // ======================== ARRANGE (PREPARAÇÃO DO CENÁRIO) ========================
        
        // 1. Gestor
        User gestor = criarUsuario("Gestor da Copa", "gestor.copa@ifsp.edu.br", PerfilUser.GESTOR_EVENTOS);
        Authentication authGestor = new UsernamePasswordAuthenticationToken(gestor, null, gestor.getAuthorities());

        // 2. Evento e Modalidade
        Evento evento = criarEvento(gestor);
        Modalidade futsal = criarModalidade("Futsal Copa");
        EventoModalidade evtMod = criarEventoModalidade(evento, futsal);

        // 3. Times e Capitães
        User cap1 = criarUsuario("Capitão A", "cap.a@teste.com", PerfilUser.ALUNO);
        Time timeA = criarTime(cap1, futsal, "Time A");

        User cap2 = criarUsuario("Capitão B", "cap.b@teste.com", PerfilUser.ALUNO);
        Time timeB = criarTime(cap2, futsal, "Time B");

        // 4. Inscrições APROVADAS (Necessário para gerar chave)
        criarInscricaoAprovada(timeA, evtMod);
        criarInscricaoAprovada(timeB, evtMod);

        // ======================== ACT 1: GERAR CHAVE ========================
        
        GerarChaveRequestDTO gerarChaveDTO = new GerarChaveRequestDTO();
        gerarChaveDTO.setFormato(FormatoEventoModalidade.MATA_MATA);

        // Chama endpoint para gerar partidas
        mockMvc.perform(post("/events/{id}/gerar-chave", evento.getId())
                .with(authentication(authGestor))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(gerarChaveDTO)))
                .andExpect(status().isNoContent());

        // Validação intermediária: Verifica se a partida foi criada no banco
        List<Partida> partidas = partidaRepository.findAllByEventoModalidade_Evento_Id(evento.getId());
        assertEquals(1, partidas.size(), "Deveria ter gerado 1 partida (2 times em mata-mata)");
        Partida partidaGerada = partidas.get(0);
        assertEquals(StatusPartida.AGENDADA, partidaGerada.getStatusPartida());

        // ======================== ACT 2: LISTAR PARTIDAS ========================
        
        mockMvc.perform(get("/events/{id}/partidas", evento.getId())
                .with(authentication(authGestor))) // Endpoint é público, mas autenticação não atrapalha
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(partidaGerada.getId()))
                .andExpect(jsonPath("$[0].statusPartida").value("AGENDADA"));

        // ======================== ACT 3: ATUALIZAR RESULTADO ========================
        
        PartidaResultadoRequestDTO resultadoDTO = new PartidaResultadoRequestDTO();
        // Time 1 vence (5 > 3)
        resultadoDTO.setTime1Placar(5);
        resultadoDTO.setTime2Placar(3);

        mockMvc.perform(patch("/events/{eventoId}/partidas/{partidaId}/resultado", evento.getId(), partidaGerada.getId())
                .with(authentication(authGestor))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resultadoDTO)))
                .andExpect(status().isNoContent());

        // ======================== ASSERT FINAL ========================
        
        Partida partidaFinalizada = partidaRepository.findById(partidaGerada.getId()).orElseThrow();
        
        assertEquals(StatusPartida.FINALIZADA, partidaFinalizada.getStatusPartida());
        assertEquals(5, partidaFinalizada.getTime1Placar());
        assertEquals(3, partidaFinalizada.getTime2Placar());
        assertNotNull(partidaFinalizada.getVencedor(), "Deveria ter um vencedor definido");
        
        // CORREÇÃO DO ASSERT:
        // Como usamos placar 5x3 (Time1 > Time2), o vencedor deve ser OBRIGATORIAMENTE o time que está na posição Time1.
        // Não importa se o Time1 é o "Time A" ou "Time B" (devido ao shuffle), o que importa é a consistência do resultado.
        assertEquals(partidaFinalizada.getTime1().getId(), partidaFinalizada.getVencedor().getId(), 
            "O vencedor deve ser o Time 1, pois ele fez mais gols");
    }

    // --- Helpers ---

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
        evento.setNome("Copa Teste");
        evento.setDescricao("Desc");
        evento.setDataInicio(LocalDate.now().plusDays(10));
        evento.setDataFim(LocalDate.now().plusDays(20));
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
        em.setMaxTimes(4);
        em.setDataFimInscricao(LocalDate.now().minusDays(1)); 
        return eventoModalidadeRepository.save(em);
    }

    private Time criarTime(User capitao, Modalidade modalidade, String nomeTime) {
        Time t = new Time();
        t.setNome(nomeTime);
        t.setCapitao(capitao);
        t.setModalidade(modalidade);
        t.setMembros(new HashSet<>());
        t.getMembros().add(capitao);
        return timeRepository.save(t);
    }

    private void criarInscricaoAprovada(Time time, EventoModalidade em) {
        Inscricao i = new Inscricao();
        i.setTime(time);
        i.setEventoModalidade(em);
        i.setStatusInscricao(StatusInscricao.APROVADA);
        inscricaoRepository.save(i);
    }
}
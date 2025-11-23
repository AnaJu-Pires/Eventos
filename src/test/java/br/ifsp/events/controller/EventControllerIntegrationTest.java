package br.ifsp.events.controller;

import br.ifsp.events.dto.event.EventRequestDTO;
import br.ifsp.events.dto.event.EventoModalidadeRequestDTO;
import br.ifsp.events.model.Evento;
import br.ifsp.events.model.FormatoEventoModalidade;
import br.ifsp.events.model.Modalidade;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusEvento;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.EventoRepository;
import br.ifsp.events.repository.ModalidadeRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet; // Import Adicionado
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private ModalidadeRepository modalidadeRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("Integração: Deve criar evento com sucesso quando usuário é GESTOR")
    @WithMockUser(username = "gestor.teste@ifsp.edu.br", authorities = {"GESTOR_EVENTOS"})
    void deveCriarEventoComSucesso() throws Exception {
        // --- ARRANGE ---
        User gestor = new User();
        gestor.setNome("Gestor Teste Integração");
        gestor.setEmail("gestor.teste@ifsp.edu.br");
        gestor.setSenha("senha123");
        gestor.setPerfilUser(PerfilUser.GESTOR_EVENTOS);
        gestor.setStatusUser(StatusUser.ATIVO);
        userRepository.save(gestor);

        Modalidade modalidade = new Modalidade();
        modalidade.setNome("Futsal Teste"); 
        modalidade = modalidadeRepository.save(modalidade);

        EventoModalidadeRequestDTO modDTO = new EventoModalidadeRequestDTO();
        modDTO.setModalidadeId(modalidade.getId());
        modDTO.setFormatoEventoModalidade(FormatoEventoModalidade.MATA_MATA);
        modDTO.setDataFimInscricao(LocalDate.now().plusDays(5));
        modDTO.setMaxTimes(8);

        EventRequestDTO eventDTO = new EventRequestDTO();
        eventDTO.setNome("Interclasse 2025");
        eventDTO.setDescricao("Torneio anual");
        eventDTO.setDataInicio(LocalDate.now().plusDays(10));
        eventDTO.setDataFim(LocalDate.now().plusDays(20));
        eventDTO.setModalidades(Set.of(modDTO));

        String jsonBody = objectMapper.writeValueAsString(eventDTO);

        // --- ACT ---
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
        
        // --- ASSERT (HTTP) ---
                .andExpect(status().isCreated());

        // --- ASSERT (Banco) ---
        assertEquals(1, eventoRepository.findAll().stream()
            .filter(e -> e.getNome().equals("Interclasse 2025"))
            .count());
    }

    @Test
    @DisplayName("Integração: Deve proibir criação de evento se usuário for ALUNO (403 Forbidden)")
    @WithMockUser(username = "aluno@ifsp.edu.br", authorities = {"ALUNO"})
    void deveProibirAlunoDeCriarEvento() throws Exception {
        Modalidade modalidade = new Modalidade();
        modalidade.setNome("Volei Teste");
        modalidade = modalidadeRepository.save(modalidade);

        EventoModalidadeRequestDTO modDTO = new EventoModalidadeRequestDTO();
        modDTO.setModalidadeId(modalidade.getId());
        modDTO.setFormatoEventoModalidade(FormatoEventoModalidade.MATA_MATA);
        modDTO.setDataFimInscricao(LocalDate.now().plusDays(5));
        modDTO.setMaxTimes(8);

        EventRequestDTO eventDTO = new EventRequestDTO();
        eventDTO.setNome("Tentativa Hacker");
        eventDTO.setDescricao("Descricao qualquer");
        eventDTO.setDataInicio(LocalDate.now().plusDays(1));
        eventDTO.setDataFim(LocalDate.now().plusDays(2));
        eventDTO.setModalidades(Set.of(modDTO));
        
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Integração: Deve listar eventos cadastrados (GET /events)")
    @WithMockUser(username = "aluno@ifsp.edu.br", authorities = {"ALUNO"})
    void deveListarEventosComSucesso() throws Exception {
        // --- ARRANGE ---
        User organizador = new User();
        organizador.setNome("Organizador GET");
        organizador.setEmail("organizador.get@teste.com");
        organizador.setSenha("123456");
        organizador.setPerfilUser(PerfilUser.ALUNO);
        organizador.setStatusUser(StatusUser.ATIVO);
        userRepository.save(organizador);

        Evento evento = new Evento();
        evento.setNome("Festival de Vôlei");
        evento.setDescricao("Evento de Teste GET");
        evento.setDataInicio(LocalDate.now().plusDays(2));
        evento.setDataFim(LocalDate.now().plusDays(3));
        evento.setStatus(StatusEvento.PLANEJADO);
        evento.setOrganizador(organizador);
        
        // CORREÇÃO: Inicializar a lista para evitar NullPointerException no Service
        evento.setEventoModalidades(new HashSet<>()); 
        
        eventoRepository.save(evento);

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/events")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome == 'Festival de Vôlei')]").exists());
    }
}
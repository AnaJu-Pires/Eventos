package br.ifsp.events.controller;

import br.ifsp.events.dto.time.TimeCreateDTO;
import br.ifsp.events.model.Modalidade;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.Time;
import br.ifsp.events.model.User;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TimeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private ModalidadeRepository modalidadeRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("Integração: Deve criar um time com sucesso (POST /times)")
    // REMOVIDO @WithMockUser pois precisamos de um objeto User real no principal
    void deveCriarTimeComSucesso() throws Exception {
        // --- ARRANGE ---
        // 1. Criar usuário logado (Capitão)
        User capitao = new User();
        capitao.setNome("Capitão Silva");
        capitao.setEmail("capitao.teste@ifsp.edu.br");
        capitao.setSenha("senha123");
        capitao.setPerfilUser(PerfilUser.ALUNO);
        capitao.setStatusUser(StatusUser.ATIVO);
        userRepository.save(capitao); // Salvar para gerar ID

        // 2. Criar Autenticação Manual com o objeto User real
        Authentication auth = new UsernamePasswordAuthenticationToken(capitao, null, capitao.getAuthorities());

        // 3. Criar Modalidade
        Modalidade modalidade = new Modalidade();
        modalidade.setNome("Basquete 3x3");
        modalidade = modalidadeRepository.save(modalidade);

        // 4. DTO de Criação
        TimeCreateDTO timeDTO = new TimeCreateDTO();
        timeDTO.setNome("Os Imbatíveis");
        timeDTO.setModalidadeId(modalidade.getId());

        String jsonBody = objectMapper.writeValueAsString(timeDTO);

        // --- ACT & ASSERT ---
        // Usamos .with(authentication(auth)) para injetar o usuário correto
        mockMvc.perform(post("/times")
                .with(authentication(auth)) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Os Imbatíveis"));
    }

    @Test
    @DisplayName("Integração: Deve listar times cadastrados (GET /times)")
    @WithMockUser(username = "aluno.qualquer@ifsp.edu.br", authorities = {"ALUNO"})
    void deveListarTimes() throws Exception {
        // --- ARRANGE ---
        User capitao = new User();
        capitao.setNome("Capitão Lista");
        capitao.setEmail("lista@ifsp.edu.br");
        capitao.setSenha("123");
        capitao.setPerfilUser(PerfilUser.ALUNO);
        capitao.setStatusUser(StatusUser.ATIVO);
        userRepository.save(capitao);

        Modalidade modalidade = new Modalidade();
        modalidade.setNome("Natação");
        modalidade = modalidadeRepository.save(modalidade);

        Time time1 = new Time();
        time1.setNome("Time Alpha");
        time1.setCapitao(capitao);
        time1.setModalidade(modalidade);
        time1.setMembros(new HashSet<>());
        time1.getMembros().add(capitao); // Adicionando membro para consistência
        timeRepository.save(time1);

        Time time2 = new Time();
        time2.setNome("Time Beta");
        time2.setCapitao(capitao);
        time2.setModalidade(modalidade);
        time2.setMembros(new HashSet<>());
        time2.getMembros().add(capitao);
        timeRepository.save(time2);

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/times")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(2))) // Ajuste para paginação se necessário
                .andExpect(jsonPath("$.content[?(@.nome == 'Time Alpha')]").exists());
    }
    
    @Test
    @DisplayName("Integração: Não deve criar time sem modalidade (400 Bad Request)")
    @WithMockUser(username = "aluno.erro@ifsp.edu.br", authorities = {"ALUNO"})
    void naoDeveCriarTimeSemModalidade() throws Exception {
        // --- ARRANGE ---
        TimeCreateDTO timeDTO = new TimeCreateDTO();
        timeDTO.setNome("Time Sem Modalidade");
        timeDTO.setModalidadeId(null); // Erro intencional

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/times")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(timeDTO)))
                .andExpect(status().isBadRequest());
    }
}
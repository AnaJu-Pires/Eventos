package br.ifsp.events.controller;

import br.ifsp.events.dto.comunidade.ComunidadeCreateDTO;
import br.ifsp.events.model.Comunidade;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.ComunidadeRepository;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.EmailService;
import br.ifsp.events.service.GamificationService; // Importante
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ComunidadeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComunidadeRepository comunidadeRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private GamificationService gamificationService;

    @Test
    @DisplayName("Integração: Deve criar comunidade com sucesso")
    void deveCriarComunidadeComSucesso() throws Exception {

        User user = new User();
        user.setNome("Midas");
        user.setEmail("midas@gold.com");
        user.setSenha("123");
        user.setPerfilUser(PerfilUser.ALUNO);
        user.setStatusUser(StatusUser.ATIVO);
        userRepository.save(user);

        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        ComunidadeCreateDTO dto = new ComunidadeCreateDTO();
        dto.setNome("Comunidade VIP");
        dto.setDescricao("Só para quem pode.");

        mockMvc.perform(post("/comunidades")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Comunidade VIP"))
                .andExpect(jsonPath("$.criadorNome").value("Midas"));
    }

    @Test
    @DisplayName("Integração: Deve listar todas as comunidades (GET /comunidades)")
    void deveListarComunidades() throws Exception {
        User criador = new User();
        criador.setNome("Criador");
        criador.setEmail("criador@teste.com");
        criador.setSenha("123");
        criador.setPerfilUser(PerfilUser.ALUNO);
        criador.setStatusUser(StatusUser.ATIVO);
        userRepository.save(criador);

        Comunidade c1 = new Comunidade();
        c1.setNome("Comunidade A");
        c1.setDescricao("Desc A");
        c1.setCriador(criador);
        comunidadeRepository.save(c1);

        Comunidade c2 = new Comunidade();
        c2.setNome("Comunidade B");
        c2.setDescricao("Desc B");
        c2.setCriador(criador);
        comunidadeRepository.save(c2);

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/comunidades")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(org.hamcrest.Matchers.greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[?(@.nome == 'Comunidade A')]").exists());
    }

    @Test
    @DisplayName("Integração: Admin deve deletar comunidade (204 No Content)")
    void adminDeveDeletarComunidade() throws Exception {
        // --- ARRANGE ---
        User criador = new User();
        criador.setNome("User Comum");
        criador.setEmail("user@delete.com");
        criador.setSenha("123");
        criador.setPerfilUser(PerfilUser.ALUNO);
        criador.setStatusUser(StatusUser.ATIVO);
        userRepository.save(criador);

        Comunidade comunidade = new Comunidade();
        comunidade.setNome("Para Deletar");
        comunidade.setDescricao("Vai sumir");
        comunidade.setCriador(criador);
        comunidadeRepository.save(comunidade);

        User admin = new User();
        admin.setNome("Admin");
        admin.setEmail("admin@delete.com");
        admin.setSenha("123");
        admin.setPerfilUser(PerfilUser.ADMIN);
        admin.setStatusUser(StatusUser.ATIVO);
        userRepository.save(admin);

        Authentication authAdmin = new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());

        mockMvc.perform(delete("/comunidades/{id}", comunidade.getId())
                .with(authentication(authAdmin)))
                .andExpect(status().isNoContent());
        
        mockMvc.perform(get("/comunidades/{id}", comunidade.getId()))
                .andExpect(status().isNotFound());
    }
}
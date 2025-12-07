package br.ifsp.events.controller;

import br.ifsp.events.dto.user.UserInteresseUpdateDTO;
import br.ifsp.events.dto.user.UserRoleUpdateDTO;
import br.ifsp.events.model.Modalidade;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.User;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModalidadeRepository modalidadeRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("Integração: Deve retornar dados do usuário logado (GET /users/me)")
    void deveRetornarDadosDoUsuarioLogado() throws Exception {
        // --- ARRANGE ---
        User user = criarUsuario("Eu Mesmo", "eu.teste@ifsp.edu.br", PerfilUser.ALUNO);
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/users/me")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("eu.teste@ifsp.edu.br"))
                .andExpect(jsonPath("$.nome").value("Eu Mesmo"));
    }

    @Test
    @DisplayName("Integração: Admin deve conseguir alterar perfil de usuário (PATCH /users/{id}/role)")
    void adminDeveAlterarPerfilDeUsuario() throws Exception {
        // --- ARRANGE ---
        // Admin com e-mail válido para o cargo
        User admin = criarUsuario("Admin Teste", "admin.teste@ifsp.edu.br", PerfilUser.ADMIN);
        // Vítima com e-mail válido para virar Gestor
        User vitima = criarUsuario("Funcionario Teste", "func.teste@ifsp.edu.br", PerfilUser.FUNCIONARIO);

        Authentication authAdmin = new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());

        UserRoleUpdateDTO roleDTO = new UserRoleUpdateDTO();
        roleDTO.setPerfilUser(PerfilUser.GESTOR_EVENTOS); 

        // --- ACT & ASSERT ---
        mockMvc.perform(patch("/users/{id}/role", vitima.getId())
                .with(authentication(authAdmin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfilUser").value("GESTOR_EVENTOS"));
    }

    @Test
    @DisplayName("Integração: Aluno NÃO deve conseguir alterar perfil de usuário (403 Forbidden)")
    void alunoNaoDeveAlterarPerfil() throws Exception {
        // --- ARRANGE ---
        User atacante = criarUsuario("Hacker User", "hacker@aluno.ifsp.edu.br", PerfilUser.ALUNO);
        User alvo = criarUsuario("Alvo", "alvo@aluno.ifsp.edu.br", PerfilUser.ALUNO);

        Authentication authAluno = new UsernamePasswordAuthenticationToken(atacante, null, atacante.getAuthorities());

        UserRoleUpdateDTO roleDTO = new UserRoleUpdateDTO();
        roleDTO.setPerfilUser(PerfilUser.ADMIN); 

        // --- ACT & ASSERT ---
        mockMvc.perform(patch("/users/{id}/role", alvo.getId())
                .with(authentication(authAluno))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isForbidden()); 
    }

    @Test
    @DisplayName("Integração: Usuário deve atualizar seus interesses (PATCH /users/me/interesses)")
    void deveAtualizarInteresses() throws Exception {
        // --- ARRANGE ---
        User user = criarUsuario("Interessado", "interest@aluno.ifsp.edu.br", PerfilUser.ALUNO);
        Modalidade futsal = criarModalidade("Futsal Teste Interesses");
        Modalidade volei = criarModalidade("Vôlei Teste Interesses");

        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        UserInteresseUpdateDTO dto = new UserInteresseUpdateDTO();
        dto.setModalidadeIds(List.of(futsal.getId(), volei.getId()));

        // --- ACT & ASSERT ---
        mockMvc.perform(patch("/users/me/interesses")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                // CORREÇÃO AQUI: O campo no JSON é 'interesses' e não 'modalidadesInteresse'
                .andExpect(jsonPath("$.interesses").isArray())
                .andExpect(jsonPath("$.interesses.length()").value(2));
    }

    @Test
    @DisplayName("Integração: Admin deve exportar CSV de perfis (200 OK + Content-Type)")
    void adminDeveBaixarCSV() throws Exception {
        // --- ARRANGE ---
        User admin = criarUsuario("Admin CSV", "admin.csv@ifsp.edu.br", PerfilUser.ADMIN);
        criarUsuario("User CSV 1", "u1.csv@aluno.ifsp.edu.br", PerfilUser.ALUNO);
        
        Authentication authAdmin = new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/users/perfis/csv")
                .with(authentication(authAdmin)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("User CSV 1")));
    }

    // --- Métodos Auxiliares ---

    private User criarUsuario(String nome, String email, PerfilUser perfil) {
        User user = new User();
        user.setNome(nome);
        user.setEmail(email);
        user.setSenha("123");
        user.setPerfilUser(perfil);
        user.setStatusUser(StatusUser.ATIVO);
        return userRepository.save(user);
    }

    private Modalidade criarModalidade(String nome) {
        Modalidade m = new Modalidade();
        m.setNome(nome);
        return modalidadeRepository.save(m);
    }
}
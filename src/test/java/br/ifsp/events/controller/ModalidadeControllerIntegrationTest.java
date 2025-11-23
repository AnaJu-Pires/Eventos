package br.ifsp.events.controller;

import br.ifsp.events.dto.modalidade.ModalidadeRequestDTO;
import br.ifsp.events.dto.modalidade.ModalidadePatchRequestDTO;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ModalidadeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModalidadeRepository modalidadeRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("Integração: Admin deve criar modalidade com sucesso (201 Created)")
    void adminDeveCriarModalidade() throws Exception {
        // --- ARRANGE ---
        User admin = criarUsuario("Admin Mod", "admin.mod@ifsp.edu.br", PerfilUser.ADMIN);
        Authentication auth = new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());

        ModalidadeRequestDTO dto = new ModalidadeRequestDTO();
        dto.setNome("Basquete 3x3 Teste");
        dto.setDescricao("Nova modalidade olímpica");

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/modalidades")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Basquete 3x3 Teste"));
    }

    @Test
    @DisplayName("Integração: Gestor deve listar modalidades (200 OK)")
    void gestorDeveListarModalidades() throws Exception {
        // --- ARRANGE ---
        User gestor = criarUsuario("Gestor Mod", "gestor.mod@ifsp.edu.br", PerfilUser.GESTOR_EVENTOS);
        Authentication auth = new UsernamePasswordAuthenticationToken(gestor, null, gestor.getAuthorities());

        Modalidade m1 = new Modalidade();
        m1.setNome("Natação Teste");
        m1.setDescricao("Piscina curta");
        modalidadeRepository.save(m1);

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/modalidades")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Verifica se existe pelo menos uma modalidade com esse nome na lista
                .andExpect(jsonPath("$[?(@.nome == 'Natação Teste')]").exists());
    }

    @Test
    @DisplayName("Integração: Aluno NÃO pode criar modalidade (403 Forbidden)")
    void alunoNaoDeveCriarModalidade() throws Exception {
        // --- ARRANGE ---
        User aluno = criarUsuario("Aluno Mod", "aluno.mod@ifsp.edu.br", PerfilUser.ALUNO);
        Authentication auth = new UsernamePasswordAuthenticationToken(aluno, null, aluno.getAuthorities());

        ModalidadeRequestDTO dto = new ModalidadeRequestDTO();
        dto.setNome("E-Sports");
        dto.setDescricao("Jogos eletrônicos");

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/modalidades")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Integração: Admin deve atualizar modalidade (200 OK)")
    void adminDeveAtualizarModalidade() throws Exception {
        // --- ARRANGE ---
        User admin = criarUsuario("Admin Update", "admin.up@ifsp.edu.br", PerfilUser.ADMIN);
        Authentication auth = new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());

        Modalidade modalidade = new Modalidade();
        modalidade.setNome("Handebol Antigo");
        modalidade = modalidadeRepository.save(modalidade);

        ModalidadeRequestDTO updateDTO = new ModalidadeRequestDTO();
        updateDTO.setNome("Handebol Novo");
        updateDTO.setDescricao("Regras atualizadas");

        // --- ACT & ASSERT ---
        mockMvc.perform(put("/modalidades/{id}", modalidade.getId())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Handebol Novo"));
    }
    
    @Test
    @DisplayName("Integração: Admin deve deletar modalidade (204 No Content)")
    void adminDeveDeletarModalidade() throws Exception {
        // --- ARRANGE ---
        User admin = criarUsuario("Admin Del", "admin.del@ifsp.edu.br", PerfilUser.ADMIN);
        Authentication auth = new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());

        Modalidade modalidade = new Modalidade();
        modalidade.setNome("Modalidade Para Deletar");
        modalidade = modalidadeRepository.save(modalidade);

        // --- ACT & ASSERT ---
        mockMvc.perform(delete("/modalidades/{id}", modalidade.getId())
                .with(authentication(auth)))
                .andExpect(status().isNoContent());
                
        // Confirma que sumiu (GET deve retornar 404)
        mockMvc.perform(get("/modalidades/{id}", modalidade.getId())
                 .with(authentication(auth)))
                 .andExpect(status().isNotFound());
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
}
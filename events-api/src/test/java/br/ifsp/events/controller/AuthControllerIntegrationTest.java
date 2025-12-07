package br.ifsp.events.controller;

import br.ifsp.events.dto.user.UserLoginDTO;
import br.ifsp.events.dto.user.UserRegisterDTO;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test") // Usa o application-test.properties (banco H2)
@Transactional // Limpa o banco após cada teste
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Simula o cliente HTTP (o "Postman")

    @Autowired
    private ObjectMapper objectMapper; // Converte objetos Java para JSON

    @Autowired
    private UserRepository userRepository; // Acesso real ao banco de teste

    @Autowired
    private PasswordEncoder passwordEncoder; // Para salvar senhas criptografadas no setup

    @MockBean
    private EmailService emailService; // Mockamos o email para não depender de SMTP externo

    @Test
    @DisplayName("Integração: Deve registrar usuário com sucesso e persistir no banco")
    void deveRegistrarUsuarioEndToEnd() throws Exception {
        // --- ARRANGE (Preparação) ---
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setNome("Usuario Integracao");
        registerDTO.setEmail("integracao@aluno.ifsp.edu.br");
        registerDTO.setSenha("SenhaForte123!");

        String jsonBody = objectMapper.writeValueAsString(registerDTO);

        // --- ACT (Ação) ---
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                
        // --- ASSERT (Verificação HTTP) ---
                .andExpect(status().isOk()); // Espera 200 OK

        // --- ASSERT (Verificação Banco de Dados) ---
        boolean usuarioFoiSalvo = userRepository.existsByEmail("integracao@aluno.ifsp.edu.br");
        assertTrue(usuarioFoiSalvo, "ERRO: O usuário deveria ter sido salvo no banco de dados H2.");
    }

    @Test
    @DisplayName("Integração: Deve realizar login e retornar Token JWT")
    void deveRealizarLoginComSucesso() throws Exception {
        // --- ARRANGE ---
        // 1. Criar e salvar um usuário manualmente no banco H2
        User user = new User();
        user.setNome("Login User");
        user.setEmail("login@aluno.ifsp.edu.br");
        user.setSenha(passwordEncoder.encode("123456")); // A senha DEVE estar criptografada no banco
        user.setPerfilUser(PerfilUser.ALUNO);
        user.setStatusUser(StatusUser.ATIVO); // O usuário deve estar ATIVO para logar
        userRepository.save(user);

        // 2. Criar o DTO de login
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setEmail("login@aluno.ifsp.edu.br");
        loginDTO.setSenha("123456"); // A senha enviada é a "crua"

        String jsonBody = objectMapper.writeValueAsString(loginDTO);

        // --- ACT ---
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))

        // --- ASSERT ---
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(jsonPath("$.token").isNotEmpty()); // Verifica apenas o token
    }
}
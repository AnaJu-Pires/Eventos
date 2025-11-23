package br.ifsp.events.controller;

import br.ifsp.events.dto.comunidade.ComunidadeCreateDTO;
import br.ifsp.events.model.Comunidade;
import br.ifsp.events.model.NivelEngajamento;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.ComunidadeRepository;
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

    @Test
    @DisplayName("Integração: Deve criar comunidade com sucesso (Nível GOLD + Pontos suficientes)")
    void deveCriarComunidadeComSucesso() throws Exception {
        // --- ARRANGE ---
        // Usuário que cumpre os requisitos: GOLD e > 1500 pontos
        User userGold = new User();
        userGold.setNome("Midas");
        userGold.setEmail("midas@gold.com");
        userGold.setSenha("123");
        userGold.setPerfilUser(PerfilUser.ALUNO);
        userGold.setStatusUser(StatusUser.ATIVO);
        userGold.setNivel(NivelEngajamento.GOLD);
        userGold.setPontosSaldo(2000L); // Saldo suficiente
        userRepository.save(userGold);

        Authentication auth = new UsernamePasswordAuthenticationToken(userGold, null, userGold.getAuthorities());

        ComunidadeCreateDTO dto = new ComunidadeCreateDTO();
        dto.setNome("Comunidade VIP");
        dto.setDescricao("Só para quem pode.");

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/comunidades")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Comunidade VIP"))
                .andExpect(jsonPath("$.criadorNome").value("Midas"));
    }

    @Test
    @DisplayName("Integração: Não deve criar comunidade se usuário for BRONZE (mesmo com pontos)")
    void deveFalharCriacaoNivelBaixo() throws Exception {
        // --- ARRANGE ---
        User userBronze = new User();
        userBronze.setNome("Novato Rico");
        userBronze.setEmail("novato@teste.com");
        userBronze.setSenha("123");
        userBronze.setPerfilUser(PerfilUser.ALUNO);
        userBronze.setStatusUser(StatusUser.ATIVO);
        userBronze.setNivel(NivelEngajamento.BRONZE); // Nível insuficiente
        userBronze.setPontosSaldo(5000L); // Tem pontos, mas não tem nível
        userRepository.save(userBronze);

        Authentication auth = new UsernamePasswordAuthenticationToken(userBronze, null, userBronze.getAuthorities());

        ComunidadeCreateDTO dto = new ComunidadeCreateDTO();
        dto.setNome("Comunidade Fail");
        dto.setDescricao("Tentativa");

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/comunidades")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // Espera erro de Regra de Negócio (400)
                // Nota: Se sua lógica lançar AccessDeniedException, pode ser 403.
                // Mas BusinessRuleException geralmente é 400.
    }

    @Test
    @DisplayName("Integração: Não deve criar comunidade se usuário GOLD não tiver pontos")
    void deveFalharCriacaoSemPontos() throws Exception {
        // --- ARRANGE ---
        User userGoldPobre = new User();
        userGoldPobre.setNome("Midas Falido");
        userGoldPobre.setEmail("falido@teste.com");
        userGoldPobre.setSenha("123");
        userGoldPobre.setPerfilUser(PerfilUser.ALUNO);
        userGoldPobre.setStatusUser(StatusUser.ATIVO);
        userGoldPobre.setNivel(NivelEngajamento.GOLD); // Nível OK
        userGoldPobre.setPontosSaldo(100L); // Pontos insuficientes (precisa de 1500)
        userRepository.save(userGoldPobre);

        Authentication auth = new UsernamePasswordAuthenticationToken(userGoldPobre, null, userGoldPobre.getAuthorities());

        ComunidadeCreateDTO dto = new ComunidadeCreateDTO();
        dto.setNome("Comunidade Sem Verba");
        dto.setDescricao("Não vai rolar");

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/comunidades")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // Espera erro por falta de saldo
    }

    @Test
    @DisplayName("Integração: Deve listar todas as comunidades (GET /comunidades)")
    void deveListarComunidades() throws Exception {
        // --- ARRANGE ---
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
                // Verifica se o array retornado tem tamanho >= 2
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

        // --- ACT & ASSERT ---
        mockMvc.perform(delete("/comunidades/{id}", comunidade.getId())
                .with(authentication(authAdmin)))
                .andExpect(status().isNoContent());
        
        // Verifica se sumiu do banco
        mockMvc.perform(get("/comunidades/{id}", comunidade.getId()))
                .andExpect(status().isNotFound());
    }
}
package br.ifsp.events.controller;

import br.ifsp.events.model.Convite;
import br.ifsp.events.model.Modalidade;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusConvite;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.Time;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.ConviteRepository;
import br.ifsp.events.repository.ModalidadeRepository;
import br.ifsp.events.repository.TimeRepository;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.EmailService;
import br.ifsp.events.service.NotificationService;
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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.hamcrest.Matchers.isOneOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ConviteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private ModalidadeRepository modalidadeRepository;

    @Autowired
    private ConviteRepository conviteRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("Integração: Deve listar convites pendentes do usuário (GET /convites/meus-convites)")
    void deveListarMeusConvites() throws Exception {
        // --- ARRANGE ---
        User convidado = criarUsuario("Convidado", "convidado.lista@ifsp.edu.br");
        User capitao = criarUsuario("Capitão", "capitao.lista@ifsp.edu.br");
        
        Time time = criarTime(capitao, "Futsal Convite Lista"); 
        
        criarConvite(time, convidado, StatusConvite.PENDENTE);

        // Autenticação manual com o objeto User real
        Authentication auth = new UsernamePasswordAuthenticationToken(convidado, null, convidado.getAuthorities());

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/convites/meus-convites")
                .with(authentication(auth)) 
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // CORREÇÃO: O JSON retorna um objeto 'time' aninhado, então o caminho é time.nome
                .andExpect(jsonPath("$[0].time.nome").value("Time Futsal Convite Lista")) 
                .andExpect(jsonPath("$[0].status").value("PENDENTE"));
    }

    @Test
    @DisplayName("Integração: Deve aceitar convite e entrar no time (POST /convites/{id}/aceitar)")
    void deveAceitarConvite() throws Exception {
        // --- ARRANGE ---
        User aceitante = criarUsuario("Aceitante", "aceitante.teste@ifsp.edu.br");
        User capitao = criarUsuario("Capitão 2", "capitao2.teste@ifsp.edu.br");
        
        Time time = criarTime(capitao, "Vôlei Convite Aceitar");

        Convite convite = criarConvite(time, aceitante, StatusConvite.PENDENTE);

        Authentication auth = new UsernamePasswordAuthenticationToken(aceitante, null, aceitante.getAuthorities());

        // --- ACT ---
        mockMvc.perform(post("/convites/{id}/aceitar", convite.getId())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // --- ASSERT ---
        Optional<Convite> conviteAtualizado = conviteRepository.findById(convite.getId());
        // A lógica pode deletar ou alterar status. Se não deletou, verifica status.
        if (conviteAtualizado.isPresent()) {
             assertEquals(StatusConvite.ACEITO, conviteAtualizado.get().getStatus());
        }

        // Verifica se o usuário entrou no time
        Time timeAtualizado = timeRepository.findById(time.getId()).get();
        boolean isMembro = timeAtualizado.getMembros().stream()
                .anyMatch(u -> u.getId().equals(aceitante.getId()));
        assertTrue(isMembro, "O usuário deveria fazer parte do time após aceitar o convite");
    }

    @Test
    @DisplayName("Integração: Deve recusar convite (POST /convites/{id}/recusar)")
    void deveRecusarConvite() throws Exception {
        // --- ARRANGE ---
        User recusante = criarUsuario("Recusante", "recusante.teste@ifsp.edu.br");
        User capitao = criarUsuario("Capitão 3", "capitao3.teste@ifsp.edu.br");
        
        Time time = criarTime(capitao, "Basquete Convite Recusar");

        Convite convite = criarConvite(time, recusante, StatusConvite.PENDENTE);

        Authentication auth = new UsernamePasswordAuthenticationToken(recusante, null, recusante.getAuthorities());

        // --- ACT ---
        mockMvc.perform(post("/convites/{id}/recusar", convite.getId())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()); 

        // --- ASSERT ---
        Optional<Convite> conviteAtualizado = conviteRepository.findById(convite.getId());
        // Se a regra for deletar o convite:
        assertFalse(conviteAtualizado.isPresent(), "O convite deveria ter sido excluído após recusa");
    }
    
    @Test
    @DisplayName("Integração: Não deve permitir aceitar convite de outro usuário (404 Not Found ou 403)")
    void naoDeveAceitarConviteAlheio() throws Exception {
        // --- ARRANGE ---
        User donoConvite = criarUsuario("Dono", "dono.teste@ifsp.edu.br");
        User hacker = criarUsuario("Hacker", "hacker.teste@ifsp.edu.br");
        
        User capitao = criarUsuario("Capitão 4", "capitao4.teste@ifsp.edu.br");
        
        Time time = criarTime(capitao, "Handebol Convite Hacker");

        Convite convite = criarConvite(time, donoConvite, StatusConvite.PENDENTE);

        Authentication auth = new UsernamePasswordAuthenticationToken(hacker, null, hacker.getAuthorities());

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/convites/{id}/aceitar", convite.getId())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(isOneOf(404, 403))); 
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

    private Time criarTime(User capitao, String modalidadeNome) {
        Modalidade mod = new Modalidade();
        mod.setNome(modalidadeNome);
        modalidadeRepository.save(mod);

        Time t = new Time();
        t.setNome("Time " + modalidadeNome);
        t.setCapitao(capitao);
        t.setModalidade(mod);
        t.setMembros(new HashSet<>());
        t.getMembros().add(capitao);
        return timeRepository.save(t);
    }

    private Convite criarConvite(Time time, User convidado, StatusConvite status) {
        Convite c = new Convite();
        c.setTime(time);
        c.setUsuarioConvidado(convidado);
        c.setStatus(status);
        c.setDataExpiracao(LocalDateTime.now().plusDays(2)); 
        return conviteRepository.save(c);
    }
}
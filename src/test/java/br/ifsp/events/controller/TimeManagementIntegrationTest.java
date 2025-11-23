package br.ifsp.events.controller;

import br.ifsp.events.dto.convite.ConviteCreateDTO;
import br.ifsp.events.dto.time.CapitaoTransferDTO;
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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TimeManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("Integração: Capitão deve enviar convite com sucesso")
    void deveEnviarConvite() throws Exception {
        // --- ARRANGE ---
        User capitao = criarUsuario("Capitão Invite", "cap.invite@ifsp.edu.br");
        User convidado = criarUsuario("Convidado", "guest@ifsp.edu.br");
        Time time = criarTime(capitao, "Time Invite");

        Authentication auth = new UsernamePasswordAuthenticationToken(capitao, null, capitao.getAuthorities());
        ConviteCreateDTO dto = new ConviteCreateDTO(convidado.getEmail());

        // --- ACT ---
        mockMvc.perform(post("/times/{id}/convites", time.getId())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // --- ASSERT ---
        boolean conviteExiste = conviteRepository.existsByTimeAndUsuarioConvidadoAndStatus(
                time, convidado, StatusConvite.PENDENTE);
        assertTrue(conviteExiste, "O convite deveria ter sido salvo no banco como PENDENTE.");
    }

    @Test
    @DisplayName("Integração: Capitão deve remover membro do time")
    void deveRemoverMembro() throws Exception {
        // --- ARRANGE ---
        User capitao = criarUsuario("Capitão Kick", "cap.kick@ifsp.edu.br");
        User membro = criarUsuario("Membro Alvo", "alvo@ifsp.edu.br");
        
        Time time = criarTime(capitao, "Time Kick");
        // Adiciona membro manualmente ao time
        time.getMembros().add(membro);
        timeRepository.save(time);

        Authentication auth = new UsernamePasswordAuthenticationToken(capitao, null, capitao.getAuthorities());

        // --- ACT ---
        mockMvc.perform(delete("/times/{id}/membros/{userId}", time.getId(), membro.getId())
                .with(authentication(auth)))
                .andExpect(status().isNoContent());

        // --- ASSERT ---
        Time timeAtualizado = timeRepository.findById(time.getId()).orElseThrow();
        assertFalse(timeAtualizado.getMembros().contains(membro), "O membro deveria ter sido removido.");
        assertTrue(timeAtualizado.getMembros().contains(capitao), "O capitão deve permanecer no time.");
    }

    @Test
    @DisplayName("Integração: Capitão deve transferir liderança para outro membro")
    void deveTransferirCapitania() throws Exception {
        // --- ARRANGE ---
        User capitaoAtual = criarUsuario("Capitão Atual", "old.cap@ifsp.edu.br");
        User novoCapitao = criarUsuario("Novo Capitão", "new.cap@ifsp.edu.br");
        
        Time time = criarTime(capitaoAtual, "Time Transfer");
        // O novo capitão PRECISA ser membro antes de assumir
        time.getMembros().add(novoCapitao);
        timeRepository.save(time);

        Authentication auth = new UsernamePasswordAuthenticationToken(capitaoAtual, null, capitaoAtual.getAuthorities());
        CapitaoTransferDTO dto = new CapitaoTransferDTO(novoCapitao.getId());

        // --- ACT ---
        mockMvc.perform(patch("/times/{id}/capitao", time.getId())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capitao.id").value(novoCapitao.getId()));

        // --- ASSERT ---
        Time timeAtualizado = timeRepository.findById(time.getId()).orElseThrow();
        assertEquals(novoCapitao.getId(), timeAtualizado.getCapitao().getId(), "O capitão do time deve ter mudado.");
    }

    @Test
    @DisplayName("Integração: Não deve permitir que membro comum remova outros membros")
    void membroNaoDeveExpulsarOutros() throws Exception {
        // --- ARRANGE ---
        User capitao = criarUsuario("Capitão Real", "real@ifsp.edu.br");
        User membroComum = criarUsuario("Membro Comum", "comum@ifsp.edu.br");
        User vitima = criarUsuario("Vítima", "vitima@ifsp.edu.br");

        Time time = criarTime(capitao, "Time Hierarquia");
        time.getMembros().add(membroComum);
        time.getMembros().add(vitima);
        timeRepository.save(time);

        // Autentica como Membro Comum
        Authentication auth = new UsernamePasswordAuthenticationToken(membroComum, null, membroComum.getAuthorities());

        // --- ACT & ASSERT ---
        mockMvc.perform(delete("/times/{id}/membros/{userId}", time.getId(), vitima.getId())
                .with(authentication(auth)))
                .andExpect(status().isBadRequest()); // Regra de negócio: Apenas capitão pode remover (BusinessRuleException)
                // Nota: Se o sistema lançasse AccessDenied, seria 403. O seu TimeServiceImpl lança BusinessRuleException (400).
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

    private Time criarTime(User capitao, String nome) {
        Modalidade m = new Modalidade();
        m.setNome("Mod " + nome);
        modalidadeRepository.save(m);

        Time t = new Time();
        t.setNome(nome);
        t.setCapitao(capitao);
        t.setModalidade(m);
        t.setMembros(new HashSet<>());
        t.getMembros().add(capitao);
        return timeRepository.save(t);
    }
}
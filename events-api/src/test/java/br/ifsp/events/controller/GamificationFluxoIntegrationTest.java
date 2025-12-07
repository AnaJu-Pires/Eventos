package br.ifsp.events.controller;

import br.ifsp.events.dto.post.PostCreateDTO;
import br.ifsp.events.model.Comunidade;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.TipoAcaoGamificacao;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.ComunidadeRepository;
import br.ifsp.events.repository.PostRepository;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.EmailService;
import br.ifsp.events.service.GamificationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GamificationFluxoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComunidadeRepository comunidadeRepository;

    @Autowired
    private PostRepository postRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private GamificationService gamificationService;

    @Test
    @DisplayName("Integração: Criar Post deve chamar o serviço de gamificação")
    void deveAcionarGamificacaoAoCriarPost() throws Exception {
        User autor = criarUsuario("Autor Rico", "rico@test.com");
        Comunidade comunidade = criarComunidade("Comunidade Teste", autor);
        Authentication auth = new UsernamePasswordAuthenticationToken(autor, null, autor.getAuthorities());
        PostCreateDTO postDTO = new PostCreateDTO("Titulo", "Conteudo");

        mockMvc.perform(post("/comunidades/{id}/posts", comunidade.getId())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDTO)))
                .andExpect(status().isCreated());

        verify(gamificationService).registrarAcao(any(User.class), eq(TipoAcaoGamificacao.CRIAR_POST));
    }

    private User criarUsuario(String nome, String email) {
        User user = new User();
        user.setNome(nome);
        user.setEmail(email);
        user.setSenha("123");
        user.setPerfilUser(PerfilUser.ALUNO);
        user.setStatusUser(StatusUser.ATIVO);
        return userRepository.save(user);
    }

    private Comunidade criarComunidade(String nome, User criador) {
        Comunidade c = new Comunidade();
        c.setNome(nome);
        c.setDescricao("Descrição teste");
        c.setCriador(criador);
        return comunidadeRepository.save(c);
    }
}
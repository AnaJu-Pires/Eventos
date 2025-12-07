package br.ifsp.events.controller;

import br.ifsp.events.dto.voto.VotoCreateDTO;
import br.ifsp.events.model.Comentario;
import br.ifsp.events.model.Comunidade;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.Post;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.TipoVoto;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.ComentarioRepository;
import br.ifsp.events.repository.ComunidadeRepository;
import br.ifsp.events.repository.PostRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class VotoControllerIntegrationTest {

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

    @Autowired
    private ComentarioRepository comentarioRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("Integração: Deve registrar voto em POST com sucesso (UPVOTE)")
    void deveVotarEmPost() throws Exception {
        // --- ARRANGE ---
        User autor = criarUsuario("Autor", "autor@voto.com");
        User eleitor = criarUsuario("Eleitor", "eleitor@voto.com");
        Comunidade comunidade = criarComunidade("Comunidade Votos", autor);
        Post post = criarPost("Post Legal", autor, comunidade);

        Authentication auth = new UsernamePasswordAuthenticationToken(eleitor, null, eleitor.getAuthorities());

        VotoCreateDTO dto = new VotoCreateDTO();
        dto.setPostId(post.getId());
        dto.setTipoVoto(TipoVoto.UPVOTE);

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/votos")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                // CORREÇÃO: Usando 'novoPlacar' conforme seu DTO
                .andExpect(jsonPath("$.novoPlacar").exists()) 
                // CORREÇÃO: Usando 'seuVoto' conforme seu DTO
                .andExpect(jsonPath("$.seuVoto").value("UPVOTE"));
    }

    @Test
    @DisplayName("Integração: Deve registrar voto em COMENTÁRIO com sucesso (DOWNVOTE)")
    void deveVotarEmComentario() throws Exception {
        // --- ARRANGE ---
        User autor = criarUsuario("Autor Coment", "coment@voto.com");
        User hater = criarUsuario("Hater", "hater@voto.com");
        Comunidade comunidade = criarComunidade("Debates", autor);
        Post post = criarPost("Polêmica", autor, comunidade);
        Comentario comentario = criarComentario("Concordo", autor, post);

        Authentication auth = new UsernamePasswordAuthenticationToken(hater, null, hater.getAuthorities());

        VotoCreateDTO dto = new VotoCreateDTO();
        dto.setComentarioId(comentario.getId());
        dto.setTipoVoto(TipoVoto.DOWNVOTE);

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/votos")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                // CORREÇÃO: Usando 'seuVoto'
                .andExpect(jsonPath("$.seuVoto").value("DOWNVOTE"));
    }

    @Test
    @DisplayName("Integração: Não deve votar sem informar alvo (400 Bad Request)")
    void deveFalharSemAlvo() throws Exception {
        // --- ARRANGE ---
        User eleitor = criarUsuario("Eleitor Perdido", "perdido@voto.com");
        Authentication auth = new UsernamePasswordAuthenticationToken(eleitor, null, eleitor.getAuthorities());

        VotoCreateDTO dto = new VotoCreateDTO();
        dto.setTipoVoto(TipoVoto.UPVOTE);
        // postId e comentarioId são NULL

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/votos")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Integração: Não deve votar em Post inexistente (404 Not Found)")
    void deveFalharPostInexistente() throws Exception {
        // --- ARRANGE ---
        User eleitor = criarUsuario("Eleitor 404", "notfound@voto.com");
        Authentication auth = new UsernamePasswordAuthenticationToken(eleitor, null, eleitor.getAuthorities());

        VotoCreateDTO dto = new VotoCreateDTO();
        dto.setPostId(9999L); // ID que não existe
        dto.setTipoVoto(TipoVoto.UPVOTE);

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/votos")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
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

    private Comunidade criarComunidade(String nome, User criador) {
        Comunidade c = new Comunidade();
        c.setNome(nome);
        c.setDescricao("Desc");
        c.setCriador(criador);
        return comunidadeRepository.save(c);
    }

    private Post criarPost(String titulo, User autor, Comunidade comunidade) {
        Post p = new Post();
        p.setTitulo(titulo);
        p.setConteudo("Conteúdo");
        p.setAutor(autor);
        p.setComunidade(comunidade);
        p.setVotos(0);
        return postRepository.save(p);
    }

    private Comentario criarComentario(String conteudo, User autor, Post post) {
        Comentario c = new Comentario();
        c.setConteudo(conteudo);
        c.setAutor(autor);
        c.setPost(post);
        c.setVotos(0);
        return comentarioRepository.save(c);
    }
}
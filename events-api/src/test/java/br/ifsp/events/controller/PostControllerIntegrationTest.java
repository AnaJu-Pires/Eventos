package br.ifsp.events.controller;

import br.ifsp.events.dto.comentario.ComentarioCreateDTO;
import br.ifsp.events.model.Comentario;
import br.ifsp.events.model.Comunidade;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.Post;
import br.ifsp.events.model.StatusUser;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
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
class PostControllerIntegrationTest {

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
    @DisplayName("Integração: Deve buscar post por ID com sucesso (200 OK)")
    void deveBuscarPostPorId() throws Exception {
        // --- ARRANGE ---
        User autor = criarUsuario("Autor Post", "autor@post.com", PerfilUser.ALUNO);
        Comunidade comunidade = criarComunidade("Comunidade Teste", autor);
        Post post = criarPost("Título Teste", "Conteúdo bacana", autor, comunidade);

        // --- ACT & ASSERT ---
        // O endpoint GET /posts/{id} é público (permitAll), não precisa de autenticação
        mockMvc.perform(get("/posts/{id}", post.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Título Teste"))
                .andExpect(jsonPath("$.autorNome").value("Autor Post"));
    }

    @Test
    @DisplayName("Integração: Deve criar comentário em post existente (201 Created)")
    void deveCriarComentario() throws Exception {
        // --- ARRANGE ---
        User autorPost = criarUsuario("Dono Post", "dono@post.com", PerfilUser.ALUNO);
        Comunidade comunidade = criarComunidade("Games", autorPost);
        Post post = criarPost("Discussão Zelda", "Melhor jogo?", autorPost, comunidade);

        User comentarista = criarUsuario("Comentarista", "hater@net.com", PerfilUser.ALUNO);
        Authentication auth = new UsernamePasswordAuthenticationToken(comentarista, null, comentarista.getAuthorities());

        ComentarioCreateDTO comentarioDTO = new ComentarioCreateDTO();
        comentarioDTO.setConteudo("Com certeza é o melhor!");

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/posts/{postId}/comentarios", post.getId())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comentarioDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.conteudo").value("Com certeza é o melhor!"))
                .andExpect(jsonPath("$.autorNome").value("Comentarista"));
    }

    @Test
    @DisplayName("Integração: Admin deve conseguir deletar qualquer post (204 No Content)")
    void deveDeletarPostComoAdmin() throws Exception {
        // --- ARRANGE ---
        User autor = criarUsuario("Aluno", "aluno@delete.com", PerfilUser.ALUNO);
        Comunidade comunidade = criarComunidade("Geral", autor);
        Post post = criarPost("Post Ofensivo", "XXX", autor, comunidade);

        User admin = criarUsuario("Admin", "admin@sistema.com", PerfilUser.ADMIN);
        Authentication authAdmin = new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());

        // --- ACT ---
        mockMvc.perform(delete("/posts/{id}", post.getId())
                .with(authentication(authAdmin))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // --- ASSERT (Banco) ---
        // Verifica se o post foi realmente removido
        mockMvc.perform(get("/posts/{id}", post.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integração: Aluno NÃO deve conseguir deletar post (403 Forbidden)")
    void deveProibirAlunoDeDeletarPost() throws Exception {
        // --- ARRANGE ---
        User autor = criarUsuario("Vítima", "vitima@teste.com", PerfilUser.ALUNO);
        Comunidade comunidade = criarComunidade("Segura", autor);
        Post post = criarPost("Post Importante", "Conteúdo útil", autor, comunidade);

        User atacante = criarUsuario("Hacker", "hacker@teste.com", PerfilUser.ALUNO);
        Authentication authAluno = new UsernamePasswordAuthenticationToken(atacante, null, atacante.getAuthorities());

        // --- ACT & ASSERT ---
        // Endpoint exige 'hasAuthority("ADMIN")', então ALUNO deve falhar
        mockMvc.perform(delete("/posts/{id}", post.getId())
                .with(authentication(authAluno))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Integração: Admin deve deletar comentário ofensivo (204 No Content)")
    void deveDeletarComentarioComoAdmin() throws Exception {
        // --- ARRANGE ---
        User autorPost = criarUsuario("Autor Post 2", "autor.post2@del.com", PerfilUser.ALUNO);
        Comunidade comunidade = criarComunidade("Comunidade Del 2", autorPost);
        Post post = criarPost("Post Normal", "Conteúdo ok", autorPost, comunidade);

        User comentarista = criarUsuario("Troll", "troll@del.com", PerfilUser.ALUNO);
        
        // USO DO MÉTODO QUE ESTAVA FALTANDO
        Comentario comentario = criarComentario("Comentário Tóxico", comentarista, post);

        User admin = criarUsuario("Admin Mod", "admin.mod@del.com", PerfilUser.ADMIN);
        Authentication authAdmin = new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());

        // --- ACT ---
        mockMvc.perform(delete("/posts/{postId}/comentarios/{comentarioId}", post.getId(), comentario.getId())
                .with(authentication(authAdmin))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // --- ASSERT ---
        // Verifica se o comentário sumiu do banco
        boolean existe = comentarioRepository.existsById(comentario.getId());
        assertFalse(existe, "O comentário deveria ter sido removido do banco de dados.");
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

    private Comunidade criarComunidade(String nome, User criador) {
        Comunidade c = new Comunidade();
        c.setNome(nome);
        c.setDescricao("Desc");
        c.setCriador(criador);
        return comunidadeRepository.save(c);
    }

    private Post criarPost(String titulo, String conteudo, User autor, Comunidade comunidade) {
        Post p = new Post();
        p.setTitulo(titulo);
        p.setConteudo(conteudo);
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
package br.ifsp.events.controller;

import br.ifsp.events.config.GamificationConstants;
import br.ifsp.events.dto.post.PostCreateDTO;
import br.ifsp.events.dto.voto.VotoCreateDTO;
import br.ifsp.events.model.Comunidade;
import br.ifsp.events.model.NivelEngajamento;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.Post;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.TipoVoto;
import br.ifsp.events.model.User;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de Integração focado no ciclo de vida da Gamificação.
 * Verifica se as ações na API (Posts, Votos) refletem corretamente
 * no saldo de pontos dos usuários no banco de dados.
 */
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

    // --- CORREÇÃO DO ERRO DE CONTEXTO ---
    // O EmailService exige a propriedade 'app.base-url' que não existe no profile de teste.
    // Como este teste é sobre Gamificação, nós "Mockamos" o serviço de email e notificação.
    // Isso impede que o Spring tente criar o bean real e falhe, e também deixa o teste mais rápido.
    @MockBean
    private EmailService emailService;

    @MockBean
    private NotificationService notificationService;
    // -------------------------------------

    @Test
    @DisplayName("Integração: Criar Post deve debitar pontos do usuário (Custo: 100)")
    void deveDebitarPontosAoCriarPost() throws Exception {
        // --- ARRANGE ---
        // 1. Criar usuário com saldo suficiente
        User autor = criarUsuario("Autor Rico", "rico@test.com", 500L);
        
        // 2. Criar comunidade para postar
        Comunidade comunidade = criarComunidade("Comunidade Teste", autor);

        // 3. Autenticação
        Authentication auth = new UsernamePasswordAuthenticationToken(autor, null, autor.getAuthorities());

        // 4. DTO do Post
        PostCreateDTO postDTO = new PostCreateDTO("Titulo", "Conteudo");

        // --- ACT ---
        mockMvc.perform(post("/comunidades/{id}/posts", comunidade.getId())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDTO)))
                .andExpect(status().isCreated());

        // --- ASSERT ---
        // Recarrega o usuário do banco para ver o novo saldo
        User autorAtualizado = userRepository.findById(autor.getId()).orElseThrow();
        
        // Saldo inicial (500) + Custo Post (-100) = 400
        long saldoEsperado = 500L + GamificationConstants.PONTOS_CRIAR_POST;
        
        assertEquals(saldoEsperado, autorAtualizado.getPontosSaldo(), 
            "O saldo do usuário deveria ter sido reduzido após criar um post.");
    }

    @Test
    @DisplayName("Integração: Não deve permitir criar post se saldo for insuficiente")
    void naoDeveCriarPostSemSaldo() throws Exception {
        // --- ARRANGE ---
        // Usuário com 0 pontos
        User autorPobre = criarUsuario("Autor Pobre", "pobre@test.com", 0L);
        Comunidade comunidade = criarComunidade("Comunidade Free", autorPobre);
        Authentication auth = new UsernamePasswordAuthenticationToken(autorPobre, null, autorPobre.getAuthorities());

        PostCreateDTO postDTO = new PostCreateDTO("Titulo", "Conteudo");

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/comunidades/{id}/posts", comunidade.getId())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDTO)))
                .andExpect(status().isBadRequest()); // Espera BusinessRuleException (400)
    }

    @Test
    @DisplayName("Integração: Autor do Post deve ganhar pontos ao receber UPVOTE")
    void autorDeveGanharPontosComUpvote() throws Exception {
        // --- ARRANGE ---
        // 1. Autor do post (Começa com 0 para facilitar a conta)
        User autorPost = criarUsuario("Autor Post", "autor.post@test.com", 0L);
        Comunidade comunidade = criarComunidade("Geral", autorPost);
        Post post = criarPost(autorPost, comunidade);

        // 2. Eleitor (quem vai votar)
        User eleitor = criarUsuario("Eleitor", "eleitor@test.com", 100L);
        Authentication authEleitor = new UsernamePasswordAuthenticationToken(eleitor, null, eleitor.getAuthorities());

        // 3. DTO do Voto
        VotoCreateDTO votoDTO = new VotoCreateDTO();
        votoDTO.setPostId(post.getId());
        votoDTO.setTipoVoto(TipoVoto.UPVOTE);

        // --- ACT ---
        mockMvc.perform(post("/votos")
                .with(authentication(authEleitor))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(votoDTO)))
                .andExpect(status().isOk());

        // --- ASSERT ---
        User autorAtualizado = userRepository.findById(autorPost.getId()).orElseThrow();
        
        // Autor deve receber pontos por RECEBER_UPVOTE
        assertEquals(GamificationConstants.PONTOS_RECEBER_UPVOTE, autorAtualizado.getPontosSaldo(),
            "O autor do post deveria ter recebido pontos pelo upvote.");
            
        // Eleitor deve receber pontos por VOTAR (Ação de engajamento)
        User eleitorAtualizado = userRepository.findById(eleitor.getId()).orElseThrow();
        assertEquals(100L + GamificationConstants.PONTOS_VOTAR, eleitorAtualizado.getPontosSaldo(),
            "O eleitor deveria ter recebido pontos por engajar (votar).");
    }

    // --- Métodos Auxiliares (Helpers) para manter o código limpo ---

    private User criarUsuario(String nome, String email, Long pontosIniciais) {
        User user = new User();
        user.setNome(nome);
        user.setEmail(email);
        user.setSenha("123");
        user.setPerfilUser(PerfilUser.ALUNO);
        user.setStatusUser(StatusUser.ATIVO);
        user.setPontosSaldo(pontosIniciais);
        user.setPontosRecorde(pontosIniciais); // Recorde acompanha saldo inicial
        user.setNivel(NivelEngajamento.BRONZE);
        return userRepository.save(user);
    }

    private Comunidade criarComunidade(String nome, User criador) {
        Comunidade c = new Comunidade();
        c.setNome(nome);
        c.setDescricao("Descrição teste");
        c.setCriador(criador);
        return comunidadeRepository.save(c);
    }

    private Post criarPost(User autor, Comunidade comunidade) {
        Post p = new Post();
        p.setTitulo("Post para Votar");
        p.setConteudo("Conteúdo relevante");
        p.setAutor(autor);
        p.setComunidade(comunidade);
        p.setVotos(0);
        return postRepository.save(p);
    }
}
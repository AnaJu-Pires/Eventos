package br.ifsp.events.service.impl;

import br.ifsp.events.dto.post.PostCreateDTO;
import br.ifsp.events.dto.post.PostResponseDTO;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.Comunidade;
import br.ifsp.events.model.Post;
import br.ifsp.events.model.TipoAcaoGamificacao;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.ComunidadeRepository;
import br.ifsp.events.repository.PostRepository;
import br.ifsp.events.service.GamificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link PostServiceImpl}.
 * Cobre criação, listagem e busca de posts com integração de gamificação.
 */
@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ComunidadeRepository comunidadeRepository;

    @Mock
    private GamificationService gamificationService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PostServiceImpl postService;

    private User autorFixture;
    private User criadorFixture;
    private Comunidade comunidadeFixture;
    private Post postFixture;
    private PostCreateDTO createDTOFixture;
    private PostResponseDTO responseDTOFixture;

    @BeforeEach
    void setUp() {
        // Setup SecurityContextHolder
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Fixture: User (Autor)
        autorFixture = new User();
        autorFixture.setId(1L);
        autorFixture.setNome("João Author");
        autorFixture.setEmail("joao@aluno.ifsp.edu.br");

        // Fixture: User (Criador da comunidade)
        criadorFixture = new User();
        criadorFixture.setId(2L);
        criadorFixture.setNome("Maria Criadora");
        criadorFixture.setEmail("maria@test.com");

        // Fixture: Comunidade
        comunidadeFixture = Comunidade.builder()
            .id(1L)
            .nome("Futebol Amigos")
            .descricao("Comunidade de futebol")
            .criador(criadorFixture)
            .build();

        // Fixture: Post
        postFixture = Post.builder()
            .id(1L)
            .titulo("Novo jogo marcado")
            .conteudo("Vamos jogar amanhã às 14h")
            .autor(autorFixture)
            .comunidade(comunidadeFixture)
            .votos(0)
            .dataCriacao(LocalDateTime.now())
            .build();

        // Fixture: CreateDTO
        createDTOFixture = new PostCreateDTO();
        createDTOFixture.setTitulo("Novo jogo marcado");
        createDTOFixture.setConteudo("Vamos jogar amanhã às 14h");

        // Fixture: ResponseDTO
        responseDTOFixture = PostResponseDTO.builder()
            .id(1L)
            .titulo("Novo jogo marcado")
            .conteudo("Vamos jogar amanhã às 14h")
            .autorNome("João Author")
            .comunidadeNome("Futebol Amigos")
            .dataCriacao(LocalDateTime.now())
            .votos(0)
            .build();
    }

    @Test
    void criarPost_comDadosValidos_retornaDTO() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(autorFixture);
        when(comunidadeRepository.findById(1L)).thenReturn(Optional.of(comunidadeFixture));
        when(postRepository.save(any(Post.class))).thenReturn(postFixture);

        // Act
        PostResponseDTO result = postService.create(createDTOFixture, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("Novo jogo marcado", result.getTitulo());
        assertEquals("João Author", result.getAutorNome());
        verify(postRepository, times(1)).save(any(Post.class));
        verify(gamificationService, times(1)).checarPermissao(autorFixture, TipoAcaoGamificacao.CRIAR_POST);
        verify(gamificationService, times(1)).registrarAcao(autorFixture, TipoAcaoGamificacao.CRIAR_POST);
    }

    @Test
    void criarPost_registraAcaoParaCriadorDaComunidade() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(autorFixture);
        when(comunidadeRepository.findById(1L)).thenReturn(Optional.of(comunidadeFixture));
        when(postRepository.save(any(Post.class))).thenReturn(postFixture);

        // Act
        postService.create(createDTOFixture, 1L);

        // Assert
        verify(gamificationService, times(1))
            .registrarAcao(criadorFixture, TipoAcaoGamificacao.RECEBER_POST_EM_COMUNIDADE);
    }

    @Test
    void criarPost_naoRegistraAcaoParoAutorQuandoEhCriador() {
        // Arrange - Author é o criador da comunidade
        postFixture.setAutor(criadorFixture);
        when(authentication.getPrincipal()).thenReturn(criadorFixture);
        when(comunidadeRepository.findById(1L)).thenReturn(Optional.of(comunidadeFixture));
        when(postRepository.save(any(Post.class))).thenReturn(postFixture);

        // Act
        postService.create(createDTOFixture, 1L);

        // Assert
        verify(gamificationService, never())
            .registrarAcao(criadorFixture, TipoAcaoGamificacao.RECEBER_POST_EM_COMUNIDADE);
    }

    @Test
    void criarPost_comComunidadeInvalida_lancaResourceNotFoundException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(autorFixture);
        when(comunidadeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            postService.create(createDTOFixture, 999L);
        });
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void listarPostsPorComunidade_comPaginacao_retornaPage() {
        // Arrange
        Post post2 = Post.builder()
            .id(2L)
            .titulo("Treino agora")
            .conteudo("Vamos treinar")
            .autor(autorFixture)
            .comunidade(comunidadeFixture)
            .votos(0)
            .build();

        List<Post> posts = Arrays.asList(postFixture, post2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> pageResult = new PageImpl<>(posts, pageable, 2);

        when(comunidadeRepository.existsById(1L)).thenReturn(true);
        when(postRepository.findAllByComunidadeId(1L, pageable)).thenReturn(pageResult);

        // Act
        Page<PostResponseDTO> result = postService.listByComunidade(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());
        verify(postRepository, times(1)).findAllByComunidadeId(1L, pageable);
    }

    @Test
    void listarPostsPorComunidade_comComunidadeInvalida_lancaResourceNotFoundException() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(comunidadeRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            postService.listByComunidade(999L, pageable);
        });
    }

    @Test
    void buscarPostPorId_comIdValido_retornaDTO() {
        // Arrange
        when(postRepository.findById(1L)).thenReturn(Optional.of(postFixture));

        // Act
        PostResponseDTO result = postService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Novo jogo marcado", result.getTitulo());
        verify(postRepository, times(1)).findById(1L);
    }

    @Test
    void buscarPostPorId_comIdInvalido_lancaResourceNotFoundException() {
        // Arrange
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            postService.findById(999L);
        });
    }
}

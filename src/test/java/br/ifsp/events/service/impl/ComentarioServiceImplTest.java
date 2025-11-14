package br.ifsp.events.service.impl;

import br.ifsp.events.dto.comentario.ComentarioCreateDTO;
import br.ifsp.events.dto.comentario.ComentarioResponseDTO;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.Comentario;
import br.ifsp.events.model.Post;
import br.ifsp.events.model.TipoAcaoGamificacao;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.ComentarioRepository;
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
 * Testes unitários para {@link ComentarioServiceImpl}.
 * Cobre criação, listagem e busca de comentários com suporte a replies.
 */
@ExtendWith(MockitoExtension.class)
class ComentarioServiceImplTest {

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private GamificationService gamificationService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ComentarioServiceImpl comentarioService;

    private User autorFixture;
    private User autorPostFixture;
    private Post postFixture;
    private Comentario comentarioFixture;
    private ComentarioCreateDTO createDTOFixture;
    private ComentarioResponseDTO responseDTOFixture;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Fixture: User (Autor do comentário)
        autorFixture = new User();
        autorFixture.setId(1L);
        autorFixture.setNome("João Comentarista");
        autorFixture.setEmail("joao@test.com");

        // Fixture: User (Autor do post)
        autorPostFixture = new User();
        autorPostFixture.setId(2L);
        autorPostFixture.setNome("Maria Autora");
        autorPostFixture.setEmail("maria@test.com");

        // Fixture: Post
        postFixture = Post.builder()
            .id(1L)
            .titulo("Novo jogo marcado")
            .conteudo("Vamos jogar amanhã")
            .autor(autorPostFixture)
            .build();

        // Fixture: Comentario
        comentarioFixture = Comentario.builder()
            .id(1L)
            .conteudo("Que legal!")
            .autor(autorFixture)
            .post(postFixture)
            .votos(0)
            .dataCriacao(LocalDateTime.now())
            .build();

        // Fixture: CreateDTO
        createDTOFixture = new ComentarioCreateDTO();
        createDTOFixture.setConteudo("Que legal!");

        // Fixture: ResponseDTO
        responseDTOFixture = ComentarioResponseDTO.builder()
            .id(1L)
            .conteudo("Que legal!")
            .autorNome("João Comentarista")
            .dataCriacao(LocalDateTime.now())
            .votos(0)
            .build();
    }

    @Test
    void criarComentario_comDadosValidos_retornaDTO() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(autorFixture);
        when(postRepository.findById(1L)).thenReturn(Optional.of(postFixture));
        when(comentarioRepository.save(any(Comentario.class))).thenReturn(comentarioFixture);

        // Act
        ComentarioResponseDTO result = comentarioService.create(createDTOFixture, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("Que legal!", result.getConteudo());
        verify(comentarioRepository, times(1)).save(any(Comentario.class));
        verify(gamificationService, times(1))
            .registrarAcao(autorFixture, TipoAcaoGamificacao.CRIAR_COMENTARIO);
    }

    @Test
    void criarComentario_registraAcaoParaAutorDoPost() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(autorFixture);
        when(postRepository.findById(1L)).thenReturn(Optional.of(postFixture));
        when(comentarioRepository.save(any(Comentario.class))).thenReturn(comentarioFixture);

        // Act
        comentarioService.create(createDTOFixture, 1L);

        // Assert
        verify(gamificationService, times(1))
            .registrarAcao(autorPostFixture, TipoAcaoGamificacao.RECEBER_COMENTARIO);
    }

    @Test
    void criarComentario_naoRegistraAcaoQuandoAutorEhDono() {
        // Arrange - Autor do comentário é o mesmo do post
        comentarioFixture.setAutor(autorPostFixture);
        when(authentication.getPrincipal()).thenReturn(autorPostFixture);
        when(postRepository.findById(1L)).thenReturn(Optional.of(postFixture));
        when(comentarioRepository.save(any(Comentario.class))).thenReturn(comentarioFixture);

        // Act
        comentarioService.create(createDTOFixture, 1L);

        // Assert
        verify(gamificationService, never())
            .registrarAcao(autorPostFixture, TipoAcaoGamificacao.RECEBER_COMENTARIO);
    }

    @Test
    void criarComentario_comPostInvalido_lancaResourceNotFoundException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(autorFixture);
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            comentarioService.create(createDTOFixture, 999L);
        });
        verify(comentarioRepository, never()).save(any(Comentario.class));
    }

    @Test
    void criarComentarioReply_comComentarioPaiValido_retornaDTO() {
        // Arrange
        createDTOFixture.setComentarioPaiId(1L);
        when(authentication.getPrincipal()).thenReturn(autorFixture);
        when(postRepository.findById(1L)).thenReturn(Optional.of(postFixture));
        when(comentarioRepository.findById(1L)).thenReturn(Optional.of(comentarioFixture));
        when(comentarioRepository.save(any(Comentario.class))).thenReturn(comentarioFixture);

        // Act
        ComentarioResponseDTO result = comentarioService.create(createDTOFixture, 1L);

        // Assert
        assertNotNull(result);
        verify(comentarioRepository, times(1)).save(any(Comentario.class));
    }

    @Test
    void criarComentarioReply_comComentarioPaiInvalido_lancaResourceNotFoundException() {
        // Arrange
        createDTOFixture.setComentarioPaiId(999L);
        when(authentication.getPrincipal()).thenReturn(autorFixture);
        when(postRepository.findById(1L)).thenReturn(Optional.of(postFixture));
        when(comentarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            comentarioService.create(createDTOFixture, 1L);
        });
    }

    @Test
    void listarComentariosPorPost_comPaginacao_retornaPage() {
        // Arrange
        Comentario comentario2 = Comentario.builder()
            .id(2L)
            .conteudo("Que maneira!")
            .autor(autorFixture)
            .post(postFixture)
            .build();

        List<Comentario> comentarios = Arrays.asList(comentarioFixture, comentario2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comentario> pageResult = new PageImpl<>(comentarios, pageable, 2);

        when(postRepository.existsById(1L)).thenReturn(true);
        when(comentarioRepository.findAllByPostIdAndComentarioPaiIsNull(1L, pageable))
            .thenReturn(pageResult);

        // Act
        Page<ComentarioResponseDTO> result = comentarioService.listByPost(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(comentarioRepository, times(1)).findAllByPostIdAndComentarioPaiIsNull(1L, pageable);
    }

    @Test
    void listarComentariosPorPost_comPostInvalido_lancaResourceNotFoundException() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(postRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            comentarioService.listByPost(999L, pageable);
        });
    }
}

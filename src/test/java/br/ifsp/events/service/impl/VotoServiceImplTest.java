package br.ifsp.events.service.impl;

import br.ifsp.events.dto.voto.VotoCreateDTO;
import br.ifsp.events.dto.voto.VotoResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.*;
import br.ifsp.events.repository.ComentarioRepository;
import br.ifsp.events.repository.PostRepository;
import br.ifsp.events.repository.VotoRepository;
import br.ifsp.events.service.GamificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link VotoServiceImpl}.
 * Cobre votação em posts e comentários com validações de negócio.
 */
@ExtendWith(MockitoExtension.class)
class VotoServiceImplTest {

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private GamificationService gamificationService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private VotoServiceImpl votoService;

    private User votanteFixture;
    private User autorPostFixture;
    private Post postFixture;
    private Comentario comentarioFixture;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Fixture: User (Votante)
        votanteFixture = new User();
        votanteFixture.setId(1L);
        votanteFixture.setNome("João Votante");
        votanteFixture.setEmail("joao@aluno.ifsp.edu.br");

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
            .votos(0)
            .build();

        // Fixture: Comentario
        comentarioFixture = Comentario.builder()
            .id(1L)
            .conteudo("Que legal!")
            .autor(autorPostFixture)
            .post(postFixture)
            .votos(0)
            .build();
    }

    @Test
    void votarEmPost_comVotoUpvote_retornaDTO() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(votanteFixture);
        when(postRepository.findById(1L)).thenReturn(Optional.of(postFixture));
        when(votoRepository.findByUsuarioIdAndPostId(1L, 1L)).thenReturn(Optional.empty());
        when(votoRepository.save(any(Voto.class))).thenReturn(new Voto());

        VotoCreateDTO createDTO = new VotoCreateDTO();
        createDTO.setPostId(1L);
        createDTO.setTipoVoto(TipoVoto.UPVOTE);

        // Act
        VotoResponseDTO result = votoService.votar(createDTO);

        // Assert
        assertNotNull(result);
        verify(votoRepository, times(1)).save(any(Voto.class));
        verify(gamificationService, times(1))
            .registrarAcao(votanteFixture, TipoAcaoGamificacao.VOTAR);
    }

    @Test
    void votarEmPost_comVotoDownvote_retornaDTO() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(votanteFixture);
        when(postRepository.findById(1L)).thenReturn(Optional.of(postFixture));
        when(votoRepository.findByUsuarioIdAndPostId(1L, 1L)).thenReturn(Optional.empty());
        when(votoRepository.save(any(Voto.class))).thenReturn(new Voto());

        VotoCreateDTO createDTO = new VotoCreateDTO();
        createDTO.setPostId(1L);
        createDTO.setTipoVoto(TipoVoto.DOWNVOTE);

        // Act
        VotoResponseDTO result = votoService.votar(createDTO);

        // Assert
        assertNotNull(result);
        verify(votoRepository, times(1)).save(any(Voto.class));
    }

    @Test
    void votarEmComentario_comVotoValido_retornaDTO() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(votanteFixture);
        when(comentarioRepository.findById(1L)).thenReturn(Optional.of(comentarioFixture));
        when(votoRepository.findByUsuarioIdAndComentarioId(1L, 1L)).thenReturn(Optional.empty());
        when(votoRepository.save(any(Voto.class))).thenReturn(new Voto());

        VotoCreateDTO createDTO = new VotoCreateDTO();
        createDTO.setComentarioId(1L);
        createDTO.setTipoVoto(TipoVoto.UPVOTE);

        // Act
        VotoResponseDTO result = votoService.votar(createDTO);

        // Assert
        assertNotNull(result);
        verify(votoRepository, times(1)).save(any(Voto.class));
    }

    @Test
    void votar_comAmbosPostEComentario_lancaBusinessRuleException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(votanteFixture);

        VotoCreateDTO createDTO = new VotoCreateDTO();
        createDTO.setPostId(1L);
        createDTO.setComentarioId(1L);
        createDTO.setTipoVoto(TipoVoto.UPVOTE);

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            votoService.votar(createDTO);
        });
    }

    @Test
    void votar_semPostNemComentario_lancaBusinessRuleException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(votanteFixture);

        VotoCreateDTO createDTO = new VotoCreateDTO();
        createDTO.setTipoVoto(TipoVoto.UPVOTE);

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            votoService.votar(createDTO);
        });
    }

    @Test
    void votarEmPost_comPostInvalido_lancaResourceNotFoundException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(votanteFixture);
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        VotoCreateDTO createDTO = new VotoCreateDTO();
        createDTO.setPostId(999L);
        createDTO.setTipoVoto(TipoVoto.UPVOTE);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            votoService.votar(createDTO);
        });
    }

    @Test
    void votarEmComentario_comComentarioInvalido_lancaResourceNotFoundException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(votanteFixture);
        when(comentarioRepository.findById(999L)).thenReturn(Optional.empty());

        VotoCreateDTO createDTO = new VotoCreateDTO();
        createDTO.setComentarioId(999L);
        createDTO.setTipoVoto(TipoVoto.UPVOTE);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            votoService.votar(createDTO);
        });
    }
}

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    private Comunidade comunidadeFixture;
    private Post postFixture;
    private PostCreateDTO createDTOFixture;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);

        autorFixture = new User();
        autorFixture.setId(1L);
        autorFixture.setNome("João Author");

        comunidadeFixture = Comunidade.builder()
            .id(1L)
            .criador(new User())
            .build();
        comunidadeFixture.getCriador().setId(2L);

        postFixture = Post.builder()
            .id(1L)
            .titulo("Novo jogo")
            .autor(autorFixture)
            .comunidade(comunidadeFixture)
            .dataCriacao(LocalDateTime.now())
            .build();

        createDTOFixture = new PostCreateDTO();
        createDTOFixture.setTitulo("Novo jogo");
    }

    @Test
    void criarPost_comDadosValidos_retornaDTO() {
        when(authentication.getPrincipal()).thenReturn(autorFixture);
        when(comunidadeRepository.findById(1L)).thenReturn(Optional.of(comunidadeFixture));
        when(postRepository.save(any(Post.class))).thenReturn(postFixture);

        PostResponseDTO result = postService.create(createDTOFixture, 1L);

        assertNotNull(result);
        assertEquals("Novo jogo", result.getTitulo());
        
        verify(gamificationService, times(1)).registrarAcao(autorFixture, TipoAcaoGamificacao.CRIAR_POST);

        verify(gamificationService, times(1)).registrarAcao(any(User.class), eq(TipoAcaoGamificacao.RECEBER_POST_EM_COMUNIDADE));
    }

    @Test
    void criarPost_comComunidadeInvalida_lancaResourceNotFoundException() {
        when(authentication.getPrincipal()).thenReturn(autorFixture);
        when(comunidadeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            postService.create(createDTOFixture, 999L);
        });
    }
}
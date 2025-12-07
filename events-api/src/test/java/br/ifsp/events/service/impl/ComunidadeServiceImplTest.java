package br.ifsp.events.service.impl;

import br.ifsp.events.dto.comunidade.ComunidadeCreateDTO;
import br.ifsp.events.dto.comunidade.ComunidadeResponseDTO;
import br.ifsp.events.exception.DuplicateResourceException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.Comunidade;
import br.ifsp.events.model.TipoAcaoGamificacao;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.ComunidadeRepository;
import br.ifsp.events.repository.UserRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComunidadeServiceImplTest {

    @Mock
    private ComunidadeRepository comunidadeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GamificationService gamificationService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ComunidadeServiceImpl comunidadeService;

    private User userFixture;
    private Comunidade comunidadeFixture;
    private ComunidadeCreateDTO createDTOFixture;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);

        userFixture = new User();
        userFixture.setId(1L);
        userFixture.setNome("João Silva");

        comunidadeFixture = Comunidade.builder()
            .id(1L)
            .nome("Futebol Amigos")
            .criador(userFixture)
            .dataCriacao(LocalDateTime.now())
            .build();

        createDTOFixture = new ComunidadeCreateDTO();
        createDTOFixture.setNome("Futebol Amigos");
    }

    @Test
    void criarComunidade_comDadosValidos_retornaDTO() {
        when(authentication.getPrincipal()).thenReturn(userFixture);
        when(comunidadeRepository.findByNomeIgnoreCase("Futebol Amigos")).thenReturn(Optional.empty());
        when(comunidadeRepository.save(any(Comunidade.class))).thenReturn(comunidadeFixture);

        ComunidadeResponseDTO result = comunidadeService.create(createDTOFixture);

        assertNotNull(result);
        assertEquals("Futebol Amigos", result.getNome());
        verify(comunidadeRepository, times(1)).save(any(Comunidade.class));
        verify(gamificationService, times(1)).registrarAcao(userFixture, TipoAcaoGamificacao.CRIAR_COMUNIDADE);
    }

    @Test
    void criarComunidade_comNomeDuplicado_lancaDuplicateResourceException() {
        when(authentication.getPrincipal()).thenReturn(userFixture);
        when(comunidadeRepository.findByNomeIgnoreCase("Futebol Amigos")).thenReturn(Optional.of(comunidadeFixture));

        assertThrows(DuplicateResourceException.class, () -> {
            comunidadeService.create(createDTOFixture);
        });
        verify(comunidadeRepository, never()).save(any(Comunidade.class));
    }

    @Test
    void listarTodasComunidades_retornaLista() {
        List<Comunidade> comunidades = Arrays.asList(comunidadeFixture);
        when(comunidadeRepository.findAll()).thenReturn(comunidades);

        List<ComunidadeResponseDTO> result = comunidadeService.listAll();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void buscarComunidadePorId_comIdInvalido_lancaResourceNotFoundException() {
        when(comunidadeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            comunidadeService.findById(999L);
        });
    }
}
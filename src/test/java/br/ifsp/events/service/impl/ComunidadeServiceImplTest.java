package br.ifsp.events.service.impl;

import br.ifsp.events.dto.comunidade.ComunidadeCreateDTO;
import br.ifsp.events.dto.comunidade.ComunidadeResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
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

/**
 * Testes unitários para {@link ComunidadeServiceImpl}.
 * Cobre criação, listagem e busca de comunidades com validações de segurança e duplicação.
 */
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
    private ComunidadeResponseDTO responseDTOFixture;

    @BeforeEach
    void setUp() {
        // Setup SecurityContextHolder
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Fixture: User
        userFixture = new User();
        userFixture.setId(1L);
        userFixture.setNome("João Silva");
        userFixture.setEmail("joao@test.com");

        // Fixture: Comunidade
        comunidadeFixture = Comunidade.builder()
            .id(1L)
            .nome("Futebol Amigos")
            .descricao("Comunidade de futebol")
            .criador(userFixture)
            .dataCriacao(LocalDateTime.now())
            .build();

        // Fixture: CreateDTO
        createDTOFixture = new ComunidadeCreateDTO();
        createDTOFixture.setNome("Futebol Amigos");
        createDTOFixture.setDescricao("Comunidade de futebol");

        // Fixture: ResponseDTO
        responseDTOFixture = ComunidadeResponseDTO.builder()
            .id(1L)
            .nome("Futebol Amigos")
            .descricao("Comunidade de futebol")
            .criadorNome("João Silva")
            .dataCriacao(LocalDateTime.now())
            .build();
    }

    @Test
    void criarComunidade_comDadosValidos_retornaDTO() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userFixture);
        when(comunidadeRepository.findByNomeIgnoreCase("Futebol Amigos")).thenReturn(Optional.empty());
        when(comunidadeRepository.save(any(Comunidade.class))).thenReturn(comunidadeFixture);

        // Act
        ComunidadeResponseDTO result = comunidadeService.create(createDTOFixture);

        // Assert
        assertNotNull(result);
        assertEquals("Futebol Amigos", result.getNome());
        assertEquals("João Silva", result.getCriadorNome());
        verify(comunidadeRepository, times(1)).save(any(Comunidade.class));
        verify(gamificationService, times(1)).registrarAcao(userFixture, TipoAcaoGamificacao.CRIAR_COMUNIDADE);
    }

    @Test
    void criarComunidade_comNomeDuplicado_lancaDuplicateResourceException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userFixture);
        when(comunidadeRepository.findByNomeIgnoreCase("Futebol Amigos")).thenReturn(Optional.of(comunidadeFixture));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            comunidadeService.create(createDTOFixture);
        });
        verify(comunidadeRepository, never()).save(any(Comunidade.class));
    }

    @Test
    void criarComunidade_semPermissao_lancaBusinessRuleException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userFixture);
        when(comunidadeRepository.findByNomeIgnoreCase("Futebol Amigos")).thenReturn(Optional.empty());
        doThrow(new BusinessRuleException("Usuário sem permissão"))
            .when(gamificationService).checarPermissao(userFixture, TipoAcaoGamificacao.CRIAR_COMUNIDADE);

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            comunidadeService.create(createDTOFixture);
        });
        verify(comunidadeRepository, never()).save(any(Comunidade.class));
    }

    @Test
    void listarTodasComunidades_retornaLista() {
        // Arrange
        Comunidade comunidade2 = Comunidade.builder()
            .id(2L)
            .nome("Vôlei Amigos")
            .descricao("Comunidade de vôlei")
            .criador(userFixture)
            .build();

        List<Comunidade> comunidades = Arrays.asList(comunidadeFixture, comunidade2);
        when(comunidadeRepository.findAll()).thenReturn(comunidades);

        // Act
        List<ComunidadeResponseDTO> result = comunidadeService.listAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(comunidadeRepository, times(1)).findAll();
    }

    @Test
    void buscarComunidadePorId_comIdValido_retornaDTO() {
        // Arrange
        when(comunidadeRepository.findById(1L)).thenReturn(Optional.of(comunidadeFixture));

        // Act
        ComunidadeResponseDTO result = comunidadeService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Futebol Amigos", result.getNome());
        verify(comunidadeRepository, times(1)).findById(1L);
    }

    @Test
    void buscarComunidadePorId_comIdInvalido_lancaResourceNotFoundException() {
        // Arrange
        when(comunidadeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            comunidadeService.findById(999L);
        });
    }
}

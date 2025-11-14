package br.ifsp.events.service.impl;

import br.ifsp.events.dto.convite.ConviteResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.*;
import br.ifsp.events.repository.ConviteRepository;
import br.ifsp.events.repository.TimeRepository;
import br.ifsp.events.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link ConviteServiceImpl}.
 * Cobre listagem, aceitação e rejeição de convites para times.
 */
@ExtendWith(MockitoExtension.class)
class ConviteServiceImplTest {

    @Mock
    private ConviteRepository conviteRepository;

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ConviteServiceImpl conviteService;

    private User usuarioFixture;
    private User capitaoFixture;
    private Time timeFixture;
    private Modalidade modalidadeFixture;
    private Convite conviteFixture;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Fixture: User (Convidado)
        usuarioFixture = new User();
        usuarioFixture.setId(1L);
        usuarioFixture.setNome("João Convidado");
        usuarioFixture.setEmail("joao@test.com");
        usuarioFixture.setTimesQueParticipo(new HashSet<>());

        // Fixture: User (Capitão)
        capitaoFixture = new User();
        capitaoFixture.setId(2L);
        capitaoFixture.setNome("Maria Capitã");
        capitaoFixture.setEmail("maria@test.com");

        // Fixture: Modalidade
        modalidadeFixture = new Modalidade();
        modalidadeFixture.setId(1L);
        modalidadeFixture.setNome("Futebol");

        // Fixture: Time
        timeFixture = new Time();
        timeFixture.setId(1L);
        timeFixture.setNome("Time A");
        timeFixture.setCapitao(capitaoFixture);
        timeFixture.setModalidade(modalidadeFixture);
        timeFixture.setMembros(new HashSet<>());

        // Fixture: Convite
        conviteFixture = new Convite();
        conviteFixture.setId(1L);
        conviteFixture.setUsuarioConvidado(usuarioFixture);
        conviteFixture.setTime(timeFixture);
        conviteFixture.setStatus(StatusConvite.PENDENTE);
        conviteFixture.setDataExpiracao(LocalDateTime.now().plusDays(7));
    }

    @Test
    void listarMeusConvites_retornaListaDeConvitesPendentes() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(usuarioFixture);
        ConviteResponseDTO conviteDTO = new ConviteResponseDTO();
        conviteDTO.setId(1L);

        when(conviteRepository.findByUsuarioConvidadoAndStatus(usuarioFixture, StatusConvite.PENDENTE))
            .thenReturn(Arrays.asList(conviteFixture));
        when(modelMapper.map(conviteFixture, ConviteResponseDTO.class)).thenReturn(conviteDTO);

        // Act
        List<ConviteResponseDTO> result = conviteService.listarMeusConvites(authentication);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(conviteRepository, times(1))
            .findByUsuarioConvidadoAndStatus(usuarioFixture, StatusConvite.PENDENTE);
    }

    @Test
    void listarMeusConvites_ignoraConvitesExpirados() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(usuarioFixture);
        Convite conviteExpirado = new Convite();
        conviteExpirado.setStatus(StatusConvite.PENDENTE);
        conviteExpirado.setDataExpiracao(LocalDateTime.now().minusDays(1)); // Expirado

        when(conviteRepository.findByUsuarioConvidadoAndStatus(usuarioFixture, StatusConvite.PENDENTE))
            .thenReturn(Arrays.asList(conviteExpirado));

        // Act
        List<ConviteResponseDTO> result = conviteService.listarMeusConvites(authentication);

        // Assert
        assertEquals(0, result.size()); // Convite expirado não deve aparecer
    }

    @Test
    void aceitarConvite_comConviteValido_adicionaUsuarioAoTime() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(usuarioFixture);
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioFixture));
        when(conviteRepository.findByIdAndUsuarioConvidado(1L, usuarioFixture))
            .thenReturn(Optional.of(conviteFixture));
        when(timeRepository.save(any(Time.class))).thenReturn(timeFixture);

        // Act
        conviteService.aceitarConvite(1L, authentication);

        // Assert
        verify(timeRepository, times(1)).save(any(Time.class));
        verify(conviteRepository, times(1)).save(any(Convite.class));
    }

    @Test
    void aceitarConvite_usuarioJaTemTimeNaModalidade_lancaBusinessRuleException() {
        // Arrange
        Time timeExistente = new Time();
        timeExistente.setModalidade(modalidadeFixture);
        usuarioFixture.getTimesQueParticipo().add(timeExistente);

        when(authentication.getPrincipal()).thenReturn(usuarioFixture);
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioFixture));
        when(conviteRepository.findByIdAndUsuarioConvidado(1L, usuarioFixture))
            .thenReturn(Optional.of(conviteFixture));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            conviteService.aceitarConvite(1L, authentication);
        });
        verify(timeRepository, never()).save(any(Time.class));
    }

    @Test
    void aceitarConvite_conviteExpirado_lancaBusinessRuleException() {
        // Arrange
        conviteFixture.setDataExpiracao(LocalDateTime.now().minusDays(1)); // Expirado
        when(authentication.getPrincipal()).thenReturn(usuarioFixture);
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioFixture));
        when(conviteRepository.findByIdAndUsuarioConvidado(1L, usuarioFixture))
            .thenReturn(Optional.of(conviteFixture));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            conviteService.aceitarConvite(1L, authentication);
        });
    }

    @Test
    void recusarConvite_comConviteValido_deletaConvite() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(usuarioFixture);
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioFixture));
        when(conviteRepository.findByIdAndUsuarioConvidado(1L, usuarioFixture))
            .thenReturn(Optional.of(conviteFixture));

        // Act
        conviteService.recusarConvite(1L, authentication);

        // Assert
        verify(conviteRepository, times(1)).delete(conviteFixture);
    }

    @Test
    void recusarConvite_conviteJaAceito_lancaBusinessRuleException() {
        // Arrange
        conviteFixture.setStatus(StatusConvite.ACEITO);
        when(authentication.getPrincipal()).thenReturn(usuarioFixture);
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioFixture));
        when(conviteRepository.findByIdAndUsuarioConvidado(1L, usuarioFixture))
            .thenReturn(Optional.of(conviteFixture));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            conviteService.recusarConvite(1L, authentication);
        });
        verify(conviteRepository, never()).delete(any(Convite.class));
    }

    @Test
    void expirarConvitesPendentes_expiradosBeforeNow() {
        // Arrange
        Convite conviteParaExpirar = new Convite();
        conviteParaExpirar.setId(2L);
        conviteParaExpirar.setStatus(StatusConvite.PENDENTE);
        conviteParaExpirar.setDataExpiracao(LocalDateTime.now().minusMinutes(1));

        when(conviteRepository.findByStatusAndDataExpiracaoBefore(StatusConvite.PENDENTE, any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(conviteParaExpirar));

        // Act
        conviteService.expirarConvitesPendentes();

        // Assert
        verify(conviteRepository, times(1)).saveAll(any());
    }
}

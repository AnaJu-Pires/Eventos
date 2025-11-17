package br.ifsp.events.service.impl;

import br.ifsp.events.dto.convite.ConviteResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.*;
import br.ifsp.events.repository.ConviteRepository;
import br.ifsp.events.repository.TimeRepository;
import br.ifsp.events.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link ConviteServiceImpl}.
 * Cobre listagem, aceitação, rejeição e expiração de convites.
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

    @InjectMocks
    private ConviteServiceImpl conviteService;

    // Fixtures
    private User usuarioLogado;
    private Modalidade modalidade;
    private Time time;
    private Convite convite;
    private Authentication authMock;

    @BeforeEach
    void setUp() {
        // Fixture: Usuário
        usuarioLogado = new User();
        usuarioLogado.setId(1L);
        usuarioLogado.setNome("João Convidado");
        usuarioLogado.setTimesQueParticipo(Collections.emptySet()); // Começa sem time

        // Fixture: Modalidade
        modalidade = new Modalidade();
        modalidade.setId(1L);
        modalidade.setNome("Futsal");

        // Fixture: Time
        time = new Time();
        time.setId(1L);
        time.setModalidade(modalidade);
        time.setMembros(new java.util.HashSet<>()); // Garante que a lista de membros é mutável

        // Fixture: Convite
        convite = new Convite();
        convite.setId(1L);
        convite.setTime(time);
        convite.setUsuarioConvidado(usuarioLogado);
        convite.setStatus(StatusConvite.PENDENTE);
        convite.setDataExpiracao(LocalDateTime.now().plusDays(1));

        // Mock: Autenticação
        authMock = mock(Authentication.class);
        lenient().when(authMock.getPrincipal()).thenReturn(usuarioLogado);
    }


    @Test
    @DisplayName("listarMeusConvites deve retornar apenas convites válidos (filtrando os expirados)")
    void listarMeusConvitesFiltraExpirados() {
        // Arrange
        Convite conviteValido = convite;
        
        Convite conviteExpirado = new Convite();
        conviteExpirado.setId(2L);
        conviteExpirado.setStatus(StatusConvite.PENDENTE);
        conviteExpirado.setDataExpiracao(LocalDateTime.now().minusDays(1)); // Expirado
        
        when(conviteRepository.findByUsuarioConvidadoAndStatus(usuarioLogado, StatusConvite.PENDENTE))
                .thenReturn(List.of(conviteValido, conviteExpirado));
        
        when(modelMapper.map(conviteValido, ConviteResponseDTO.class)).thenReturn(new ConviteResponseDTO());

        // Act
        List<ConviteResponseDTO> result = conviteService.listarMeusConvites(authMock);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size()); // Apenas o convite válido deve ser retornado

        // Garante que o convite expirado não foi mapeado
        verify(modelMapper, times(1)).map(conviteValido, ConviteResponseDTO.class);
        verify(modelMapper, never()).map(conviteExpirado, ConviteResponseDTO.class);
    }

    @Test
    @DisplayName("Deve aceitar um convite com sucesso")
    void aceitarConviteOk() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioLogado));
        when(conviteRepository.findByIdAndUsuarioConvidado(1L, usuarioLogado)).thenReturn(Optional.of(convite));
        
        // Captura os argumentos que são passados para os métodos save()
        ArgumentCaptor<Time> timeCaptor = ArgumentCaptor.forClass(Time.class);
        ArgumentCaptor<Convite> conviteCaptor = ArgumentCaptor.forClass(Convite.class);

        // Act
        conviteService.aceitarConvite(1L, authMock);

        // Assert (Verifica o ESTADO)
        // 1. Verifica se o time foi salvo
        verify(timeRepository, times(1)).save(timeCaptor.capture());
        // 2. Verifica se o usuário foi realmente adicionado ao time
        assertTrue(timeCaptor.getValue().getMembros().contains(usuarioLogado)); 

        // 3. Verifica se o convite foi salvo
        verify(conviteRepository, times(1)).save(conviteCaptor.capture());
        // 4. Verifica se o status do convite foi atualizado
        assertEquals(StatusConvite.ACEITO, conviteCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("aceitarConvite deve lançar exceção se usuário já está em time daquela modalidade")
    void aceitarConviteJaEstaEmTimeDaModalidade() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioLogado));
        
        Time timeExistente = new Time();
        timeExistente.setNome("Time Antigo");
        timeExistente.setModalidade(modalidade); // Mesma modalidade do convite
        
        usuarioLogado.setTimesQueParticipo(Set.of(timeExistente));

        when(conviteRepository.findByIdAndUsuarioConvidado(1L, usuarioLogado)).thenReturn(Optional.of(convite));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            conviteService.aceitarConvite(1L, authMock);
        });
        
        // Garante que nada foi salvo
        verify(timeRepository, never()).save(any());
        verify(conviteRepository, never()).save(any());
    }

    @Test
    @DisplayName("aceitarConvite deve lançar exceção se o convite expirou")
    void aceitarConviteExpirado() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioLogado));
        
        convite.setDataExpiracao(LocalDateTime.now().minusDays(1)); // Expirado
        when(conviteRepository.findByIdAndUsuarioConvidado(1L, usuarioLogado)).thenReturn(Optional.of(convite));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            conviteService.aceitarConvite(1L, authMock);
        });
    }

    @Test
    @DisplayName("Deve recusar (deletar) um convite com sucesso")
    void recusarConviteOk() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioLogado));
        when(conviteRepository.findByIdAndUsuarioConvidado(1L, usuarioLogado)).thenReturn(Optional.of(convite));

        // Act
        conviteService.recusarConvite(1L, authMock);

        // Assert
        verify(conviteRepository, times(1)).delete(convite);
    }

    @Test
    @DisplayName("recusarConvite deve lançar exceção se o convite não for encontrado")
    void recusarConviteNaoEncontrado() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioLogado));
        when(conviteRepository.findByIdAndUsuarioConvidado(99L, usuarioLogado)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            conviteService.recusarConvite(99L, authMock);
        });
    }

    @Test
    @DisplayName("recusarConvite deve lançar exceção se o convite já foi aceito")
    void recusarConvite_conviteJaAceito_lancaBusinessRuleException() {
        // Arrange
        convite.setStatus(StatusConvite.ACEITO);
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioLogado));
        when(conviteRepository.findByIdAndUsuarioConvidado(1L, usuarioLogado))
            .thenReturn(Optional.of(convite));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            conviteService.recusarConvite(1L, authMock);
        });
        
        // Garante que o delete não foi chamado
        verify(conviteRepository, never()).delete(any(Convite.class));
    }

    @Test
    @DisplayName("Deve expirar convites pendentes e vencidos")
    void expirarConvitesPendentes_expiradosBeforeNow() {
        // Arrange
        Convite conviteParaExpirar = new Convite();
        conviteParaExpirar.setId(2L);
        conviteParaExpirar.setStatus(StatusConvite.PENDENTE);
        conviteParaExpirar.setDataExpiracao(LocalDateTime.now().minusMinutes(1));

        when(conviteRepository.findByStatusAndDataExpiracaoBefore(eq(StatusConvite.PENDENTE), any(LocalDateTime.class)))
            .thenReturn(List.of(conviteParaExpirar));

        // Act
        conviteService.expirarConvitesPendentes();

        // Assert
        // Verifica que o saveAll foi chamado (para atualizar o status para EXPIRADO)
        verify(conviteRepository, times(1)).saveAll(any());
    }
}
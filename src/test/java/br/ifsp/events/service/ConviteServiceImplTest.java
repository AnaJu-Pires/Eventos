package br.ifsp.events.service;

import br.ifsp.events.dto.convite.ConviteResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.*;
import br.ifsp.events.repository.ConviteRepository;
import br.ifsp.events.repository.TimeRepository;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.impl.ConviteServiceImpl;

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
import static org.mockito.Mockito.*;

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

    private User usuarioLogado;
    private Modalidade modalidade;
    private Time time;
    private Convite convite;
    private Authentication authMock;

    @BeforeEach
    void setUp() {
        usuarioLogado = new User();
        usuarioLogado.setId(1L);

        modalidade = new Modalidade();
        modalidade.setId(1L);
        modalidade.setNome("Futsal");

        time = new Time();
        time.setId(1L);
        time.setModalidade(modalidade);

        convite = new Convite();
        convite.setId(1L);
        convite.setTime(time);
        convite.setUsuarioConvidado(usuarioLogado);
        convite.setStatus(StatusConvite.PENDENTE);
        convite.setDataExpiracao(LocalDateTime.now().plusDays(1));

        authMock = mock(Authentication.class);
        when(authMock.getPrincipal()).thenReturn(usuarioLogado);
        
    }


    @Test
    @DisplayName("listarMeusConvites deve retornar apenas convites válidos (filtrando os expirados)")
    void listarMeusConvitesFiltraExpirados() {
        Convite conviteValido = convite;
        
        Convite conviteExpirado = new Convite();
        conviteExpirado.setId(2L);
        conviteExpirado.setStatus(StatusConvite.PENDENTE);
        conviteExpirado.setDataExpiracao(LocalDateTime.now().minusDays(1));
        
        when(conviteRepository.findByUsuarioConvidadoAndStatus(usuarioLogado, StatusConvite.PENDENTE))
                .thenReturn(List.of(conviteValido, conviteExpirado));
        when(modelMapper.map(conviteValido, ConviteResponseDTO.class)).thenReturn(new ConviteResponseDTO());

        List<ConviteResponseDTO> result = conviteService.listarMeusConvites(authMock);
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(conviteRepository, never()).save(any(Convite.class));
        
        verify(modelMapper, times(1)).map(conviteValido, ConviteResponseDTO.class);
    }

    @Test
    @DisplayName("Deve aceitar um convite com sucesso")
    void aceitarConviteOk() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioLogado));
        when(conviteRepository.findByIdAndUsuarioConvidado(1L, usuarioLogado)).thenReturn(Optional.of(convite));
        usuarioLogado.setTimesQueParticipo(Collections.emptySet());
        ArgumentCaptor<Time> timeCaptor = ArgumentCaptor.forClass(Time.class);
        ArgumentCaptor<Convite> conviteCaptor = ArgumentCaptor.forClass(Convite.class);

        conviteService.aceitarConvite(1L, authMock);

        verify(timeRepository, times(1)).save(timeCaptor.capture());
        assertTrue(timeCaptor.getValue().getMembros().contains(usuarioLogado));
        verify(conviteRepository, times(1)).save(conviteCaptor.capture());
        assertEquals(StatusConvite.ACEITO, conviteCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("aceitarConvite deve lançar exceção se usuário já está em time daquela modalidade")
    void aceitarConviteJaEstaEmTimeDaModalidade() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioLogado));
        
        Time timeExistente = new Time();
        timeExistente.setNome("Time Antigo");
        timeExistente.setModalidade(modalidade);
        
        usuarioLogado.setTimesQueParticipo(Set.of(timeExistente));

        when(conviteRepository.findByIdAndUsuarioConvidado(1L, usuarioLogado)).thenReturn(Optional.of(convite));

        assertThrows(BusinessRuleException.class, () -> {
            conviteService.aceitarConvite(1L, authMock);
        });
        verify(timeRepository, never()).save(any());
    }

    @Test
    @DisplayName("aceitarConvite deve lançar exceção se o convite expirou")
    void aceitarConviteExpirado() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioLogado));
        
        convite.setDataExpiracao(LocalDateTime.now().minusDays(1)); // Expirado
        when(conviteRepository.findByIdAndUsuarioConvidado(1L, usuarioLogado)).thenReturn(Optional.of(convite));

        assertThrows(BusinessRuleException.class, () -> {
            conviteService.aceitarConvite(1L, authMock);
        });
    }

    @Test
    @DisplayName("Deve recusar (deletar) um convite com sucesso")
    void recusarConviteOk() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioLogado));
        when(conviteRepository.findByIdAndUsuarioConvidado(1L, usuarioLogado)).thenReturn(Optional.of(convite));

        conviteService.recusarConvite(1L, authMock);

        verify(conviteRepository, times(1)).delete(convite);
    }

    @Test
    @DisplayName("recusarConvite deve lançar exceção se o convite não for encontrado")
    void recusarConviteNaoEncontrado() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioLogado));
        when(conviteRepository.findByIdAndUsuarioConvidado(99L, usuarioLogado)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            conviteService.recusarConvite(99L, authMock);
        });
    }
}
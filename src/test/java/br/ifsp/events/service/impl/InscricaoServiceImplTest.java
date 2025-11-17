package br.ifsp.events.service.impl;

import br.ifsp.events.dto.inscricao.InscricaoResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.*;
import br.ifsp.events.repository.EventoRepository;
import br.ifsp.events.repository.InscricaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link InscricaoServiceImpl}.
 * Cobre aprovação e rejeição de inscrições em eventos.
 */
@ExtendWith(MockitoExtension.class)
class InscricaoServiceImplTest {

    @Mock
    private InscricaoRepository inscricaoRepository;

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private InscricaoServiceImpl inscricaoService;

    private User gestorFixture;
    private Evento eventoFixture;
    private Inscricao inscricaoFixture;
    private EventoModalidade eventoModalidadeFixture;
    private Time timeFixture;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Fixture: User (Gestor do evento)
        gestorFixture = new User();
        gestorFixture.setId(1L);
        gestorFixture.setNome("João Gestor");
        gestorFixture.setEmail("joao@aluno.ifsp.edu.br");

        // Fixture: Evento
        eventoFixture = new Evento();
        eventoFixture.setId(1L);
        eventoFixture.setNome("Campeonato de Futebol");
        eventoFixture.setOrganizador(gestorFixture);
        eventoFixture.setStatus(StatusEvento.PLANEJADO);
        eventoFixture.setDataInicio(LocalDate.now().plusDays(10));
        eventoFixture.setDataFim(LocalDate.now().plusDays(15));

        // Fixture: Modalidade
        Modalidade modalidadeFixture = new Modalidade();
        modalidadeFixture.setId(1L);
        modalidadeFixture.setNome("Futebol");

        // Fixture: EventoModalidade
        eventoModalidadeFixture = new EventoModalidade();
        eventoModalidadeFixture.setId(1L);
        eventoModalidadeFixture.setEvento(eventoFixture);
        eventoModalidadeFixture.setModalidade(modalidadeFixture);

        //Fixture: Time (Necessário para a Inscrição)
        timeFixture = new Time();
        timeFixture.setId(1L);
        timeFixture.setNome("Time do João");
        // Você pode adicionar o participante ao time aqui se o modelo 'Time' permitir
        // ex: timeFixture.setMembros(new HashSet<>(Arrays.asList(participante)));
        // ex: timeFixture.setCapitao(participante);

        // Fixture: Inscricao
        inscricaoFixture = new Inscricao();
        inscricaoFixture.setId(1L);
        inscricaoFixture.setTime(timeFixture);
        inscricaoFixture.setEventoModalidade(eventoModalidadeFixture);
        inscricaoFixture.setStatusInscricao(StatusInscricao.PENDENTE);
    }

    @Test
    void listarInscricoesPendentes_comEventoValido_retornaLista() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(gestorFixture);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(eventoFixture));
        when(inscricaoRepository.findAllByEventoModalidade_Evento_IdAndStatusInscricao(1L, StatusInscricao.PENDENTE))
            .thenReturn(Arrays.asList(inscricaoFixture));

        // Act
        List<InscricaoResponseDTO> result = inscricaoService.listPendentesByEvento(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(inscricaoRepository, times(1))
            .findAllByEventoModalidade_Evento_IdAndStatusInscricao(1L, StatusInscricao.PENDENTE);
    }

    @Test
    void listarInscricoesPendentes_usuarioNaoEhGestor_lancaBusinessRuleException() {
        // Arrange
        User outroUsuario = new User();
        outroUsuario.setId(2L);
        outroUsuario.setNome("Outro Usuário");

        when(authentication.getPrincipal()).thenReturn(outroUsuario);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(eventoFixture));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            inscricaoService.listPendentesByEvento(1L);
        });
    }

    @Test
    void listarInscricoesPendentes_eventoInvalido_lancaResourceNotFoundException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(gestorFixture);
        when(eventoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            inscricaoService.listPendentesByEvento(999L);
        });
    }

    @Test
    void aprovarInscricao_comInscricaoPendente_atualizaParaAprovada() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(gestorFixture);
        when(inscricaoRepository.findById(1L)).thenReturn(Optional.of(inscricaoFixture));
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(eventoFixture));
        when(inscricaoRepository.save(any(Inscricao.class))).thenReturn(inscricaoFixture);

        // Act
        InscricaoResponseDTO result = inscricaoService.aprovarInscricao(1L);

        // Assert
        assertNotNull(result);
        verify(inscricaoRepository, times(1)).save(any(Inscricao.class));
    }

    @Test
    void aprovarInscricao_comInscricaoJaAprovada_lancaBusinessRuleException() {
        // Arrange
        inscricaoFixture.setStatusInscricao(StatusInscricao.APROVADA);
        when(authentication.getPrincipal()).thenReturn(gestorFixture);
        when(inscricaoRepository.findById(1L)).thenReturn(Optional.of(inscricaoFixture));
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(eventoFixture));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            inscricaoService.aprovarInscricao(1L);
        });
        verify(inscricaoRepository, never()).save(any(Inscricao.class));
    }

    @Test
    void rejeitarInscricao_comInscricaoPendente_atualizaParaRejeitada() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(gestorFixture);
        when(inscricaoRepository.findById(1L)).thenReturn(Optional.of(inscricaoFixture));
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(eventoFixture));
        when(inscricaoRepository.save(any(Inscricao.class))).thenReturn(inscricaoFixture);

        // Act
        InscricaoResponseDTO result = inscricaoService.rejeitarInscricao(1L);

        // Assert
        assertNotNull(result);
        verify(inscricaoRepository, times(1)).save(any(Inscricao.class));
    }

    @Test
    void rejeitarInscricao_usuarioNaoEhGestor_lancaBusinessRuleException() {
        // Arrange
        User outroUsuario = new User();
        outroUsuario.setId(2L);

        when(authentication.getPrincipal()).thenReturn(outroUsuario);
        when(inscricaoRepository.findById(1L)).thenReturn(Optional.of(inscricaoFixture));
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(eventoFixture));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            inscricaoService.rejeitarInscricao(1L);
        });
    }

    @Test
    void aprovarInscricao_comInscricaoInvalida_lancaResourceNotFoundException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(gestorFixture);
        when(inscricaoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            inscricaoService.aprovarInscricao(999L);
        });
    }
}

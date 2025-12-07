package br.ifsp.events.service.impl;

// IMPORTS ADICIONADOS E CORRIGIDOS
import br.ifsp.events.dto.event.EventRequestDTO;
import br.ifsp.events.dto.event.EventResponseDTO;
import br.ifsp.events.dto.event.EventoModalidadeRequestDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.*;
import br.ifsp.events.repository.EventoModalidadeRepository;
import br.ifsp.events.repository.EventoRepository;
import br.ifsp.events.repository.ModalidadeRepository;
import br.ifsp.events.repository.TimeRepository;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.NotificationService; // Adicionado
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
import java.util.ArrayList; // Adicionado
import java.util.HashSet;
import java.util.Optional;
import java.util.Set; // Adicionado

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient; // Adicionado

/**
 * Testes unitários para {@link EventServiceImpl}.
 * Cobre criação, atualização e gerenciamento de eventos e modalidades.
 */
@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private ModalidadeRepository modalidadeRepository;

    @Mock // CORREÇÃO 1: Adicionado Mock Faltante
    private EventoModalidadeRepository eventoModalidadeRepository;
    
    @Mock // CORREÇÃO 2: Adicionado Mock Faltante
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EventServiceImpl eventService;

    private User organizadorFixture;
    private Evento eventoFixture;
    private Modalidade modalidadeFixture;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        // CORREÇÃO 3: Adicionado lenient() para evitar UnnecessaryStubbingException
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);

        // Fixture: User (Organizador)
        organizadorFixture = new User();
        organizadorFixture.setId(1L);
        organizadorFixture.setNome("João Organizador");
        organizadorFixture.setEmail("joao@aluno.ifsp.edu.br");

        // Fixture: Modalidade
        modalidadeFixture = new Modalidade();
        modalidadeFixture.setId(1L);
        modalidadeFixture.setNome("Futebol");

        // Fixture: Evento
        eventoFixture = new Evento();
        eventoFixture.setId(1L);
        eventoFixture.setNome("Campeonato de Futebol");
        eventoFixture.setDescricao("Um grande campeonato");
        eventoFixture.setDataInicio(LocalDate.now().plusDays(10));
        eventoFixture.setDataFim(LocalDate.now().plusDays(15));
        eventoFixture.setOrganizador(organizadorFixture);
        eventoFixture.setStatus(StatusEvento.PLANEJADO);
        eventoFixture.setEventoModalidades(new HashSet<>());
    }

    @Test
    void criarEvento_comDadosValidos_retornaDTO() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("joao@aluno.ifsp.edu.br");
        when(userRepository.findByEmail("joao@aluno.ifsp.edu.br")).thenReturn(Optional.of(organizadorFixture));
        when(modalidadeRepository.findById(1L)).thenReturn(Optional.of(modalidadeFixture));
        when(eventoRepository.save(any(Evento.class))).thenReturn(eventoFixture);
        
        // Mock para o saveAll que é chamado internamente
        when(eventoModalidadeRepository.saveAll(any())).thenReturn(new ArrayList<>());

        // DTO da Modalidade (necessário para o Set)
        EventoModalidadeRequestDTO modalidadeDTO = new EventoModalidadeRequestDTO();
        modalidadeDTO.setModalidadeId(1L); // ID 1, que corresponde ao mock 'modalidadeRepository.findById(1L)'
        // CORREÇÃO 4: Usando um valor válido do Enum
        modalidadeDTO.setFormatoEventoModalidade(FormatoEventoModalidade.MATA_MATA); 
        modalidadeDTO.setDataFimInscricao(LocalDate.now().plusDays(5));
        modalidadeDTO.setMaxTimes(8);
        
        EventRequestDTO requestDTO = new EventRequestDTO();
        requestDTO.setNome("Campeonato de Futebol");
        requestDTO.setDescricao("Um grande campeonato");
        requestDTO.setDataInicio(LocalDate.now().plusDays(10));
        requestDTO.setDataFim(LocalDate.now().plusDays(15));
        
        // CORREÇÃO 5: Nome do método é 'setModalidades' e passamos um Set com o DTO
        requestDTO.setModalidades(Set.of(modalidadeDTO));

        // Act
        EventResponseDTO result = eventService.create(requestDTO);

        // Assert
        assertNotNull(result);
        verify(eventoRepository, times(1)).save(any(Evento.class));
        verify(eventoModalidadeRepository, times(1)).saveAll(any());
        verify(notificationService, times(1)).notifyEventCreated(any(Evento.class));
    }

    @Test
    void criarEvento_comDataInvalida_lancaBusinessRuleException() {
        // Arrange
        
        // CORREÇÃO 6: Adicionado lenient() pois a lógica falha antes de usar os mocks
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("joao@aluno.ifsp.edu.br");
        lenient().when(userRepository.findByEmail("joao@aluno.ifsp.edu.br")).thenReturn(Optional.of(organizadorFixture));

        EventRequestDTO requestDTO = new EventRequestDTO();
        requestDTO.setNome("Campeonato de Futebol");
        requestDTO.setDataInicio(LocalDate.now().plusDays(15)); // Data início após fim
        requestDTO.setDataFim(LocalDate.now().plusDays(10));

        // CORREÇÃO 5 (Repetida): Nome do método é 'setModalidades'
        requestDTO.setModalidades(new HashSet<EventoModalidadeRequestDTO>());

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            eventService.create(requestDTO);
        });
        verify(eventoRepository, never()).save(any(Evento.class));
    }

    @Test
    void criarEvento_organizadorNaoEncontrado_lancaBusinessRuleException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("desconhecido@test.com");
        when(userRepository.findByEmail("desconhecido@test.com")).thenReturn(Optional.empty());

        EventRequestDTO requestDTO = new EventRequestDTO();
        requestDTO.setNome("Campeonato de Futebol");
        requestDTO.setDataInicio(LocalDate.now().plusDays(10));
        requestDTO.setDataFim(LocalDate.now().plusDays(15));
        
        // CORREÇÃO 5 (Repetida): Nome do método é 'setModalidades'
        requestDTO.setModalidades(new HashSet<EventoModalidadeRequestDTO>());

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            eventService.create(requestDTO);
        });
    }

    @Test
    void buscarEventoPorId_comIdValido_retornaDTO() {
        // Arrange
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(eventoFixture));

        // Act
        EventResponseDTO result = eventService.findById(1L);

        // Assert
        assertNotNull(result);
        verify(eventoRepository, times(1)).findById(1L);
    }

    @Test
    void buscarEventoPorId_comIdInvalido_lancaResourceNotFoundException() {
        // Arrange
        when(eventoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            eventService.findById(999L);
        });
    }
}
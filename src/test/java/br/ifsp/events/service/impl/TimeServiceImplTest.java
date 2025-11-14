package br.ifsp.events.service.impl;

import br.ifsp.events.dto.time.TimeCreateDTO;
import br.ifsp.events.dto.time.TimeResponseDTO;
import br.ifsp.events.dto.time.TimeUpdateDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.Modalidade;
import br.ifsp.events.model.Time;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.ConviteRepository;
import br.ifsp.events.repository.ModalidadeRepository;
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

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link TimeServiceImpl}.
 * Cobre criação, atualização e gerenciamento de times com validações.
 */
@ExtendWith(MockitoExtension.class)
class TimeServiceImplTest {

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModalidadeRepository modalidadeRepository;

    @Mock
    private ConviteRepository conviteRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TimeServiceImpl timeService;

    private User capitaoFixture;
    private Modalidade modalidadeFixture;
    private Time timeFixture;
    private TimeCreateDTO createDTOFixture;
    private TimeResponseDTO responseDTOFixture;

    @BeforeEach
    void setUp() {
        // Fixture: User (Capitão)
        capitaoFixture = new User();
        capitaoFixture.setId(1L);
        capitaoFixture.setNome("João Capitão");
        capitaoFixture.setEmail("joao@test.com");

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
        timeFixture.getMembros().add(capitaoFixture);

        // Fixture: CreateDTO
        createDTOFixture = new TimeCreateDTO();
        createDTOFixture.setNome("Time A");
        createDTOFixture.setModalidadeId(1L);

        // Fixture: ResponseDTO
        responseDTOFixture = new TimeResponseDTO();
        responseDTOFixture.setId(1L);
        responseDTOFixture.setNome("Time A");
    }

    @Test
    void criarTime_comDadosValidos_retornaDTO() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(capitaoFixture);
        when(userRepository.findById(1L)).thenReturn(Optional.of(capitaoFixture));
        when(modalidadeRepository.findById(1L)).thenReturn(Optional.of(modalidadeFixture));
        when(timeRepository.save(any(Time.class))).thenReturn(timeFixture);
        when(modelMapper.map(timeFixture, TimeResponseDTO.class)).thenReturn(responseDTOFixture);

        // Act
        TimeResponseDTO result = timeService.createTime(createDTOFixture, authentication);

        // Assert
        assertNotNull(result);
        assertEquals("Time A", result.getNome());
        verify(timeRepository, times(1)).save(any(Time.class));
    }

    @Test
    void criarTime_comModalidadeInvalida_lancaResourceNotFoundException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(capitaoFixture);
        when(userRepository.findById(1L)).thenReturn(Optional.of(capitaoFixture));
        when(modalidadeRepository.findById(999L)).thenReturn(Optional.empty());

        createDTOFixture.setModalidadeId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            timeService.createTime(createDTOFixture, authentication);
        });
        verify(timeRepository, never()).save(any(Time.class));
    }

    @Test
    void atualizarTime_comDadosValidos_retornaDTO() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(capitaoFixture);
        when(timeRepository.findById(1L)).thenReturn(Optional.of(timeFixture));
        when(timeRepository.save(any(Time.class))).thenReturn(timeFixture);
        when(modelMapper.map(timeFixture, TimeResponseDTO.class)).thenReturn(responseDTOFixture);

        TimeUpdateDTO updateDTO = new TimeUpdateDTO();
        updateDTO.setNome("Time A Atualizado");

        // Act
        TimeResponseDTO result = timeService.updateTime(1L, updateDTO, authentication);

        // Assert
        assertNotNull(result);
        verify(timeRepository, times(1)).save(any(Time.class));
    }

    @Test
    void atualizarTime_naoCapitao_lancaBusinessRuleException() {
        // Arrange
        User outroUsuario = new User();
        outroUsuario.setId(2L);
        outroUsuario.setNome("Outro Usuário");

        when(authentication.getPrincipal()).thenReturn(outroUsuario);
        when(timeRepository.findById(1L)).thenReturn(Optional.of(timeFixture));

        TimeUpdateDTO updateDTO = new TimeUpdateDTO();
        updateDTO.setNome("Team Updated");

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            timeService.updateTime(1L, updateDTO, authentication);
        });
    }

    @Test
    void buscarTimePorId_comIdValido_retornaDTO() {
        // Arrange
        when(timeRepository.findById(1L)).thenReturn(Optional.of(timeFixture));
        when(modelMapper.map(timeFixture, TimeResponseDTO.class)).thenReturn(responseDTOFixture);

        // Act
        TimeResponseDTO result = timeService.getTimeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Time A", result.getNome());
        verify(timeRepository, times(1)).findById(1L);
    }

    @Test
    void buscarTimePorId_comIdInvalido_lancaResourceNotFoundException() {
        // Arrange
        when(timeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            timeService.getTimeById(999L);
        });
    }
}

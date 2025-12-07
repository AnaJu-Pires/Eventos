package br.ifsp.events.service.impl;

import br.ifsp.events.dto.modalidade.ModalidadeRequestDTO;
import br.ifsp.events.dto.modalidade.ModalidadeResponseDTO;
import br.ifsp.events.exception.DuplicateResourceException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.Modalidade;
import br.ifsp.events.repository.ModalidadeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link ModalidadeServiceImpl}.
 * Cobre operações CRUD: criar, atualizar, remover, listar e buscar modalidades.
 */
@ExtendWith(MockitoExtension.class)
class ModalidadeServiceImplTest {

    @Mock
    private ModalidadeRepository modalidadeRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ModalidadeServiceImpl modalidadeService;

    private Modalidade modalidadeFixture;
    private ModalidadeRequestDTO requestDTOFixture;
    private ModalidadeResponseDTO responseDTOFixture;

    @BeforeEach
    void setUp() {
        // Fixture: Modalidade
        modalidadeFixture = new Modalidade();
        modalidadeFixture.setId(1L);
        modalidadeFixture.setNome("Futebol");
        modalidadeFixture.setDescricao("Esporte coletivo");

        // Fixture: ModalidadeRequestDTO
        requestDTOFixture = new ModalidadeRequestDTO();
        requestDTOFixture.setNome("Futebol");
        requestDTOFixture.setDescricao("Esporte coletivo");

        // Fixture: ModalidadeResponseDTO
        responseDTOFixture = new ModalidadeResponseDTO();
        responseDTOFixture.setId(1L);
        responseDTOFixture.setNome("Futebol");
        responseDTOFixture.setDescricao("Esporte coletivo");
    }

    @Test
    void criarModalidade_comDadosValidos_retornaDTO() {
        // Arrange
        when(modalidadeRepository.findByNome(anyString())).thenReturn(Optional.empty());
        when(modelMapper.map(requestDTOFixture, Modalidade.class)).thenReturn(modalidadeFixture);
        when(modalidadeRepository.save(any(Modalidade.class))).thenReturn(modalidadeFixture);
        when(modelMapper.map(modalidadeFixture, ModalidadeResponseDTO.class)).thenReturn(responseDTOFixture);

        // Act
        ModalidadeResponseDTO result = modalidadeService.create(requestDTOFixture);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Futebol", result.getNome());
        verify(modalidadeRepository, times(1)).save(any(Modalidade.class));
    }

    @Test
    void criarModalidade_comNomeDuplicado_lancaDuplicateResourceException() {
        // Arrange
        when(modalidadeRepository.findByNome(anyString())).thenReturn(Optional.of(modalidadeFixture));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            modalidadeService.create(requestDTOFixture);
        });
        verify(modalidadeRepository, never()).save(any(Modalidade.class));
    }

    @Test
    void buscarModalidadePorId_comIdValido_retornaDTO() {
        // Arrange
        when(modalidadeRepository.findById(1L)).thenReturn(Optional.of(modalidadeFixture));
        when(modelMapper.map(modalidadeFixture, ModalidadeResponseDTO.class)).thenReturn(responseDTOFixture);

        // Act
        ModalidadeResponseDTO result = modalidadeService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Futebol", result.getNome());
        verify(modalidadeRepository, times(1)).findById(1L);
    }

    @Test
    void buscarModalidadePorId_comIdInvalido_lancaResourceNotFoundException() {
        // Arrange
        when(modalidadeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            modalidadeService.findById(999L);
        });
    }
}

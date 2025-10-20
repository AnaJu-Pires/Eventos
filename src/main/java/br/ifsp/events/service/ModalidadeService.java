package br.ifsp.events.service;

import br.ifsp.events.dto.modalidade.ModalidadePatchRequestDTO;
import br.ifsp.events.dto.modalidade.ModalidadeRequestDTO;
import br.ifsp.events.dto.modalidade.ModalidadeResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.DuplicateResourceException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.Modalidade;
import br.ifsp.events.repository.ModalidadeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModalidadeService {

    @Autowired
    private ModalidadeRepository modalidadeRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public ModalidadeResponseDTO create(ModalidadeRequestDTO requestDTO) {
        if (modalidadeRepository.findByNome(requestDTO.getNome()).isPresent()) {
            throw new DuplicateResourceException("Já existe uma modalidade com o nome: " + requestDTO.getNome());
        }
        
        Modalidade modalidade = modelMapper.map(requestDTO, Modalidade.class);
        Modalidade savedModalidade = modalidadeRepository.save(modalidade);
        return modelMapper.map(savedModalidade, ModalidadeResponseDTO.class);
    }

    @Transactional
    public ModalidadeResponseDTO update(Long id, ModalidadeRequestDTO requestDTO) {
        Modalidade modalidade = findModalidadeById(id);

        modalidadeRepository.findByNome(requestDTO.getNome()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("O nome '" + requestDTO.getNome() + "' já está em uso por outra modalidade.");
            }
        });

        modalidade.setNome(requestDTO.getNome());
        modalidade.setDescricao(requestDTO.getDescricao());
        
        Modalidade updatedModalidade = modalidadeRepository.save(modalidade);
        return modelMapper.map(updatedModalidade, ModalidadeResponseDTO.class);
    }
    
    @Transactional
    public ModalidadeResponseDTO patch(Long id, ModalidadePatchRequestDTO requestDTO) {
        Modalidade modalidade = findModalidadeById(id);

        if (requestDTO.getNome() != null) {
            modalidadeRepository.findByNome(requestDTO.getNome()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new DuplicateResourceException("O nome '" + requestDTO.getNome() + "' já está em uso por outra modalidade.");
                }
            });
            modalidade.setNome(requestDTO.getNome());
        }
        
        // Esta verificação corrige o problema da descrição nula
        if (requestDTO.getDescricao() != null) {
            modalidade.setDescricao(requestDTO.getDescricao());
        }
        
        Modalidade patchedModalidade = modalidadeRepository.save(modalidade);
        return modelMapper.map(patchedModalidade, ModalidadeResponseDTO.class);
    }

    @Transactional
    public void delete(Long id) {
        Modalidade modalidade = findModalidadeById(id);

        // REGRA DE NEGÓCIO: Não permitir exclusão se a modalidade estiver vinculada a eventos
        if (modalidade.getEventos() != null && !modalidade.getEventos().isEmpty()) {
            throw new BusinessRuleException("Não é possível excluir uma modalidade que está associada a um ou mais eventos.");
        }
        
        modalidadeRepository.delete(modalidade);
    }

    @Transactional(readOnly = true)
    public List<ModalidadeResponseDTO> findAll() {
        return modalidadeRepository.findAll().stream()
                .map(modalidade -> modelMapper.map(modalidade, ModalidadeResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ModalidadeResponseDTO findById(Long id) {
        Modalidade modalidade = findModalidadeById(id);
        return modelMapper.map(modalidade, ModalidadeResponseDTO.class);
    }

    // Método privado para evitar repetição de código
    private Modalidade findModalidadeById(Long id) {
        return modalidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modalidade não encontrada com o id: " + id));
    }
}
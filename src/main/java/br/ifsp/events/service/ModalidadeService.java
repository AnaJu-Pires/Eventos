package br.ifsp.events.service;

import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.dto.modalidade.ModalidadeDTO;
import br.ifsp.events.exception.DuplicateResourceException;
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
    public ModalidadeDTO create(ModalidadeDTO modalidadeDTO) {

        if (modalidadeRepository.findByNome(modalidadeDTO.getNome()).isPresent()) {
            throw new DuplicateResourceException("Já existe uma modalidade com o nome: " + modalidadeDTO.getNome());
        }
        
        Modalidade modalidade = convertToEntity(modalidadeDTO);
        Modalidade savedModalidade = modalidadeRepository.save(modalidade);
        return convertToDto(savedModalidade);
    }

    @Transactional
    public ModalidadeDTO update(Long id, ModalidadeDTO modalidadeDTO) {
        Modalidade modalidade = modalidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modalidade não encontrada com o id: " + id));

        // Verifica duplicidade se o nome for alterado
        modalidadeRepository.findByNome(modalidadeDTO.getNome()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("O nome '" + modalidadeDTO.getNome() + "' já está em uso por outra modalidade.");
            }
        });

        modalidade.setNome(modalidadeDTO.getNome());
        modalidade.setDescricao(modalidadeDTO.getDescricao()); // Atualiza a descrição
        
        Modalidade updatedModalidade = modalidadeRepository.save(modalidade);
        return convertToDto(updatedModalidade);
    }
    
    @Transactional
    public ModalidadeDTO patch(Long id, ModalidadeDTO modalidadeDTO) {
        Modalidade modalidade = modalidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modalidade não encontrada com o id: " + id));

        if (modalidadeDTO.getNome() != null) {
            // Verifica duplicidade ao tentar alterar o nome
            modalidadeRepository.findByNome(modalidadeDTO.getNome()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new DuplicateResourceException("O nome '" + modalidadeDTO.getNome() + "' já está em uso por outra modalidade.");
                }
            });
            modalidade.setNome(modalidadeDTO.getNome());
        }
        
        if (modalidadeDTO.getDescricao() != null) {
            modalidade.setDescricao(modalidadeDTO.getDescricao()); // Atualiza a descrição
        }
        
        Modalidade patchedModalidade = modalidadeRepository.save(modalidade);
        return convertToDto(patchedModalidade);
    }

    // --- Métodos que não precisam de grandes alterações ---

    @Transactional(readOnly = true)
    public List<ModalidadeDTO> findAll() {
        return modalidadeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ModalidadeDTO findById(Long id) {
        Modalidade modalidade = modalidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modalidade não encontrada com o id: " + id));
        return convertToDto(modalidade);
    }

    @Transactional
    public void delete(Long id) {
        if (!modalidadeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Modalidade não encontrada com o id: " + id);
        }
        // ATENÇÃO: Se a modalidade estiver associada a um evento ou interesse de usuário,
        // a exclusão pode falhar com uma ConstraintViolationException.
        // O tratamento adequado (ex: remover associações primeiro) pode ser necessário.
        modalidadeRepository.deleteById(id);
    }
    
    // --- Métodos de conversão atualizados ---
    
    private ModalidadeDTO convertToDto(Modalidade modalidade) {
        return modelMapper.map(modalidade, ModalidadeDTO.class);
    }

    private Modalidade convertToEntity(ModalidadeDTO modalidadeDTO) {
        return modelMapper.map(modalidadeDTO, Modalidade.class);
    }
}
package br.ifsp.events.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.events.dto.modalidade.ModalidadePatchRequestDTO;
import br.ifsp.events.dto.modalidade.ModalidadeRequestDTO;
import br.ifsp.events.dto.modalidade.ModalidadeResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.DuplicateResourceException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.Modalidade;
import br.ifsp.events.repository.ModalidadeRepository;
import br.ifsp.events.service.ModalidadeService;

@Service
public class ModalidadeServiceImpl implements ModalidadeService {

    @Autowired
    private ModalidadeRepository modalidadeRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public ModalidadeResponseDTO create(ModalidadeRequestDTO requestDTO) {
        if (modalidadeRepository.findByNome(requestDTO.getNome()).isPresent()) {
            throw new DuplicateResourceException("Já existe uma modalidade com o nome: " + requestDTO.getNome());
        }

        Modalidade modalidade = modelMapper.map(requestDTO, Modalidade.class);
        Modalidade saved = modalidadeRepository.save(modalidade);
        return modelMapper.map(saved, ModalidadeResponseDTO.class);
    }

    @Override
    @Transactional
    public ModalidadeResponseDTO update(Long id, ModalidadeRequestDTO requestDTO) {
        Modalidade modalidade = findModalidadeById(id);

        modalidadeRepository.findByNome(requestDTO.getNome()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("O nome '" + requestDTO.getNome() + "' já está em uso.");
            }
        });

        modalidade.setNome(requestDTO.getNome());
        modalidade.setDescricao(requestDTO.getDescricao());

        Modalidade updated = modalidadeRepository.save(modalidade);
        return modelMapper.map(updated, ModalidadeResponseDTO.class);
    }

    @Override
    @Transactional
    public ModalidadeResponseDTO patch(Long id, ModalidadePatchRequestDTO requestDTO) {
        Modalidade modalidade = findModalidadeById(id);

        if (requestDTO.getNome() != null) {
            modalidadeRepository.findByNome(requestDTO.getNome()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new DuplicateResourceException("O nome '" + requestDTO.getNome() + "' já está em uso.");
                }
            });
            modalidade.setNome(requestDTO.getNome());
        }

        if (requestDTO.getDescricao() != null) {
            modalidade.setDescricao(requestDTO.getDescricao());
        }

        Modalidade patched = modalidadeRepository.save(modalidade);
        return modelMapper.map(patched, ModalidadeResponseDTO.class);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Modalidade modalidade = findModalidadeById(id);

        if (modalidade.getEventoModalidades() != null && !modalidade.getEventoModalidades().isEmpty()) {
            throw new BusinessRuleException("Não é possível excluir uma modalidade vinculada a eventos.");
        }

        modalidadeRepository.delete(modalidade);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModalidadeResponseDTO> findAll() {
        return modalidadeRepository.findAll().stream()
                .map(m -> modelMapper.map(m, ModalidadeResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ModalidadeResponseDTO findById(Long id) {
        Modalidade modalidade = findModalidadeById(id);
        return modelMapper.map(modalidade, ModalidadeResponseDTO.class);
    }

    private Modalidade findModalidadeById(Long id) {
        return modalidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modalidade não encontrada com o id: " + id));
    }
}

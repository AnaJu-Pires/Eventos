package br.ifsp.events.service.impl;

import br.ifsp.events.dto.modalidade.ModalidadeResponseDTO;
import br.ifsp.events.dto.time.CapitaoTransferDTO;
import br.ifsp.events.dto.time.TimeCreateDTO;
import br.ifsp.events.dto.time.TimeUpdateDTO;
import br.ifsp.events.dto.time.TimeResponseDTO;
import br.ifsp.events.dto.user.UserResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.Modalidade;
import br.ifsp.events.model.Time;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.ModalidadeRepository;
import br.ifsp.events.repository.TimeRepository;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.TimeService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class TimeServiceImpl implements TimeService {

    private final TimeRepository timeRepository;
    private final UserRepository userRepository;
    private final ModalidadeRepository modalidadeRepository;

    public TimeServiceImpl(TimeRepository timeRepository, UserRepository userRepository, ModalidadeRepository modalidadeRepository) {
        this.timeRepository = timeRepository;
        this.userRepository = userRepository;
        this.modalidadeRepository = modalidadeRepository;
    }

    @Override
    public TimeResponseDTO createTime(TimeCreateDTO createDTO, Authentication auth) {
        User capitao = (User) auth.getPrincipal();
        Modalidade modalidade = modalidadeRepository.findById(createDTO.getModalidadeId())
                .orElseThrow(() -> new ResourceNotFoundException("Modalidade com ID " + createDTO.getModalidadeId() + " não encontrada."));
        Time newTime = new Time();
        newTime.setNome(createDTO.getNome());
        newTime.setCapitao(capitao);
        newTime.setModalidade(modalidade);
        Time savedTime = timeRepository.save(newTime);
        return toResponseDTO(savedTime);
    }

    @Override
    public TimeResponseDTO updateTime(Long timeId, TimeUpdateDTO updateDTO, Authentication auth) {
        User loggedInUser = (User) auth.getPrincipal();
        Time time = findTimeAndCheckCaptaincy(timeId, loggedInUser.getId());

        time.setNome(updateDTO.getNome());
        Time updatedTime = timeRepository.save(time);

        return toResponseDTO(updatedTime);
    }

    @Override
    public TimeResponseDTO transferCaptaincy(Long timeId, CapitaoTransferDTO transferDTO, Authentication auth) {
        User loggedInUser = (User) auth.getPrincipal();
        Time time = findTimeAndCheckCaptaincy(timeId, loggedInUser.getId());

        User newCapitao = userRepository.findById(transferDTO.getNovoCapitaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + transferDTO.getNovoCapitaoId() + " não encontrado."));

        if (loggedInUser.getId().equals(newCapitao.getId())) {
            throw new BusinessRuleException("Você já é o capitão deste time.");
        }

        time.setCapitao(newCapitao);
        Time updatedTime = timeRepository.save(time);

        return toResponseDTO(updatedTime);
    }

    @Override
    public void deleteTime(Long timeId, Authentication auth) {
        User loggedInUser = (User) auth.getPrincipal();
        findTimeAndCheckCaptaincy(timeId, loggedInUser.getId());
        
        timeRepository.deleteById(timeId);
    }

    @Override
    public TimeResponseDTO getTimeById(Long timeId) {
        Time time = timeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("Time com ID " + timeId + " não encontrado."));
        return toResponseDTO(time);
    }

    private Time findTimeAndCheckCaptaincy(Long timeId, Long userId) {
        Time time = timeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("Time com ID " + timeId + " não encontrado."));
        if (!time.getCapitao().getId().equals(userId)) {
            throw new BusinessRuleException("Acesso negado. Apenas o capitão pode modificar este time.");
        }
        return time;
    }

    private TimeResponseDTO toResponseDTO(Time time) {
        TimeResponseDTO dto = new TimeResponseDTO();
        dto.setId(time.getId());
        dto.setNome(time.getNome());
        dto.setCapitao(toUserResponseDTO(time.getCapitao()));
        dto.setModalidade(toModalidadeResponseDTO(time.getModalidade()));
        return dto;
    }

    private UserResponseDTO toUserResponseDTO(User user) {
        return new UserResponseDTO(
            user.getId(),
            user.getNome(),
            user.getEmail(),
            user.getPerfilUser()
        );
    }

    private ModalidadeResponseDTO toModalidadeResponseDTO(Modalidade modalidade) {
        ModalidadeResponseDTO dto = new ModalidadeResponseDTO();
        dto.setId(modalidade.getId());
        dto.setNome(modalidade.getNome());
        dto.setDescricao(modalidade.getDescricao());
        return dto;
    }

    @Override
    public Page<TimeResponseDTO> listAllTimes(Pageable pageable) {
        Page<Time> timePage = timeRepository.findAll(pageable);
        return timePage.map(this::toResponseDTO);
    }
}
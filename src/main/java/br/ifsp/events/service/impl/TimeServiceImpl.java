package br.ifsp.events.service.impl;

import br.ifsp.events.dto.convite.ConviteCreateDTO;
import br.ifsp.events.dto.time.CapitaoTransferDTO;
import br.ifsp.events.dto.time.TimeCreateDTO;
import br.ifsp.events.dto.time.TimeUpdateDTO;
import br.ifsp.events.dto.time.TimeResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.Convite;
import br.ifsp.events.model.Modalidade;
import br.ifsp.events.model.StatusConvite;
import br.ifsp.events.model.Time;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.ConviteRepository;
import br.ifsp.events.repository.ModalidadeRepository;
import br.ifsp.events.repository.TimeRepository;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.TimeService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TimeServiceImpl implements TimeService {

    private final TimeRepository timeRepository;
    private final UserRepository userRepository;
    private final ModalidadeRepository modalidadeRepository;
    private final ConviteRepository conviteRepository;
    private final ModelMapper modelMapper;

    public TimeServiceImpl(TimeRepository timeRepository, UserRepository userRepository, 
                           ModalidadeRepository modalidadeRepository, ConviteRepository conviteRepository,
                           ModelMapper modelMapper) {
        this.timeRepository = timeRepository;
        this.userRepository = userRepository;
        this.modalidadeRepository = modalidadeRepository;
        this.conviteRepository = conviteRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public TimeResponseDTO createTime(TimeCreateDTO createDTO, Authentication auth) {
        User capitaoDoToken = (User) auth.getPrincipal();
        
        User capitao = userRepository.findById(capitaoDoToken.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário do capitão não encontrado."));

        Modalidade modalidade = modalidadeRepository.findById(createDTO.getModalidadeId())
                .orElseThrow(() -> new ResourceNotFoundException("Modalidade..."));
        
        Time newTime = new Time();
        newTime.setNome(createDTO.getNome());
        newTime.setCapitao(capitao);
        newTime.setModalidade(modalidade);
        
        newTime.getMembros().add(capitao); 
        
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

    @Override
    public void convidarMembro(Long timeId, ConviteCreateDTO createDTO, Authentication auth) {
        User capitao = (User) auth.getPrincipal();
        Time time = findTimeAndCheckCaptaincy(timeId, capitao.getId()); 

        User usuarioConvidado = userRepository.findByEmail(createDTO.getEmailUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com e-mail " + createDTO.getEmailUsuario() + " não encontrado."));

        if (capitao.getId().equals(usuarioConvidado.getId())) {
            throw new BusinessRuleException("Você não pode convidar a si mesmo.");
        }

        if (time.getMembros().contains(usuarioConvidado)) {
            throw new BusinessRuleException("Este usuário já é um membro do time.");
        }

        if (conviteRepository.existsByTimeAndUsuarioConvidadoAndStatus(time, usuarioConvidado, StatusConvite.PENDENTE)) {
            throw new BusinessRuleException("Um convite pendente para este usuário já existe.");
        }

        Convite novoConvite = new Convite(time, usuarioConvidado);
        conviteRepository.save(novoConvite);
    }

    @Override
    public void removerMembro(Long timeId, Long userId, Authentication auth) {
        User capitao = (User) auth.getPrincipal();
        Time time = findTimeAndCheckCaptaincy(timeId, capitao.getId());

        User membroParaRemover = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Membro com ID " + userId + " não encontrado."));

        if (capitao.getId().equals(membroParaRemover.getId())) {
            throw new BusinessRuleException("O capitão não pode remover a si mesmo. Transfira a capitania primeiro.");
        }

        if (!time.getMembros().contains(membroParaRemover)) {
            throw new BusinessRuleException("Este usuário não é membro do time.");
        }

        time.getMembros().remove(membroParaRemover);
        timeRepository.save(time);
    }

    private TimeResponseDTO toResponseDTO(Time time) {
        return modelMapper.map(time, TimeResponseDTO.class);
    }

    @Override
    public Page<TimeResponseDTO> listAllTimes(Pageable pageable) {
        Page<Time> timePage = timeRepository.findAll(pageable);
        return timePage.map(this::toResponseDTO);
    }

}
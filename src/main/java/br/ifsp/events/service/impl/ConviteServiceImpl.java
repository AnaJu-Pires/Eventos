package br.ifsp.events.service.impl;

import br.ifsp.events.dto.convite.ConviteResponseDTO;
import br.ifsp.events.dto.time.TimeResponseDTO;
import br.ifsp.events.dto.user.UserResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.*;
import br.ifsp.events.repository.ConviteRepository;
import br.ifsp.events.repository.TimeRepository;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.ConviteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.ifsp.events.dto.modalidade.ModalidadeResponseDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ConviteServiceImpl implements ConviteService {

    private final ConviteRepository conviteRepository;
    private final TimeRepository timeRepository;
    private final UserRepository userRepository;

    public ConviteServiceImpl(ConviteRepository conviteRepository, TimeRepository timeRepository, UserRepository userRepository) {
        this.conviteRepository = conviteRepository;
        this.timeRepository = timeRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public List<ConviteResponseDTO> listarMeusConvites(Authentication auth) {
        User usuarioLogado = (User) auth.getPrincipal();
        List<Convite> convitesPendentes = conviteRepository.findByUsuarioConvidadoAndStatus(usuarioLogado, StatusConvite.PENDENTE);
        
        List<ConviteResponseDTO> convitesValidosDTO = new ArrayList<>();
        List<Convite> convitesParaExpirar = new ArrayList<>();
        LocalDateTime agora = LocalDateTime.now();

        for (Convite convite : convitesPendentes) {
            if (agora.isAfter(convite.getDataExpiracao())) {
                convitesParaExpirar.add(convite);
            } else {
                convitesValidosDTO.add(toConviteResponseDTO(convite));
            }
        }

        if (!convitesParaExpirar.isEmpty()) {
            for (Convite convite : convitesParaExpirar) {
                convite.setStatus(StatusConvite.EXPIRADO);
                conviteRepository.save(convite);
            }
        }
        
        return convitesValidosDTO;
    }

    @Override
    @Transactional
    public void aceitarConvite(Long conviteId, Authentication auth) {
        User usuarioLogado = getManagedUserFromAuth(auth);
        Convite convite = findAndValidateConvite(conviteId, usuarioLogado);

        Time timeParaEntrar = convite.getTime();
        Modalidade modalidadeDoConvite = timeParaEntrar.getModalidade();

        for (Time timeAtual : usuarioLogado.getTimesQueParticipo()) {
            if (timeAtual.getModalidade().equals(modalidadeDoConvite)) {
                throw new BusinessRuleException("Você já faz parte de um time (" + timeAtual.getNome() + ") nesta modalidade (" + modalidadeDoConvite.getNome() + ").");
            }
        }

        timeParaEntrar.getMembros().add(usuarioLogado);
        timeRepository.save(timeParaEntrar);

        convite.setStatus(StatusConvite.ACEITO);
        conviteRepository.save(convite);
    }

    @Override
    @Transactional
    public void recusarConvite(Long conviteId, Authentication auth) {
        User usuarioLogado = getManagedUserFromAuth(auth);
        
        Convite convite = conviteRepository.findByIdAndUsuarioConvidado(conviteId, usuarioLogado)
                .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado ou não pertence a você."));

        if (convite.getStatus() != StatusConvite.PENDENTE) {
            throw new BusinessRuleException("Este convite não está mais pendente.");
        }
        
        conviteRepository.delete(convite);
    }

    private User getManagedUserFromAuth(Authentication auth) {
        User usuarioDoToken = (User) auth.getPrincipal();
        return userRepository.findById(usuarioDoToken.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado no banco de dados."));
    }

    private Convite findAndValidateConvite(Long conviteId, User usuarioLogado) {
        Convite convite = conviteRepository.findByIdAndUsuarioConvidado(conviteId, usuarioLogado)
                .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado ou não pertence a você."));

        if (LocalDateTime.now().isAfter(convite.getDataExpiracao())) {
            convite.setStatus(StatusConvite.EXPIRADO);
            conviteRepository.save(convite);
            throw new BusinessRuleException("Este convite expirou.");
        }

        if (convite.getStatus() != StatusConvite.PENDENTE) {
            throw new BusinessRuleException("Este convite não está mais pendente.");
        }
        
        return convite;
    }



    private ConviteResponseDTO toConviteResponseDTO(Convite convite) {
        ConviteResponseDTO dto = new ConviteResponseDTO();
        dto.setId(convite.getId());
        dto.setStatus(convite.getStatus());
        dto.setDataExpiracao(convite.getDataExpiracao());
        dto.setTime(toTimeResponseDTO(convite.getTime()));
        return dto;
    }

    private TimeResponseDTO toTimeResponseDTO(Time time) {
        TimeResponseDTO dto = new TimeResponseDTO();
        dto.setId(time.getId());
        dto.setNome(time.getNome());
        dto.setCapitao(toUserResponseDTO(time.getCapitao()));
        dto.setModalidade(toModalidadeResponseDTO(time.getModalidade()));

        Set<UserResponseDTO> membrosDTO = time.getMembros().stream()
                .map(this::toUserResponseDTO)
                .collect(Collectors.toSet());
        dto.setMembros(membrosDTO);
        
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
        ModalidadeResponseDTO dto = new ModalidadeResponseDTO(); // <-- SIMPLIFICADO
        dto.setId(modalidade.getId());
        dto.setNome(modalidade.getNome());
        dto.setDescricao(modalidade.getDescricao());
        return dto;
    }
}
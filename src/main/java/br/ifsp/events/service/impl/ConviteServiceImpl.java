package br.ifsp.events.service.impl;

import br.ifsp.events.dto.convite.ConviteResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.*;
import br.ifsp.events.repository.ConviteRepository;
import br.ifsp.events.repository.TimeRepository;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.ConviteService;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;

@Service
public class ConviteServiceImpl implements ConviteService {

    // Adicione o Logger
    private static final Logger logger = LoggerFactory.getLogger(ConviteServiceImpl.class);

    private final ConviteRepository conviteRepository;
    private final TimeRepository timeRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public ConviteServiceImpl(ConviteRepository conviteRepository, TimeRepository timeRepository, 
                              UserRepository userRepository, ModelMapper modelMapper) {
        this.conviteRepository = conviteRepository;
        this.timeRepository = timeRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * MÉTODO ATUALIZADO
     * Agora este método não ATUALIZA mais o banco. Ele apenas LÊ.
     * A lógica de expiração foi movida para o scheduler.
     * Nós ainda filtramos os expirados para o usuário não vê-los.
     */
    @Override
    @Transactional(readOnly = true) // Pode ser readOnly agora
    public List<ConviteResponseDTO> listarMeusConvites(Authentication auth) {
        User usuarioLogado = (User) auth.getPrincipal();
        List<Convite> convitesPendentes = conviteRepository.findByUsuarioConvidadoAndStatus(usuarioLogado, StatusConvite.PENDENTE);
        
        LocalDateTime agora = LocalDateTime.now();

        // Filtra convites que AINDA SÃO VÁLIDOS (expiração > agora)
        // e os mapeia para DTO.
        return convitesPendentes.stream()
                .filter(convite -> agora.isBefore(convite.getDataExpiracao()))
                .map(convite -> modelMapper.map(convite, ConviteResponseDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * NOVO MÉTODO
     * Este método será chamado pelo ConviteScheduler.
     */
    @Override
    @Transactional
    public void expirarConvitesPendentes() {
        LocalDateTime agora = LocalDateTime.now();
        
        List<Convite> convitesParaExpirar = conviteRepository.findByStatusAndDataExpiracaoBefore(StatusConvite.PENDENTE, agora);

        if (convitesParaExpirar.isEmpty()) {
            logger.info("Nenhum convite pendente para expirar.");
            return;
        }

        logger.info("Expirando {} convites pendentes...", convitesParaExpirar.size());
        
        for (Convite convite : convitesParaExpirar) {
            convite.setStatus(StatusConvite.EXPIRADO);
        }

        // Salva todos os convites atualizados em lote
        conviteRepository.saveAll(convitesParaExpirar);
        logger.info("{} convites atualizados para EXPIRADO.", convitesParaExpirar.size());
    }


    @Override
    @Transactional
    public void aceitarConvite(Long conviteId, Authentication auth) {
        // ... (este método não precisa de alteração) ...
        // A validação em findAndValidateConvite já trata o caso de 
        // um convite expirar entre a listagem e a aceitação.
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
        // ... (este método não precisa de alteração) ...
        User usuarioLogado = getManagedUserFromAuth(auth);
        
        Convite convite = conviteRepository.findByIdAndUsuarioConvidado(conviteId, usuarioLogado)
                .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado ou não pertence a você."));

        if (convite.getStatus() != StatusConvite.PENDENTE) {
            throw new BusinessRuleException("Este convite não está mais pendente.");
        }
        
        conviteRepository.delete(convite);
    }

    private User getManagedUserFromAuth(Authentication auth) {
        // ... (este método não precisa de alteração) ...
        User usuarioDoToken = (User) auth.getPrincipal();
        return userRepository.findById(usuarioDoToken.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado no banco de dados."));
    }

    private Convite findAndValidateConvite(Long conviteId, User usuarioLogado) {
        // ... (este método não precisa de alteração, é importante manter esta validação) ...
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
}
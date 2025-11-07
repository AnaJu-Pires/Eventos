package br.ifsp.events.service.impl;

import br.ifsp.events.dto.inscricao.InscricaoResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.*;
import br.ifsp.events.repository.EventoRepository;
import br.ifsp.events.repository.InscricaoRepository;
import br.ifsp.events.service.InscricaoService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class InscricaoServiceImpl implements InscricaoService {

    private final InscricaoRepository inscricaoRepository;
    private final EventoRepository eventoRepository;

    public InscricaoServiceImpl(InscricaoRepository inscricaoRepository, EventoRepository eventoRepository) {
        this.inscricaoRepository = inscricaoRepository;
        this.eventoRepository = eventoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InscricaoResponseDTO> listPendentesByEvento(Long eventoId) {
        User gestor = getAuthenticatedUser();
        Evento evento = findEventoById(eventoId);
        
        checkGestorOwnership(evento, gestor);

        List<Inscricao> inscricoes = inscricaoRepository
                .findAllByEventoModalidade_Evento_IdAndStatusInscricao(eventoId, StatusInscricao.PENDENTE);
        
        return inscricoes.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InscricaoResponseDTO aprovarInscricao(Long inscricaoId) {
        return updateStatus(inscricaoId, StatusInscricao.APROVADA);
    }

    @Override
    @Transactional
    public InscricaoResponseDTO rejeitarInscricao(Long inscricaoId) {
        return updateStatus(inscricaoId, StatusInscricao.REJEITADA);
    }

    private InscricaoResponseDTO updateStatus(Long inscricaoId, StatusInscricao novoStatus) {
        User gestor = getAuthenticatedUser();
        Inscricao inscricao = findInscricaoById(inscricaoId);
        
        checkGestorOwnership(inscricao.getEventoModalidade().getEvento(), gestor);

        if (inscricao.getStatusInscricao() != StatusInscricao.PENDENTE) {
            throw new BusinessRuleException("Esta inscrição não está mais pendente.");
        }

        inscricao.setStatusInscricao(novoStatus);
        Inscricao savedInscricao = inscricaoRepository.save(inscricao);
        
        return toResponseDTO(savedInscricao);
    }

    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Evento findEventoById(Long eventoId) {
        return eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento com ID " + eventoId + " não encontrado."));
    }

    private Inscricao findInscricaoById(Long inscricaoId) {
        return inscricaoRepository.findById(inscricaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscrição com ID " + inscricaoId + " não encontrada."));
    }

    private void checkGestorOwnership(Evento evento, User gestor) {
        if (!Objects.equals(evento.getOrganizador().getId(), gestor.getId())) {
            throw new BusinessRuleException("Ação não permitida. Você não é o gestor deste evento.");
        }
    }

    private InscricaoResponseDTO toResponseDTO(Inscricao inscricao) {
        Time time = inscricao.getTime();
        EventoModalidade em = inscricao.getEventoModalidade();

        return InscricaoResponseDTO.builder()
                .id(inscricao.getId())
                .nomeTime(time.getNome())
                .nomeEvento(em.getEvento().getNome())
                .nomeModalidade(em.getModalidade().getNome())
                .statusInscricao(inscricao.getStatusInscricao())
                .nomeCapitao(time.getCapitao().getNome())
                .nomesJogadores(time.getMembros().stream()
                        .map(User::getNome)
                        .collect(Collectors.toSet()))
                .build();
    }
}
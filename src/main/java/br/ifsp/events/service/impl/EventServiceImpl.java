package br.ifsp.events.service.impl;

import br.ifsp.events.dto.event.EventRequestDTO;
import br.ifsp.events.dto.event.EventResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.model.Evento;
import br.ifsp.events.model.Modalidade;
import br.ifsp.events.model.StatusEvento;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.EventoRepository;
import br.ifsp.events.repository.ModalidadeRepository;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.EventService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventoRepository eventoRepository;
    private final ModalidadeRepository modalidadeRepository;
    private final UserRepository userRepository;

    public EventServiceImpl(EventoRepository eventoRepository, ModalidadeRepository modalidadeRepository, UserRepository userRepository) {
        this.eventoRepository = eventoRepository;
        this.modalidadeRepository = modalidadeRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public EventResponseDTO create(EventRequestDTO eventRequestDTO) {
        if (eventRequestDTO.getDataFim().isBefore(eventRequestDTO.getDataInicio())) {
            throw new BusinessRuleException("A data de fim não pode ser anterior à data de início.");
        }

        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User organizador = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessRuleException("Organizador não encontrado."));

        Set<Modalidade> modalidades = modalidadeRepository.findByIdIn(eventRequestDTO.getModalidadesIds());
        if (modalidades.size() != eventRequestDTO.getModalidadesIds().size()) {
            throw new BusinessRuleException("Uma ou mais modalidades não foram encontradas.");
        }

        Evento evento = new Evento();
        evento.setNome(eventRequestDTO.getNome());
        evento.setDescricao(eventRequestDTO.getDescricao());
        evento.setDataInicio(eventRequestDTO.getDataInicio());
        evento.setDataFim(eventRequestDTO.getDataFim());
        evento.setOrganizador(organizador);
        evento.setModalidades(modalidades);
        evento.setStatus(StatusEvento.PLANEJADO);

        Evento savedEvento = eventoRepository.save(evento);

        return toResponseDTO(savedEvento);
    }

    private EventResponseDTO toResponseDTO(Evento evento) {
        return EventResponseDTO.builder()
                .id(evento.getId())
                .nome(evento.getNome())
                .descricao(evento.getDescricao())
                .dataInicio(evento.getDataInicio())
                .dataFim(evento.getDataFim())
                .status(evento.getStatus())
                .organizadorNome(evento.getOrganizador().getNome())
                .modalidades(evento.getModalidades().stream()
                        .map(Modalidade::getNome)
                        .collect(Collectors.toSet()))
                .build();
    }
}
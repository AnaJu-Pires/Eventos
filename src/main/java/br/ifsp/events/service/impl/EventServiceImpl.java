package br.ifsp.events.service.impl;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.events.dto.event.EventPatchDTO;
import br.ifsp.events.dto.event.EventRequestDTO;
import br.ifsp.events.dto.event.EventResponseDTO;
import br.ifsp.events.dto.event.EventoModalidadePatchDTO;
import br.ifsp.events.dto.event.EventoModalidadeRequestDTO;
import br.ifsp.events.dto.event.EventoModalidadeResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.Evento;
import br.ifsp.events.model.EventoModalidade;
import br.ifsp.events.model.Modalidade;
import br.ifsp.events.model.StatusEvento;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.EventoModalidadeRepository;
import br.ifsp.events.repository.EventoRepository;
import br.ifsp.events.repository.ModalidadeRepository;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.EventService;

@Service
public class EventServiceImpl implements EventService {

    private final EventoRepository eventoRepository;
    private final ModalidadeRepository modalidadeRepository;
    private final EventoModalidadeRepository eventoModalidadeRepository;
    private final UserRepository userRepository;

    public EventServiceImpl(EventoRepository eventoRepository,
                            ModalidadeRepository modalidadeRepository,
                            EventoModalidadeRepository eventoModalidadeRepository,
                            UserRepository userRepository) {
        this.eventoRepository = eventoRepository;
        this.modalidadeRepository = modalidadeRepository;
        this.eventoModalidadeRepository = eventoModalidadeRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public EventResponseDTO create(EventRequestDTO eventRequestDTO) {
        validateDates(eventRequestDTO.getDataInicio(), eventRequestDTO.getDataFim());

        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User organizador = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessRuleException("Organizador não encontrado."));

        Evento evento = new Evento();
        evento.setNome(eventRequestDTO.getNome());
        evento.setDescricao(eventRequestDTO.getDescricao());
        evento.setDataInicio(eventRequestDTO.getDataInicio());
        evento.setDataFim(eventRequestDTO.getDataFim());
        evento.setOrganizador(organizador);
        evento.setStatus(StatusEvento.PLANEJADO);

        Evento savedEvento = eventoRepository.save(evento);

        Set<EventoModalidade> eventoModalidades = buildEventoModalidades(eventRequestDTO.getModalidades(), savedEvento);
        eventoModalidadeRepository.saveAll(eventoModalidades);
        savedEvento.setEventoModalidades(eventoModalidades);

        return toResponseDTO(savedEvento);
    }

    @Override
    @Transactional
    public EventResponseDTO update(Long id, EventRequestDTO eventRequestDTO) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento com ID " + id + " não encontrado."));
        validateOrganizerOwnership(evento);

        if (evento.getStatus() != StatusEvento.PLANEJADO) {
            throw new BusinessRuleException("Só é possível editar eventos com status PLANEJADO.");
        }

        validateDates(eventRequestDTO.getDataInicio(), eventRequestDTO.getDataFim());

        evento.setNome(eventRequestDTO.getNome());
        evento.setDescricao(eventRequestDTO.getDescricao());
        evento.setDataInicio(eventRequestDTO.getDataInicio());
        evento.setDataFim(eventRequestDTO.getDataFim());

        eventoModalidadeRepository.deleteAll(evento.getEventoModalidades());
        Set<EventoModalidade> eventoModalidades = buildEventoModalidades(eventRequestDTO.getModalidades(), evento);
        eventoModalidadeRepository.saveAll(eventoModalidades);
        evento.setEventoModalidades(eventoModalidades);

        Evento updated = eventoRepository.save(evento);
        return toResponseDTO(updated);
    }

    @Override
    @Transactional
    public EventResponseDTO patch(Long id, EventPatchDTO eventPatchDTO) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento com ID " + id + " não encontrado."));
        validateOrganizerOwnership(evento);

        if (evento.getStatus() != StatusEvento.PLANEJADO) {
            throw new BusinessRuleException("Só é possível editar eventos com status PLANEJADO.");
        }

        eventPatchDTO.getNome().ifPresent(evento::setNome);
        eventPatchDTO.getDescricao().ifPresent(evento::setDescricao);

        LocalDate novaDataInicio = eventPatchDTO.getDataInicio().orElse(evento.getDataInicio());
        LocalDate novaDataFim = eventPatchDTO.getDataFim().orElse(evento.getDataFim());
        validateDates(novaDataInicio, novaDataFim);

        evento.setDataInicio(novaDataInicio);
        evento.setDataFim(novaDataFim);

        eventPatchDTO.getModalidades().ifPresent(modalidadesDTO -> {
            Set<EventoModalidade> eventoModalidades = evento.getEventoModalidades();

            for (EventoModalidadePatchDTO dto : modalidadesDTO) {
                Modalidade modalidade = modalidadeRepository.findById(dto.getModalidadeId())
                        .orElseThrow(() -> new BusinessRuleException(
                                "Modalidade com ID " + dto.getModalidadeId() + " não encontrada."));

                EventoModalidade em = eventoModalidades.stream()
                        .filter(existing -> existing.getModalidade().getId().equals(dto.getModalidadeId()))
                        .findFirst()
                        .orElseGet(() -> {
                            EventoModalidade novo = new EventoModalidade();
                            novo.setEvento(evento);
                            novo.setModalidade(modalidade);
                            eventoModalidades.add(novo);
                            return novo;
                        });

                if (dto.getMaxTimes() != null) em.setMaxTimes(dto.getMaxTimes());
                if (dto.getMinJogadoresPorTime() != null) em.setMinJogadoresPorTime(dto.getMinJogadoresPorTime());
                if (dto.getMaxJogadoresPorTime() != null) em.setMaxJogadoresPorTime(dto.getMaxJogadoresPorTime());
                if (dto.getFormatoEventoModalidade() != null) em.setFormatoEventoModalidade(dto.getFormatoEventoModalidade());
            }

            eventoModalidadeRepository.saveAll(eventoModalidades);
            evento.setEventoModalidades(eventoModalidades);
        });

        Evento patched = eventoRepository.save(evento);
        return toResponseDTO(patched);
    }


    @Override
    @Transactional
    public void delete(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento com ID " + id + " não encontrado."));
        validateOrganizerOwnership(evento);

        if (evento.getStatus() == StatusEvento.PLANEJADO) {
            eventoModalidadeRepository.deleteAll(evento.getEventoModalidades());
            eventoRepository.delete(evento);
        } else {
            evento.setStatus(StatusEvento.CANCELADO);
            eventoRepository.save(evento);
        }
    }

    private Set<EventoModalidade> buildEventoModalidades(Set<EventoModalidadeRequestDTO> modalidadesDTO, Evento evento) {
        return modalidadesDTO.stream().map(dto -> {
            Modalidade modalidade = modalidadeRepository.findById(dto.getModalidadeId())
                    .orElseThrow(() -> new BusinessRuleException("Modalidade com ID " + dto.getModalidadeId() + " não encontrada."));
            EventoModalidade em = new EventoModalidade();
            em.setEvento(evento);
            em.setModalidade(modalidade);
            em.setFormatoEventoModalidade(dto.getFormatoEventoModalidade());
            em.setMaxTimes(dto.getMaxTimes());
            em.setMinJogadoresPorTime(dto.getMinJogadoresPorTime());
            em.setMaxJogadoresPorTime(dto.getMaxJogadoresPorTime());
            return em;
        }).collect(Collectors.toSet());
    }

    private EventResponseDTO toResponseDTO(Evento evento) {
        Set<EventoModalidadeResponseDTO> eventoModalidades = evento.getEventoModalidades().stream()
                .map(em -> EventoModalidadeResponseDTO.builder()
                        .id(em.getId())
                        .modalidadeNome(em.getModalidade().getNome())
                        .formatoEventoModalidade(em.getFormatoEventoModalidade())
                        .maxTimes(em.getMaxTimes())
                        .minJogadoresPorTime(em.getMinJogadoresPorTime())
                        .maxJogadoresPorTime(em.getMaxJogadoresPorTime())
                        .build())
                .collect(Collectors.toSet());

        return EventResponseDTO.builder()
                .id(evento.getId())
                .nome(evento.getNome())
                .descricao(evento.getDescricao())
                .dataInicio(evento.getDataInicio())
                .dataFim(evento.getDataFim())
                .status(evento.getStatus())
                .organizadorNome(evento.getOrganizador().getNome())
                .eventoModalidades(eventoModalidades)
                .build();
    }

    private void validateOrganizerOwnership(Evento evento) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User organizadorLogado = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessRuleException("Organizador não encontrado."));

        if (!Objects.equals(evento.getOrganizador().getId(), organizadorLogado.getId())) {
            throw new BusinessRuleException("Apenas o organizador do evento pode modificá-lo.");
        }
    }

    private void validateDates(LocalDate inicio, LocalDate fim) {
        if (fim.isBefore(inicio)) {
            throw new BusinessRuleException("A data de fim não pode ser anterior à data de início.");
        }
    }
}

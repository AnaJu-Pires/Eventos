package br.ifsp.events.service.impl;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.events.dto.event.EventRequestDTO;
import br.ifsp.events.dto.event.EventResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.Evento;
import br.ifsp.events.model.Modalidade;
import br.ifsp.events.model.StatusEvento;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.EventoRepository;
import br.ifsp.events.repository.ModalidadeRepository;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.EventService;

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

    @Override
    @Transactional
    public EventResponseDTO update(Long id, EventRequestDTO eventRequestDTO) {
        // 1. Encontra o evento no banco de dados. Se não existir, lança exceção.
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento com ID " + id + " não encontrado."));

        // 2. REGRA DE NEGÓCIO: Verifica se o evento está no status 'PLANEJADO'.
        if (evento.getStatus() != StatusEvento.PLANEJADO) {
            throw new BusinessRuleException("Só é possível editar eventos que ainda não iniciaram (status PLANEJADO).");
        }

        // 3. Validação de segurança: Verifica se o usuário logado é o organizador do evento.
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User organizadorLogado = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessRuleException("Organizador não encontrado."));

        if (!Objects.equals(evento.getOrganizador().getId(), organizadorLogado.getId())) {
            throw new BusinessRuleException("Apenas o organizador do evento pode editá-lo.");
        }

        // 4. Validação da data
        if (eventRequestDTO.getDataFim().isBefore(eventRequestDTO.getDataInicio())) {
            throw new BusinessRuleException("A data de fim não pode ser anterior à data de início.");
        }

        // 5. Busca as novas modalidades
        Set<Modalidade> modalidades = modalidadeRepository.findByIdIn(eventRequestDTO.getModalidadesIds());
        if (modalidades.size() != eventRequestDTO.getModalidadesIds().size()) {
            throw new BusinessRuleException("Uma ou mais modalidades não foram encontradas.");
        }

        // 6. Atualiza todos os campos do evento
        evento.setNome(eventRequestDTO.getNome());
        evento.setDescricao(eventRequestDTO.getDescricao());
        evento.setDataInicio(eventRequestDTO.getDataInicio());
        evento.setDataFim(eventRequestDTO.getDataFim());
        evento.setModalidades(modalidades);
        // O status e o organizador não mudam em uma atualização PUT

        Evento updatedEvento = eventoRepository.save(evento);

        return toResponseDTO(updatedEvento);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 1. Encontra o evento no banco de dados.
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento com ID " + id + " não encontrado."));

        // 2. Validação de segurança: Apenas o organizador do evento pode deletá-lo ou cancelá-lo.
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User organizadorLogado = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessRuleException("Organizador não encontrado."));

        if (!Objects.equals(evento.getOrganizador().getId(), organizadorLogado.getId())) {
            throw new BusinessRuleException("Apenas o organizador do evento pode removê-lo ou cancelá-lo.");
        }

        // 3. LÓGICA CONDICIONAL: Verifica o status do evento.
        if (evento.getStatus() == StatusEvento.PLANEJADO) {
            // Se o evento ainda não começou, remove fisicamente do banco.
            eventoRepository.delete(evento);
        } else {
            // Se o evento já começou ou terminou, faz uma "deleção lógica" mudando o status.
            evento.setStatus(StatusEvento.CANCELADO);
            eventoRepository.save(evento);
        }
    }
}
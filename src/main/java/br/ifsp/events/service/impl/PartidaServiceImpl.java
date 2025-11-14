package br.ifsp.events.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.events.dto.partida.PartidaResponseDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.model.EventoModalidade;
import br.ifsp.events.model.FormatoEventoModalidade;
import br.ifsp.events.model.Inscricao;
import br.ifsp.events.model.Partida;
import br.ifsp.events.model.StatusInscricao;
import br.ifsp.events.model.StatusPartida;
import br.ifsp.events.model.Time;
import br.ifsp.events.repository.EventoModalidadeRepository;
import br.ifsp.events.repository.InscricaoRepository;
import br.ifsp.events.repository.PartidaRepository;
import br.ifsp.events.service.PartidaService;

/**
 * Implementação do serviço de Partida. Fornece listagem e geração automática
 * de chaves (mata-mata e pontos corridos) conforme o formato definido na
 * entidade EventoModalidade.
 */
@Service
public class PartidaServiceImpl implements PartidaService {

    private static final Logger logger = LoggerFactory.getLogger(PartidaServiceImpl.class);

    private final PartidaRepository partidaRepository;
    private final EventoModalidadeRepository eventoModalidadeRepository;
    private final InscricaoRepository inscricaoRepository;

    public PartidaServiceImpl(PartidaRepository partidaRepository,
            EventoModalidadeRepository eventoModalidadeRepository,
            InscricaoRepository inscricaoRepository) {
        this.partidaRepository = partidaRepository;
        this.eventoModalidadeRepository = eventoModalidadeRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartidaResponseDTO> listByEvento(Long eventoId) {
        List<Partida> partidas = partidaRepository.findAllByEventoModalidade_Evento_Id(eventoId);

        return partidas.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void gerarChaveParaEvento(Long eventoId, FormatoEventoModalidade formato) {
        List<EventoModalidade> modalidades = eventoModalidadeRepository
                .findAllByEvento_IdAndFormatoEventoModalidade(eventoId, formato);
        if (modalidades == null || modalidades.isEmpty()) {
            throw new BusinessRuleException("Não existem modalidades com o formato informado para este evento.");
        }

        LocalDate hoje = LocalDate.now();
        for (EventoModalidade em : modalidades) {
            if (em.getDataFimInscricao() == null) {
                throw new BusinessRuleException("A modalidade " + em.getId() + " não possui dataFimInscricao definida.");
            }
            if (hoje.isBefore(em.getDataFimInscricao())) {
                throw new BusinessRuleException(
                        "Inscrições ainda abertas para modalidade " + em.getId() + ". Feche-as antes de gerar a chave.");
            }

            List<Inscricao> aprovadas = inscricaoRepository.findAllByEventoModalidadeIdAndStatusInscricao(em.getId(),
                    StatusInscricao.APROVADA);
            
            // CORREÇÃO: Filtrar times nulos para evitar NPE
            List<Time> times = aprovadas.stream()
                    .map(Inscricao::getTime)
                    .filter(Objects::nonNull) // Garante que nenhum time é nulo
                    .collect(Collectors.toList());

            int n = times.size(); // N agora é o número de times válidos
            if (n == 0) {
                throw new BusinessRuleException("Não há times aprovados (e válidos) para modalidade " + em.getId());
            }

            List<Partida> existentes = partidaRepository.findAllByEventoModalidade_Id(em.getId());
            if (existentes != null && !existentes.isEmpty()) {
                throw new BusinessRuleException("Já existem partidas geradas para a modalidade " + em.getId());
            }

            // CORREÇÃO: Declarar a lista de partidas ANTES do if/else
            List<Partida> partidas = new ArrayList<>();

            if (formato == FormatoEventoModalidade.MATA_MATA) {
                if (!isPowerOfTwo(n)) {
                    throw new BusinessRuleException(
                            "Número de times aprovados (" + n + ") não é potência de 2. Ajuste inscrições antes de gerar a chave.");
                }

                Collections.shuffle(times);

                for (int i = 0; i < times.size(); i += 2) {
                    Partida p = new Partida();
                    p.setEventoModalidade(em);
                    p.setTime1(times.get(i));
                    p.setTime2(times.get(i + 1));
                    p.setRound(1);
                    p.setStatusPartida(StatusPartida.AGENDADA);
                    partidas.add(p);
                    logger.info("Criada partida (mata-mata): modalidade={}, time1={}, time2={}", 
                        em.getId(), p.getTime1().getId(), p.getTime2().getId());
                }
                partidaRepository.saveAll(partidas);
                logger.info("{} partidas geradas (mata-mata) para modalidade {}", partidas.size(), em.getId());

            } else if (formato == FormatoEventoModalidade.PONTOS_CORRIDOS) {
                
                for (int i = 0; i < times.size(); i++) {
                    for (int j = i + 1; j < times.size(); j++) {
                        Partida p = new Partida();
                        p.setEventoModalidade(em);
                        p.setTime1(times.get(i));
                        p.setTime2(times.get(j));
                        p.setRound(1); 
                        p.setStatusPartida(StatusPartida.AGENDADA);
                        partidas.add(p);
                    }
                }
                partidaRepository.saveAll(partidas);
                logger.info("{} partidas geradas (pontos corridos) para modalidade {}", partidas.size(), em.getId());
            
            } else {
                throw new BusinessRuleException("Formato não suportado para geração automática: " + formato);
            }
        }
    }

    private boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    private PartidaResponseDTO toResponseDTO(Partida partida) {
        // CORREÇÃO: Adicionando verificação de nulos para o DTO
        String time1Nome = (partida.getTime1() != null) ? partida.getTime1().getNome() : "Time Indefinido";
        String time2Nome = (partida.getTime2() != null) ? partida.getTime2().getNome() : "Time Indefinido";
        String vencedorNome = (partida.getVencedor() != null) ? partida.getVencedor().getNome() : null;

        return PartidaResponseDTO.builder()
                .id(partida.getId())
                .round(partida.getRound())
                .statusPartida(partida.getStatusPartida().name())
                .time1Nome(time1Nome)
                .time1Placar(partida.getTime1Placar())
                .time2Nome(time2Nome)
                .time2Placar(partida.getTime2Placar())
                .vencedorNome(vencedorNome)
                .build();
    }
}
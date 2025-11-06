package br.ifsp.events.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.events.dto.partida.PartidaResponseDTO;
import br.ifsp.events.model.Partida;
import br.ifsp.events.repository.PartidaRepository;
import br.ifsp.events.service.PartidaService;

@Service
public class PartidaServiceImpl implements PartidaService {

    private final PartidaRepository partidaRepository;

    public PartidaServiceImpl(PartidaRepository partidaRepository) {
        this.partidaRepository = partidaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartidaResponseDTO> listByEvento(Long eventoId) {
        List<Partida> partidas = partidaRepository.findAllByEventoModalidade_Evento_Id(eventoId);
        
        return partidas.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private PartidaResponseDTO toResponseDTO(Partida partida) {
        return PartidaResponseDTO.builder()
                .id(partida.getId())
                .round(partida.getRound())
                .statusPartida(partida.getStatusPartida().name())
                .time1Nome(partida.getTime1().getNome())
                .time1Placar(partida.getTime1Placar())
                .time2Nome(partida.getTime2().getNome())
                .time2Placar(partida.getTime2Placar())
                .vencedorNome(partida.getVencedor() != null ? partida.getVencedor().getNome() : null)
                .build();
    }
}
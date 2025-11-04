package br.ifsp.events.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "eventoModalidades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoModalidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O evento é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventoId", nullable = false)
    private Evento evento;

    @NotNull(message = "A modalidade é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modalidadeId", nullable = false)
    private Modalidade modalidade;

    private int maxTimes;

    private int minJogadoresPorTime;

    private int maxJogadoresPorTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    private FormatoEventoModalidade formatoEventoModalidade;
}

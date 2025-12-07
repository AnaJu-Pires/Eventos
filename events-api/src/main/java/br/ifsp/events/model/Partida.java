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
@Table(name = "partidas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventoModalidadeId", nullable = false)
    private EventoModalidade eventoModalidade;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time1Id", nullable = false)
    private Time time1;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time2Id", nullable = false)
    private Time time2;

    private int time1Placar;
    private int time2Placar;

    private int round;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusPartida statusPartida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vencedorId")
    private Time vencedor;
}

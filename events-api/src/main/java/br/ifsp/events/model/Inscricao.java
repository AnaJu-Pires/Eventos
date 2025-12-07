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
@Table(name = "inscricoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inscricao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O time é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timeId", nullable = false)
    private Time time;

    @NotNull(message = "A modalidade é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventoModalidadeId", nullable = false)
    private EventoModalidade eventoModalidade;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusInscricao statusInscricao = StatusInscricao.PENDENTE;
}

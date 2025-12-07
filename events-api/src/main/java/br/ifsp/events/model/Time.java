package br.ifsp.events.model;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "times")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"modalidade", "membros"}) 
@EqualsAndHashCode(exclude = {"modalidade", "membros"})
public class Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do time é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capitaoId", nullable = false)
    private User capitao;

    @NotNull(message = "A modalidade é obrigatória na criação do time")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modalidadeId", nullable = false, updatable = false)
    private Modalidade modalidade;

    private int qtdVitorias = 0;
    private int qtdPartidas = 0;

    @ManyToMany
    @JoinTable(
        name = "timeMembros",
        joinColumns = @JoinColumn(name = "timeId"),
        inverseJoinColumns = @JoinColumn(name = "usuarioId")
    )
    private Set<User> membros = new HashSet<>();

    @OneToMany(mappedBy = "time", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Inscricao> inscricoes;

    public double getWinRate() {
        if (qtdPartidas == 0) {
            return 0.0;
        }
        return (double) qtdVitorias / qtdPartidas;
    }

    public void incrementaVitoria() {
        this.qtdVitorias++;
        this.qtdPartidas++;
    }

    public void incrementaPartida() {
        this.qtdPartidas++;
    }
}
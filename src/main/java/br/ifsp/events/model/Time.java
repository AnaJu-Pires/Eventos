package br.ifsp.events.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.Set;
import java.util.HashSet;

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

    @ManyToMany
    @JoinTable(
        name = "timeMembros",
        joinColumns = @JoinColumn(name = "timeId"),
        inverseJoinColumns = @JoinColumn(name = "usuarioId")
    )
    private Set<User> membros = new HashSet<>();
}
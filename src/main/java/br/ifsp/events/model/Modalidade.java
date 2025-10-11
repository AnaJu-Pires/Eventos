package br.ifsp.events.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "modalidades")
@Data
@NoArgsConstructor
@AllArgsConstructor
//evita stackoverflow impedindo loop
@ToString(exclude = {"usuariosInteressados", "eventos"})
@EqualsAndHashCode(exclude = {"usuariosInteressados", "eventos"})
public class Modalidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da modalidade é obrigatório")
    @Column(unique = true, nullable = false)
    private String nome;

    private String descricao;

    @ManyToMany(mappedBy = "interesses")
    @JsonIgnore //evitar loop
    private Set<User> usuariosInteressados;

    @ManyToMany(mappedBy = "modalidades")
    @JsonIgnore //evitar loop
    private Set<Evento> eventos;
    
}
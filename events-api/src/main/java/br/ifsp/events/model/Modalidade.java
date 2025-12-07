package br.ifsp.events.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
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
@ToString(exclude = {"usuariosInteressados", "eventoModalidades"})
@EqualsAndHashCode(exclude = {"usuariosInteressados", "eventoModalidades"})
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

    @OneToMany(mappedBy = "modalidade", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<EventoModalidade> eventoModalidades;
    
}
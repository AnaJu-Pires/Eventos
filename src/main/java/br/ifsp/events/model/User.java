package br.ifsp.events.model;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "O campo nome deve ser preenchido")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    private String senha;

    // Aqui eu tava pensando que não precisa de validações porque nao é o usuário que coloca, mas nao sei...
    private PerfilUser perfilUser;

    private StatusUser statusUser = StatusUser.INATIVO;

    private String tokenConfirmacao;
    
    private String tokenRecuperacaoSenha;

    private LocalDateTime dataExpiracaoTokenRecuperacao;
    
    @ManyToMany(fetch = FetchType.LAZY) //Lazy carrega so se for necessario
    @JoinTable(
        name = "usuario_interesses",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "modalidade_id")
    )
    private Set<Modalidade> interesses;
    
}
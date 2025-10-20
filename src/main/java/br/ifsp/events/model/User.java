package br.ifsp.events.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

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
    private String senha;

    // Aqui eu tava pensando que não precisa de validações porque nao é o usuário que coloca, mas nao sei...
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerfilUser perfilUser;

    @Enumerated(EnumType.STRING)
    private StatusUser statusUser = StatusUser.INATIVO;

    private String tokenConfirmacao;
    
    private String tokenRecuperacaoSenha;

    private LocalDateTime dataExpiracaoTokenConfirmacao;

    private LocalDateTime dataExpiracaoTokenRecuperacao;
    
    @ManyToMany(fetch = FetchType.LAZY) //Lazy carrega so se for necessario
    @JoinTable(
        name = "usuarioInteresses",
        joinColumns = @JoinColumn(name = "usuarioId"),
        inverseJoinColumns = @JoinColumn(name = "modalidadeId")
    )
    private Set<Modalidade> interesses;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(perfilUser.name()));
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isEnabled() {
        return this.statusUser == StatusUser.ATIVO;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.statusUser != StatusUser.BLOQUEADO;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // lógica futura
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // lógica futura
    } 
}
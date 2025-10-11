package br.ifsp.events.dto.user;

import br.ifsp.events.model.PerfilUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private PerfilUser perfilUser;
}

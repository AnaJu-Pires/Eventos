package br.ifsp.events.dto.user;

import lombok.Data;
import br.ifsp.events.model.PerfilUser;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição de atualização de perfil de usuário (Apenas Admin)")
public class UserRoleUpdateDTO {

    @Schema(description = "O novo perfil do usuário", 
            example = "ADMIN", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O perfil do usuário é obrigatório")
    private PerfilUser perfilUser;
}
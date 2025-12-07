package br.ifsp.events.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern.Flag;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição de cadastro de usuário")
public class UserRegisterDTO {

    @Schema(description = "Nome do usuário", 
            example = "Ana da Silva", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 255, message = "O nome deve ter entre 3 e 255 caracteres")
    private String nome;

    @Schema(description = "E-mail do usuário (institucional)", 
            example = "ana.silva@aluno.ifsp.edu.br", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(regexp = "^[A-Za-z0-9._%+-]+@(aluno\\.)?ifsp\\.edu\\.br$", 
           flags = Flag.CASE_INSENSITIVE, 
           message = "O e-mail deve ser institucional do IFSP (ex: ...@ifsp.edu.br ou ...@aluno.ifsp.edu.br)"
    )
    private String email;

    @Schema(description = "Senha do usuário", 
            example = "senhaForte123", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres")
    private String senha;
}
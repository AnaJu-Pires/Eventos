package br.ifsp.events.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern.Flag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição de cadastro de usuário")
public class UserRegisterDTO {

    @Schema(description = "Nome do usuário")
    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @Schema(description = "E-mail do usuário (institucional)")
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(regexp = "^[A-Za-z0-9._%+-]+@(aluno\\.)?ifsp\\.edu\\.br$", 
           flags = Flag.CASE_INSENSITIVE, 
           message = "O e-mail deve ser institucional do IFSP (ex: ...@ifsp.edu.br ou ...@aluno.ifsp.edu.br)"
    )
    private String email;

    @Schema(description = "Senha do usuário")
    @NotBlank(message = "A senha é obrigatória")
    private String senha;
}

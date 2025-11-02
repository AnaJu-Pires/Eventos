package br.ifsp.events.dto.user;

import java.util.List;

import br.ifsp.events.dto.modalidade.ModalidadeRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInteresseResponseDTO {
    private List<ModalidadeRequestDTO> interesses;
}
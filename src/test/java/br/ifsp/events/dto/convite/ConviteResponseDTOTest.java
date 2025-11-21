package br.ifsp.events.dto.convite;

import br.ifsp.events.dto.time.TimeResponseDTO;
import br.ifsp.events.model.StatusConvite;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ConviteResponseDTOTest {

    @Test
    void noArgsConstructor_createsEmptyDto() {
        ConviteResponseDTO dto = new ConviteResponseDTO();

        assertNull(dto.getId());
        assertNull(dto.getTime());
        assertNull(dto.getStatus());
        assertNull(dto.getDataExpiracao());
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        TimeResponseDTO time = new TimeResponseDTO();
        LocalDateTime expiracao = LocalDateTime.now().plusDays(7);

        ConviteResponseDTO dto = new ConviteResponseDTO(
                1L, time, StatusConvite.PENDENTE, expiracao
        );

        assertEquals(1L, dto.getId());
        assertEquals(time, dto.getTime());
        assertEquals(StatusConvite.PENDENTE, dto.getStatus());
        assertEquals(expiracao, dto.getDataExpiracao());
    }

    @Test
    void setters_updateValues() {
        ConviteResponseDTO dto = new ConviteResponseDTO();

        dto.setId(2L);
        dto.setStatus(StatusConvite.ACEITO);

        assertEquals(2L, dto.getId());
        assertEquals(StatusConvite.ACEITO, dto.getStatus());
    }

    @Test
    void statusCanBeChanged() {
        ConviteResponseDTO dto = new ConviteResponseDTO();

        dto.setStatus(StatusConvite.RECUSADO);

        assertEquals(StatusConvite.RECUSADO, dto.getStatus());
    }
}

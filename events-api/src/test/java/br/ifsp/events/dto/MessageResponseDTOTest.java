package br.ifsp.events.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageResponseDTOTest {

    @Test
    void recordAccessorsAndEquals() {
        MessageResponseDTO a = new MessageResponseDTO("ok");
        MessageResponseDTO b = new MessageResponseDTO("ok");

        assertEquals("ok", a.message());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}

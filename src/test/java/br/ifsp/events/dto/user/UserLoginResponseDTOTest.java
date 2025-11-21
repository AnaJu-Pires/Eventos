package br.ifsp.events.dto.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserLoginResponseDTOTest {

    @Test
    void noArgsConstructor_createsEmptyDto() {
        UserLoginResponseDTO dto = new UserLoginResponseDTO();

        assertNull(dto.getToken());
    }

    @Test
    void allArgsConstructor_setsToken() {
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0";

        UserLoginResponseDTO dto = new UserLoginResponseDTO(jwtToken);

        assertEquals(jwtToken, dto.getToken());
    }

    @Test
    void setter_updatesToken() {
        UserLoginResponseDTO dto = new UserLoginResponseDTO();
        String token = "new-token-123";

        dto.setToken(token);

        assertEquals(token, dto.getToken());
    }

    @Test
    void equality_twoInstancesWithSameToken() {
        String token = "same-token";

        UserLoginResponseDTO dto1 = new UserLoginResponseDTO(token);
        UserLoginResponseDTO dto2 = new UserLoginResponseDTO(token);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
}

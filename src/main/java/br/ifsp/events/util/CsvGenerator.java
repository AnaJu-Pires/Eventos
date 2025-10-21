package br.ifsp.events.util;

import java.io.PrintWriter;
import java.util.List;

import br.ifsp.events.dto.user.UserResponseDTO;

public class CsvGenerator {

    /**
     * Gera o conteúdo CSV a partir de uma lista de UserResponseDTOs.
     * @param dtos A lista de DTOs de usuário.
     * @param writer O PrintWriter para escrever o conteúdo CSV.
     */
    public static void generateUserProfilesCsv(List<UserResponseDTO> dtos, PrintWriter writer) {
        // 1. Escreve o cabeçalho
        writer.println("ID,Nome,Email,Perfil");

        // 2. Escreve os dados
        for (UserResponseDTO dto : dtos) {
            String line = String.format("%d,\"%s\",\"%s\",%s",
                dto.getId(),
                // Envolve Nome e Email em aspas para lidar com vírgulas ou caracteres especiais
                dto.getNome().replace("\"", "\"\""), // Escape de aspas duplas
                dto.getEmail().replace("\"", "\"\""),
                dto.getPerfilUser().name()
            );
            writer.println(line);
        }
    }
}
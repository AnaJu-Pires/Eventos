package br.ifsp.events.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.User;

public interface UserRepository {
    /**
     * Salva ou atualiza um usuário no banco de dados.
     * @param user O usuário a ser salvo.
     * @return O usuário salvo (com o ID, se for novo).
     */
    User save(User user);

    /**
     * Verifica se um usuário com o e-mail fornecido já existe.
     * @param email O e-mail a ser verificado.
     * @return true se o e-mail já existe, false caso contrário.
     */
    boolean existsByEmail(String email);

    /**
     * Busca um usuário pelo seu token de confirmação de cadastro.
     * @param token O token a ser buscado.
     * @return um Optional contendo o usuário, se encontrado.
     */
    Optional<User> findByTokenConfirmacao(String token);

    /**
     * Encontra todos os usuários inativos cujo token de confirmação já expirou.
     * @param status O status a ser buscado (INATIVO).
     * @param now A data/hora atual para comparação.
     * @return Uma lista de usuários expirados.
     */
    List<User> findAllByStatusUserAndDataExpiracaoTokenConfirmacaoBefore(StatusUser status, LocalDateTime now);
    
    /**
     * Deleta uma lista de usuários em lote.
     * @param users A lista de usuários a ser deletada.
     */
    void deleteAll(List<User> users);
}

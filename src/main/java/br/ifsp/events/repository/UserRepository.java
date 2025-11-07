package br.ifsp.events.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import br.ifsp.events.model.RankEngajamento;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.User;

public interface UserRepository {
    
    /**
     * salva ou atualiza um usuário
     * @param user
     * @return usuário salvo
     */
    User save(User user);

    /**
     * encontra todos os usuários
     */
    List<User> findAll();

    /**
     * verifica se já existe um usuário com o e-mail fornecido
     * @param email
     * @return true se o e-mail já existir, false caso contrário
     */
    boolean existsByEmail(String email);

    /**
     * busca um usuário pelo seu token de confirmação de cadastro
     * @param token
     * @return um Optional contendo o usuário se encontrado, ou vazio
     */
    Optional<User> findByTokenConfirmacao(String token);

    /**
     * encontra todos os usuários com um determinado status e a data de expiração do token de confirmação
     * @param status o status do usuário a ser buscado
     * @param now a data e hora atual, para comparar com a data de expiração do token.
     * @return uma lista de usuários encontrados
     */
    List<User> findAllByStatusUserAndDataExpiracaoTokenConfirmacaoBefore(StatusUser status, LocalDateTime now);
    
    /**
     * deleta uma lista de usuários do banco
     * @param users
     */
    void deleteAll(List<User> users);

    /**
     * busca um usuário pelo seu endereço de e-mail
     * @param email
     * @return o usuário, se encontrado
     */
    Optional<User> findByEmail(String email);

    /**
     * busca um usuário pelo seu ID
     * @param id
     * @return o usuário, se encontrado
     */
    Optional<User> findById(Long id);

    long count();

    /**
     * Encontra todos os usuários que possuem um dos ranks da lista (PLATINA ou DIAMANTE).
     * Usado para resetar os ranks antes de recalcular.
     */
    List<User> findAllByRankIn(List<RankEngajamento> ranks);

    /**
     * Encontra os N usuários com maior 'pontosSaldo'.
     * Usado para definir os novos ranks.
     */
    List<User> findTopNByPontosSaldo(int n);
}
package br.ifsp.events.scheduler;

import br.ifsp.events.model.NivelEngajamento;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.RankEngajamento;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.EmailService;
import br.ifsp.events.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Teste de Integração para o Agendador de Ranks (Gamificação).
 * Verifica se a lógica de atribuição de ranks (Diamante/Platina)
 * baseada na pontuação funciona corretamente no banco de dados.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class RankSchedulerIntegrationTest {

    @Autowired
    private RankScheduler rankScheduler;

    @Autowired
    private UserRepository userRepository;

    // Mockamos serviços de notificação para isolar o teste
    @MockBean
    private EmailService emailService;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("Scheduler: Deve atribuir DIAMANTE para Top 10 e PLATINA para o restante (Top 100)")
    void deveCalcularRanksCorretamente() {
        // --- ARRANGE ---
        // Vamos criar 12 usuários com pontuações decrescentes.
        // Esperado: 
        // - Usuários 1 a 10: DIAMANTE
        // - Usuários 11 e 12: PLATINA
        
        List<User> competidores = new ArrayList<>();
        long pontuacaoBase = 100000L; // Pontuação alta para ignorar dados do DataLoader

        for (int i = 1; i <= 12; i++) {
            // Cria usuário com pontuação decrescente (ex: 100.000, 99.000, 98.000...)
            competidores.add(criarCompetidor("Competidor " + i, pontuacaoBase - (i * 1000)));
        }

        // --- ACT ---
        // Força a execução do cálculo de ranks
        rankScheduler.executarAtualizacaoDeRanks();

        // --- ASSERT ---
        
        // Validar Top 10 (Diamante)
        for (int i = 0; i < 10; i++) {
            User u = buscarUsuarioAtualizado(competidores.get(i).getId());
            assertEquals(RankEngajamento.DIAMANTE, u.getRank(), 
                "O Competidor " + (i+1) + " (Top 10) deveria ser DIAMANTE.");
        }

        // Validar posições 11 e 12 (Platina)
        for (int i = 10; i < 12; i++) {
            User u = buscarUsuarioAtualizado(competidores.get(i).getId());
            assertEquals(RankEngajamento.PLATINA, u.getRank(), 
                "O Competidor " + (i+1) + " (Fora do Top 10) deveria ser PLATINA.");
        }
    }

    @Test
    @DisplayName("Scheduler: Deve 'rebaixar' usuário que perdeu pontos ou foi ultrapassado")
    void deveRebaixarUsuario() {
        // --- ARRANGE ---
        // 1. Cria um ex-campeão que já tem rank DIAMANTE no banco, mas agora tem 0 pontos.
        User exCampeao = criarCompetidor("Ex Campeao", 0L); // Removi acento por precaução
        exCampeao.setRank(RankEngajamento.DIAMANTE);
        userRepository.save(exCampeao);

        // 2. Cria um novo campeão com muitos pontos
        // CORREÇÃO AQUI: "Novo Campeao" sem til (~) para gerar o email 'novo_campeao@...'
        criarCompetidor("Novo Campeao", 50000L);

        // --- ACT ---
        rankScheduler.executarAtualizacaoDeRanks();

        // --- ASSERT ---
        User exCampeaoAtualizado = buscarUsuarioAtualizado(exCampeao.getId());
        
        // Verifica se o ex-campeão perdeu o posto (não é mais Diamante)
        // Nota: Como ele tem 0 pontos, ele provavelmente cairá para NENHUM ou PLATINA (se existirem < 100 users)
        // O importante é garantir que a lógica rodou e atualizou o banco.
        
        // Vamos focar no Novo Campeão virando Diamante.
        // Agora a busca vai funcionar porque o e-mail gerado será 'novo_campeao@rank.test'
        User novoCampeao = userRepository.findByEmail("novo_campeao@rank.test").orElseThrow();
        
        assertEquals(RankEngajamento.DIAMANTE, novoCampeao.getRank(), 
            "O usuário com mais pontos deve assumir o topo (DIAMANTE).");
            
        // Opcional: Verificar que o antigo perdeu o posto
        assertNotEquals(RankEngajamento.DIAMANTE, exCampeaoAtualizado.getRank(), 
            "O usuário que perdeu os pontos não deve mais ser DIAMANTE.");
    }

    // --- Helpers ---

    private User criarCompetidor(String nome, Long pontos) {
        User u = new User();
        // Gera email único baseado no nome para evitar duplicidade
        String email = nome.toLowerCase().replace(" ", "_") + "@rank.test";
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha("123");
        u.setPerfilUser(PerfilUser.ALUNO);
        u.setStatusUser(StatusUser.ATIVO);
        u.setPontosSaldo(pontos);
        u.setPontosRecorde(pontos);
        u.setNivel(NivelEngajamento.BRONZE);
        u.setRank(RankEngajamento.NENHUM); // Começa sem rank
        return userRepository.save(u);
    }

    private User buscarUsuarioAtualizado(Long id) {
        // Busca do banco para garantir dados persistidos após a transação do scheduler
        return userRepository.findById(id).orElseThrow();
    }
}
package br.ifsp.events.config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import br.ifsp.events.model.*;
import br.ifsp.events.repository.*;
import br.ifsp.events.service.GamificationService;
import br.ifsp.events.service.PartidaService;

@Component
@DependsOn("entityManagerFactory")
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepository userRepository;
    private final ModalidadeRepository modalidadeRepository;
    private final TimeRepository timeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventoRepository eventoRepository;
    private final EventoModalidadeRepository eventoModalidadeRepository;
    private final InscricaoRepository inscricaoRepository;
    private final PartidaService partidaService;
    private final PartidaRepository partidaRepository;
    private final ComunidadeRepository comunidadeRepository;
    private final PostRepository postRepository;
    private final ComentarioRepository comentarioRepository;
    private final VotoRepository votoRepository;
    
    // Injeção do serviço para comunicar com o microsserviço
    private final GamificationService gamificationService;

    public DataLoader(UserRepository userRepository, ModalidadeRepository modalidadeRepository, 
                      TimeRepository timeRepository, PasswordEncoder passwordEncoder,
                      EventoRepository eventoRepository,
                      EventoModalidadeRepository eventoModalidadeRepository,
                      InscricaoRepository inscricaoRepository,
                      PartidaService partidaService,
                      PartidaRepository partidaRepository,
                      ComunidadeRepository comunidadeRepository,
                      PostRepository postRepository,
                      ComentarioRepository comentarioRepository,
                      VotoRepository votoRepository,
                      GamificationService gamificationService) {
        this.userRepository = userRepository;
        this.modalidadeRepository = modalidadeRepository;
        this.timeRepository = timeRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventoRepository = eventoRepository;
        this.eventoModalidadeRepository = eventoModalidadeRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.partidaService = partidaService;
        this.partidaRepository = partidaRepository;
        this.comunidadeRepository = comunidadeRepository;
        this.postRepository = postRepository;
        this.comentarioRepository = comentarioRepository;
        this.votoRepository = votoRepository;
        this.gamificationService = gamificationService;
    }

    @Override
    public void run(String... args) throws Exception {
        
        if (userRepository.count() > 0) {
            logger.info("O banco de dados já está populado. Ignorando DataLoader.");
            return;
        }

        logger.info("Populando o banco de dados com dados de teste...");

        Faker faker = new Faker(Locale.forLanguageTag("pt-BR"));
        String senhaPadrao = passwordEncoder.encode("123456");

        logger.info("Criando usuários de teste principais...");
        
        // Usando o novo método auxiliar que já cria o score no microsserviço
        User admin = criarUsuario("Administrador Geral", "admin@ifsp.edu.br", senhaPadrao, PerfilUser.ADMIN);
        User gestor = criarUsuario("Gestor de Eventos", "gestor@ifsp.edu.br", senhaPadrao, PerfilUser.GESTOR_EVENTOS);
        
        // Alunos principais
        User alunoAna = criarUsuario("Ana", "ana@aluno.ifsp.edu.br", senhaPadrao, PerfilUser.ALUNO);
        User alunoNat = criarUsuario("Nat", "nat@aluno.ifsp.edu.br", senhaPadrao, PerfilUser.ALUNO);
        User alunoRaul = criarUsuario("Raul", "raul@aluno.ifsp.edu.br", senhaPadrao, PerfilUser.ALUNO);
        User alunoRodrigo = criarUsuario("Rodrigo", "rodrigo@aluno.ifsp.edu.br", senhaPadrao, PerfilUser.ALUNO);
        
        // Comissão Técnica (Opcional, mantido para compatibilidade)
        User comissao = new User();
        comissao.setNome("Comissão Técnica Aluno");
        comissao.setEmail("comissao@aluno.ifsp.edu.br");
        comissao.setSenha(senhaPadrao);
        comissao.setPerfilUser(PerfilUser.COMISSAO_TECNICA);
        comissao.setStatusUser(StatusUser.ATIVO);
        userRepository.save(comissao);
        // Nota: Não inicializei gamificação para comissão pois será deletada depois.

        logger.info("Gerando 20 alunos com Faker...");
        List<User> alunosFaker = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String nome = faker.name().firstName();
            String sobrenome = faker.name().lastName();
            String username = faker.name().username(); 

            // Cria usuário e chama microsserviço
            User alunoFaker = criarUsuario(nome + " " + sobrenome, username + "@aluno.ifsp.edu.br", senhaPadrao, PerfilUser.ALUNO);
            alunosFaker.add(alunoFaker);
        }

        logger.info("Criando modalidades...");
        Modalidade futsal = new Modalidade();
        futsal.setNome("Futsal");
        futsal.setDescricao("Futsal padrão de quadra");
        modalidadeRepository.save(futsal);

        Modalidade volei = new Modalidade();
        volei.setNome("Voleibol");
        volei.setDescricao("Voleibol padrão de quadra");
        modalidadeRepository.save(volei);

        Modalidade xadrez = new Modalidade();
        xadrez.setNome("Xadrez");
        xadrez.setDescricao("Misto");
        modalidadeRepository.save(xadrez);

        logger.info("Criando times de teste principais...");
    
        Time timeFutsal = new Time();
        timeFutsal.setNome("IFSP Câmpus BTV");
        timeFutsal.setCapitao(alunoRaul);
        timeFutsal.setModalidade(futsal);
        timeFutsal.setMembros(Set.of(alunoRaul, alunoRodrigo));
        timeRepository.save(timeFutsal);

        Time timeVolei = new Time();
        timeVolei.setNome("As Poderosas");
        timeVolei.setCapitao(alunoNat);
        timeVolei.setModalidade(volei);
        timeVolei.setMembros(Set.of(alunoNat, alunoAna));
        timeRepository.save(timeVolei);
        
        logger.info("Gerando 5 times de Futsal com Faker...");
        List<Time> timesFutsalFaker = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Collections.shuffle(alunosFaker);
            User capitao = alunosFaker.get(0);
            List<User> membros = new ArrayList<>(alunosFaker.subList(0, Math.min(5, alunosFaker.size())));

            Time timeFaker = new Time();
            timeFaker.setNome(faker.team().name());
            timeFaker.setCapitao(capitao);
            timeFaker.setModalidade(futsal);
            timeFaker.getMembros().addAll(membros);
            timesFutsalFaker.add(timeRepository.save(timeFaker));
        }

        logger.info("Criando eventos de teste...");
        
        Evento jogosVerao = new Evento();
        jogosVerao.setNome("Jogos de Verão IFSP 2025");
        jogosVerao.setDescricao("Evento de Jogos de Verão IFSP 2025");
        jogosVerao.setDataInicio(LocalDate.now().plusDays(10));
        jogosVerao.setDataFim(LocalDate.now().plusDays(17));
        jogosVerao.setOrganizador(gestor);
        jogosVerao.setStatus(StatusEvento.PLANEJADO);
        eventoRepository.save(jogosVerao);

        EventoModalidade futsalVerao = new EventoModalidade();
        futsalVerao.setEvento(jogosVerao);
        futsalVerao.setModalidade(futsal);
        futsalVerao.setMaxTimes(8);
        futsalVerao.setDataFimInscricao(LocalDate.now().plusDays(5));
        futsalVerao.setFormatoEventoModalidade(FormatoEventoModalidade.MATA_MATA);
        eventoModalidadeRepository.save(futsalVerao);

        EventoModalidade voleiVerao = new EventoModalidade();
        voleiVerao.setEvento(jogosVerao);
        voleiVerao.setModalidade(volei);
        voleiVerao.setMaxTimes(8);
        voleiVerao.setDataFimInscricao(LocalDate.now().plusDays(5));
        voleiVerao.setFormatoEventoModalidade(FormatoEventoModalidade.PONTOS_CORRIDOS);
        eventoModalidadeRepository.save(voleiVerao);

        Evento torneioInterno = new Evento();
        torneioInterno.setNome("Torneio Interno de Futsal 2024");
        torneioInterno.setDescricao("Torneio relâmpago que já ocorreu.");
        torneioInterno.setDataInicio(LocalDate.now().minusDays(30));
        torneioInterno.setDataFim(LocalDate.now().minusDays(29));
        torneioInterno.setOrganizador(gestor);
        torneioInterno.setStatus(StatusEvento.FINALIZADO);
        eventoRepository.save(torneioInterno);

        EventoModalidade futsalInterno = new EventoModalidade();
        futsalInterno.setEvento(torneioInterno);
        futsalInterno.setModalidade(futsal);
        futsalInterno.setMaxTimes(4);
        futsalInterno.setDataFimInscricao(LocalDate.now().minusDays(35));
        futsalInterno.setFormatoEventoModalidade(FormatoEventoModalidade.MATA_MATA);
        eventoModalidadeRepository.save(futsalInterno);

        logger.info("Criando evento em andamento...");
        Evento torneioAtual = new Evento();
        torneioAtual.setNome("Interclasse Futsal 2025");
        torneioAtual.setDescricao("O grande mata-mata do segundo semestre!");
        torneioAtual.setDataInicio(LocalDate.now().minusDays(1));
        torneioAtual.setDataFim(LocalDate.now().plusDays(1));
        torneioAtual.setOrganizador(gestor);
        torneioAtual.setStatus(StatusEvento.EM_ANDAMENTO);
        eventoRepository.save(torneioAtual);
 
        EventoModalidade futsalAtual = new EventoModalidade();
        futsalAtual.setEvento(torneioAtual);
        futsalAtual.setModalidade(futsal);
        futsalAtual.setMaxTimes(4);
        futsalAtual.setDataFimInscricao(LocalDate.now().minusDays(7)); 
        futsalAtual.setFormatoEventoModalidade(FormatoEventoModalidade.MATA_MATA);
        eventoModalidadeRepository.save(futsalAtual);


        logger.info("Inscrevendo times nos eventos...");

        Inscricao inscricao1 = new Inscricao();
        inscricao1.setTime(timeFutsal); 
        inscricao1.setEventoModalidade(futsalVerao);
        inscricao1.setStatusInscricao(StatusInscricao.APROVADA);
        inscricaoRepository.save(inscricao1);
        
        Inscricao inscricao2 = new Inscricao();
        inscricao2.setTime(timeVolei);
        inscricao2.setEventoModalidade(voleiVerao);
        inscricao2.setStatusInscricao(StatusInscricao.APROVADA);
        inscricaoRepository.save(inscricao2);

        Inscricao inscricao3 = new Inscricao();
        inscricao3.setTime(timeFutsal); 
        inscricao3.setEventoModalidade(futsalInterno);
        inscricao3.setStatusInscricao(StatusInscricao.APROVADA); 
        inscricaoRepository.save(inscricao3);

        logger.info("Inscrevendo times do Faker...");
        for (Time timeFaker : timesFutsalFaker) {
            Inscricao inscricaoFaker = new Inscricao();
            inscricaoFaker.setTime(timeFaker);
            inscricaoFaker.setEventoModalidade(futsalVerao);
            inscricaoFaker.setStatusInscricao(
                faker.bool().bool() ? StatusInscricao.APROVADA : StatusInscricao.PENDENTE
            );
            inscricaoRepository.save(inscricaoFaker);
        }

        logger.info("Inscrevendo 4 times no evento em andamento...");
        if (timesFutsalFaker.size() >= 3) {
            Inscricao inscAtual1 = new Inscricao();
            inscAtual1.setTime(timeFutsal);
            inscAtual1.setEventoModalidade(futsalAtual);
            inscAtual1.setStatusInscricao(StatusInscricao.APROVADA);
            inscricaoRepository.save(inscAtual1);

            Inscricao inscAtual2 = new Inscricao();
            inscAtual2.setTime(timesFutsalFaker.get(0));
            inscAtual2.setEventoModalidade(futsalAtual);
            inscAtual2.setStatusInscricao(StatusInscricao.APROVADA);
            inscricaoRepository.save(inscAtual2);

            Inscricao inscAtual3 = new Inscricao();
            inscAtual3.setTime(timesFutsalFaker.get(1));
            inscAtual3.setEventoModalidade(futsalAtual);
            inscAtual3.setStatusInscricao(StatusInscricao.APROVADA);
            inscricaoRepository.save(inscAtual3);

            Inscricao inscAtual4 = new Inscricao();
            inscAtual4.setTime(timesFutsalFaker.get(2));
            inscAtual4.setEventoModalidade(futsalAtual);
            inscAtual4.setStatusInscricao(StatusInscricao.APROVADA);
            inscricaoRepository.save(inscAtual4);
        }

         try {
            logger.info("Gerando chave MATA-MATA para o Evento ID: {}", torneioAtual.getId());
            partidaService.gerarChaveParaEvento(torneioAtual.getId(), FormatoEventoModalidade.MATA_MATA);
        } catch (Exception e) {
            logger.error("Falha ao gerar chave: {}", e.getMessage());
        }

        // ==========================================
        // CARGA COMPLEMENTAR (B23)
        // ==========================================
        
        complementarDados(faker, admin, gestor, alunoAna, alunoNat, alunoRaul, alunoRodrigo, comissao, 
                          alunosFaker, futsal, volei, xadrez, futsalInterno, timeFutsal, timesFutsalFaker);
        
        logger.info("Dados de teste carregados com sucesso.");
    }

    /**
     * MÉTODO AUXILIAR CRUCIAL:
     * Salva o usuário no Monólito E chama o Microsserviço de Gamificação.
     */
    private User criarUsuario(String nome, String email, String senha, PerfilUser perfil) {
        User user = new User();
        user.setNome(nome);
        user.setEmail(email);
        user.setSenha(senha);
        user.setPerfilUser(perfil);
        user.setStatusUser(StatusUser.ATIVO);
        
        // 1. Salva no banco local (Events DB)
        User salvo = userRepository.save(user);
        
        // 2. Chama o microsserviço para criar a conta de pontos (Gamificacao DB)
        try {
            gamificationService.inicializarPerfil(salvo.getId());
            logger.info("Gamificação inicializada para: " + email);
        } catch (Exception e) {
            logger.warn("Falha ao inicializar gamificação para {}: {}", email, e.getMessage());
        }
        
        return salvo;
    }

    private void complementarDados(Faker faker, User admin, User gestor, User ana, User nat, User raul, User rodrigo, User comissao,
                                   List<User> alunosFaker, Modalidade futsal, Modalidade volei, Modalidade xadrez, 
                                   EventoModalidade futsalInterno, Time timeFutsal, List<Time> timesFutsalFaker) {
        
        logger.info(">>> Iniciando carga de dados complementares (B23)...");

        // 1. REMOVER COMISSÃO TÉCNICA
        if (comissao != null) {
            try {
                userRepository.deleteAll(List.of(comissao)); 
            } catch (Exception e) {
                logger.warn("Não foi possível remover a comissão técnica: " + e.getMessage());
            }
        }

        // OBS: Removi a lógica antiga que tentava setar "pontosSaldo" e "nivel" 
        // diretamente na entidade User, pois esses campos foram removidos.
        // A pontuação inicial (300) agora é definida pelo microsserviço no momento da criação.

        // 3. POPULAR NOVOS EVENTOS E REDE SOCIAL
        popularNovosEventos(faker, gestor, futsal, volei, xadrez, alunosFaker);
        
        // 4. POPULAR DADOS DE VOLUME (Comentários reais)
        popularDadosVolume(faker, alunosFaker);
    }
    
    private void popularNovosEventos(Faker faker, User organizador, Modalidade futsal, Modalidade volei, Modalidade xadrez, List<User> alunos) {
        logger.info("Populando 3 eventos extras (Futsal, Vôlei, Xadrez)...");
        
        // --- Evento Futsal ---
        Evento evtFutsal = new Evento();
        evtFutsal.setNome("Copa dos Veteranos 2024");
        evtFutsal.setDescricao("Torneio de encerramento do ano passado.");
        evtFutsal.setDataInicio(LocalDate.now().minusMonths(3));
        evtFutsal.setDataFim(LocalDate.now().minusMonths(2));
        evtFutsal.setStatus(StatusEvento.FINALIZADO);
        evtFutsal.setOrganizador(organizador);
        eventoRepository.save(evtFutsal);

        EventoModalidade emFutsal = new EventoModalidade();
        emFutsal.setEvento(evtFutsal);
        emFutsal.setModalidade(futsal);
        emFutsal.setFormatoEventoModalidade(FormatoEventoModalidade.MATA_MATA);
        emFutsal.setMaxTimes(4);
        emFutsal.setDataFimInscricao(LocalDate.now().minusMonths(4));
        eventoModalidadeRepository.save(emFutsal);

        List<Time> timesFutsalExtra = criarTimesExtras(faker, 4, futsal, alunos);
        inscreverTimes(timesFutsalExtra, emFutsal, StatusInscricao.APROVADA);
        
        // Partidas
        criarPartidaFinalizada(emFutsal, timesFutsalExtra.get(0), timesFutsalExtra.get(1), 5, 2, 1); 
        criarPartidaFinalizada(emFutsal, timesFutsalExtra.get(2), timesFutsalExtra.get(3), 1, 3, 1);
        criarPartidaFinalizada(emFutsal, timesFutsalExtra.get(0), timesFutsalExtra.get(3), 2, 4, 2); 
        
        // REDE SOCIAL (FUTSAL)
        Comunidade comVeteranos = criarComunidade("Resenha dos Veteranos", "Espaço oficial da Copa 2024.", organizador);
        Post postFutsal = criarPost("Final da Copa Veteranos: Que jogo!", "Parabéns ao Time 4 pelo título!", organizador, comVeteranos);
        // O cálculo de votos deve ser feito pelo PostService ou mantido se for apenas contador
        postFutsal.setVotos(15);
        postRepository.save(postFutsal);
        criarComentario("Jogão! Goleiro pegou muito. #FutsalRaiz", alunos.get(0), postFutsal);
        criarComentario("Arbitragem duvidosa hein...", alunos.get(1), postFutsal);


        // --- Evento Vôlei ---
        Evento evtVolei = new Evento();
        evtVolei.setNome("Superliga IFSP 2025");
        evtVolei.setDescricao("Liga de pontos corridos.");
        evtVolei.setDataInicio(LocalDate.now().minusDays(5));
        evtVolei.setDataFim(LocalDate.now().plusDays(20));
        evtVolei.setStatus(StatusEvento.EM_ANDAMENTO);
        evtVolei.setOrganizador(organizador);
        eventoRepository.save(evtVolei);

        EventoModalidade emVolei = new EventoModalidade();
        emVolei.setEvento(evtVolei);
        emVolei.setModalidade(volei);
        emVolei.setFormatoEventoModalidade(FormatoEventoModalidade.PONTOS_CORRIDOS);
        emVolei.setMaxTimes(4);
        emVolei.setDataFimInscricao(LocalDate.now().minusDays(10));
        eventoModalidadeRepository.save(emVolei);

        List<Time> timesVoleiExtra = criarTimesExtras(faker, 4, volei, alunos);
        inscreverTimes(timesVoleiExtra, emVolei, StatusInscricao.APROVADA);
        criarPartidaFinalizada(emVolei, timesVoleiExtra.get(0), timesVoleiExtra.get(1), 3, 0, 1);

        // REDE SOCIAL (VÔLEI)
        Comunidade comSuperliga = criarComunidade("Torcida Superliga IFSP", "Quem leva a taça?", alunos.get(3));
        Post postVolei = criarPost("A arbitragem de ontem...", "Aquele ponto no 3º set foi fora? Precisamos de VAR!", alunos.get(4), comSuperliga);
        criarComentario("Foi muito dentro! Para de chorar.", alunos.get(5), postVolei);
        criarComentario("Também achei fora. Roubado!", alunos.get(6), postVolei);


        // --- Evento Xadrez ---
        Evento evtXadrez = new Evento();
        evtXadrez.setNome("Torneio de Xadrez Relâmpago");
        evtXadrez.setDescricao("Torneio rápido.");
        evtXadrez.setDataInicio(LocalDate.now().plusMonths(1));
        evtXadrez.setDataFim(LocalDate.now().plusMonths(1).plusDays(2));
        evtXadrez.setStatus(StatusEvento.PLANEJADO);
        evtXadrez.setOrganizador(organizador);
        eventoRepository.save(evtXadrez);

        EventoModalidade emXadrez = new EventoModalidade();
        emXadrez.setEvento(evtXadrez);
        emXadrez.setModalidade(xadrez);
        emXadrez.setFormatoEventoModalidade(FormatoEventoModalidade.MATA_MATA);
        emXadrez.setMaxTimes(8);
        emXadrez.setDataFimInscricao(LocalDate.now().plusDays(10));
        eventoModalidadeRepository.save(emXadrez);

        List<Time> timesXadrezExtra = criarTimesExtras(faker, 4, xadrez, alunos);
        inscreverTimes(timesXadrezExtra, emXadrez, StatusInscricao.APROVADA);

        // REDE SOCIAL (XADREZ)
        Comunidade comXadrez = criarComunidade("Clube de Xadrez", "Estratégias.", alunos.get(7));
        Post postXadrez = criarPost("Qual o tempo de relógio?", "Vai ser 3+2 ou 5 minutos seco?", alunos.get(8), comXadrez);
        criarComentario("Será 5 minutos KO.", organizador, postXadrez);
    }

    private void popularDadosVolume(Faker faker, List<User> alunos) {
        logger.info("Gerando VOLUME de dados com CONTEÚDO REAL...");

        User admin = alunos.get(0); 
        Comunidade comNutri = criarComunidade("Nutrição e Treino", "Dicas de alimentação.", admin);
        Comunidade comCaronas = criarComunidade("Caronas IFSP", "Ofereça ou peça carona.", admin);
        
        List<Post> novosPosts = new ArrayList<>();

        Post p1 = criarPost("Dica de pré-treino rápido", "Banana amassada com aveia e mel 30min antes do treino salva demais!", alunos.get(1), comNutri);
        novosPosts.add(p1);
        Post p2 = criarPost("Onde comprar Whey com desconto?", "Alguém tem cupom ou sabe loja barata perto do campus?", alunos.get(2), comNutri);
        novosPosts.add(p2);
        Post p3 = criarPost("Carona para o jogo de Sábado", "Saio do centro às 08:00. 3 vagas. Dividir gasolina.", alunos.get(3), comCaronas);
        novosPosts.add(p3);
        Post p4 = criarPost("Alguém volta pro bairro São José?", "Treino acaba 22h, tá ruim de ônibus.", alunos.get(4), comCaronas);
        novosPosts.add(p4);
        Post p5 = criarPost("Água gelada faz mal?", "Mito ou verdade que dá choque térmico?", alunos.get(5), comNutri);
        novosPosts.add(p5);

        criarComentario("Faço isso todo dia! Às vezes coloco canela.", alunos.get(6), p1);
        criarComentario("Melhor que pré-treino caro.", alunos.get(7), p1);
        criarComentario("Na farmácia da esquina tem promoção.", alunos.get(8), p2);
        criarComentario("Compra na internet que compensa mais.", alunos.get(9), p2);
        criarComentario("Guarda uma vaga pra mim!", alunos.get(10), p3);
        criarComentario("Vou de moto, te encontro lá.", alunos.get(11), p3);
        criarComentario("Eu passo perto, saio 22:15.", alunos.get(12), p4);
        criarComentario("Valeu! Vou esperar.", alunos.get(4), p4);
        criarComentario("Mito total. Hidrate-se.", alunos.get(13), p5);
        criarComentario("Bebo gelada e tô vivo kkk", alunos.get(14), p5);

        // Votos (Volume)
        int votosRegistrados = 0;
        int tentativas = 0;
        while (votosRegistrados < 100 && tentativas < 500) {
            tentativas++;
            try {
                User eleitor = alunos.get(faker.number().numberBetween(0, alunos.size()));
                Post postAlvo = novosPosts.get(faker.number().numberBetween(0, novosPosts.size()));
                
                if (votoRepository.findByUsuarioIdAndPostId(eleitor.getId(), postAlvo.getId()).isEmpty()) {
                    Voto v = new Voto();
                    v.setUsuario(eleitor);
                    v.setPost(postAlvo);
                    v.setTipoVoto(faker.bool().bool() ? TipoVoto.UPVOTE : TipoVoto.DOWNVOTE);
                    votoRepository.save(v);
                    
                    int valor = (v.getTipoVoto() == TipoVoto.UPVOTE) ? 1 : -1;
                    postAlvo.setVotos(postAlvo.getVotos() + valor);
                    postRepository.save(postAlvo);
                    votosRegistrados++;
                }
            } catch (Exception e) {}
        }
    }

    // --- Métodos Auxiliares ---

    private List<Time> criarTimesExtras(Faker faker, int qtd, Modalidade mod, List<User> alunos) {
        List<Time> times = new ArrayList<>();
        for (int i = 0; i < qtd; i++) {
            Collections.shuffle(alunos);
            User capitao = alunos.get(0);
            Time t = new Time();
            t.setNome(mod.getNome() + " " + faker.team().name()); 
            t.setCapitao(capitao);
            t.setModalidade(mod);
            t.setMembros(Set.of(capitao)); 
            times.add(timeRepository.save(t));
        }
        return times;
    }

    private void inscreverTimes(List<Time> times, EventoModalidade em, StatusInscricao status) {
        for (Time t : times) {
            Inscricao i = new Inscricao();
            i.setTime(t);
            i.setEventoModalidade(em);
            i.setStatusInscricao(status);
            inscricaoRepository.save(i);
        }
    }

    private void criarPartidaFinalizada(EventoModalidade em, Time t1, Time t2, int p1, int p2, int round) {
        Partida p = new Partida();
        p.setEventoModalidade(em);
        p.setTime1(t1);
        p.setTime2(t2);
        p.setTime1Placar(p1);
        p.setTime2Placar(p2);
        p.setRound(round);
        p.setStatusPartida(StatusPartida.FINALIZADA);
        if (p1 > p2) p.setVencedor(t1);
        else if (p2 > p1) p.setVencedor(t2);
        partidaRepository.save(p);
    }

    private Comunidade criarComunidade(String nome, String descricao, User criador) {
        Comunidade c = new Comunidade();
        c.setNome(nome);
        c.setDescricao(descricao);
        c.setCriador(criador);
        return comunidadeRepository.save(c);
    }

    private Post criarPost(String titulo, String conteudo, User autor, Comunidade comunidade) {
        Post p = new Post();
        p.setTitulo(titulo);
        p.setConteudo(conteudo);
        p.setAutor(autor);
        p.setComunidade(comunidade);
        p.setVotos(0);
        return postRepository.save(p);
    }
    
    private void criarComentario(String texto, User autor, Post post) {
        Comentario c = new Comentario();
        c.setConteudo(texto);
        c.setAutor(autor);
        c.setPost(post);
        c.setVotos(0);
        comentarioRepository.save(c);
    }

}
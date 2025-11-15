package br.ifsp.events.config;

import com.github.javafaker.Faker;
import java.util.Locale;
import br.ifsp.events.model.Modalidade;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.Time;
import br.ifsp.events.model.User;
import br.ifsp.events.model.Evento;
import br.ifsp.events.model.EventoModalidade;
import br.ifsp.events.model.FormatoEventoModalidade;
import br.ifsp.events.model.Inscricao;
import br.ifsp.events.model.StatusEvento;
import br.ifsp.events.model.StatusInscricao;
import br.ifsp.events.repository.EventoModalidadeRepository;
import br.ifsp.events.repository.EventoRepository;
import br.ifsp.events.repository.InscricaoRepository;
import br.ifsp.events.repository.ModalidadeRepository;
import br.ifsp.events.repository.TimeRepository;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.PartidaService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepository userRepository;
    private final ModalidadeRepository modalidadeRepository;
    private final TimeRepository timeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventoRepository eventoRepository;
    private final EventoModalidadeRepository eventoModalidadeRepository;
    private final InscricaoRepository inscricaoRepository;
    private final PartidaService partidaService; // <-- INJETADO

    public DataLoader(UserRepository userRepository, ModalidadeRepository modalidadeRepository, 
                      TimeRepository timeRepository, PasswordEncoder passwordEncoder,
                      EventoRepository eventoRepository,
                      EventoModalidadeRepository eventoModalidadeRepository,
                      InscricaoRepository inscricaoRepository,
                      PartidaService partidaService) { // <-- NO CONSTRUTOR
        this.userRepository = userRepository;
        this.modalidadeRepository = modalidadeRepository;
        this.timeRepository = timeRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventoRepository = eventoRepository;
        this.eventoModalidadeRepository = eventoModalidadeRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.partidaService = partidaService; // <-- ATRIBUÍDO
    }

    @Override
    public void run(String... args) throws Exception {
        
        if (userRepository.count() > 0) {
            logger.info("O banco de dados já está populado. Pulando o carregamento de dados de teste.");
            return;
        }

        logger.info("Populando o banco de dados com dados de teste...");

        Faker faker = new Faker(new Locale("pt-BR"));

        String senhaPadrao = passwordEncoder.encode("123456");

        logger.info("Criando usuários de teste principais...");
        User admin = new User();
        admin.setNome("Administrador Geral");
        admin.setEmail("admin@ifsp.edu.br");
        admin.setSenha(senhaPadrao);
        admin.setPerfilUser(PerfilUser.ADMIN);
        admin.setStatusUser(StatusUser.ATIVO);
        userRepository.save(admin);

        User gestor = new User();
        gestor.setNome("Gestor de Eventos");
        gestor.setEmail("gestor@ifsp.edu.br");
        gestor.setSenha(senhaPadrao);
        gestor.setPerfilUser(PerfilUser.GESTOR_EVENTOS);
        gestor.setStatusUser(StatusUser.ATIVO);
        userRepository.save(gestor);

        User alunoAna = new User();
        alunoAna.setNome("Ana");
        alunoAna.setEmail("ana@aluno.ifsp.edu.br");
        alunoAna.setSenha(senhaPadrao);
        alunoAna.setPerfilUser(PerfilUser.ALUNO);
        alunoAna.setStatusUser(StatusUser.ATIVO);
        userRepository.save(alunoAna);

        User alunoNat = new User();
        alunoNat.setNome("Nat");
        alunoNat.setEmail("nat@aluno.ifsp.edu.br");
        alunoNat.setSenha(senhaPadrao);
        alunoNat.setPerfilUser(PerfilUser.ALUNO);
        alunoNat.setStatusUser(StatusUser.ATIVO);
        userRepository.save(alunoNat);

        User alunoRaul = new User();
        alunoRaul.setNome("Raul");
        alunoRaul.setEmail("raul@aluno.ifsp.edu.br");
        alunoRaul.setSenha(senhaPadrao);
        alunoRaul.setPerfilUser(PerfilUser.ALUNO);
        alunoRaul.setStatusUser(StatusUser.ATIVO);
        userRepository.save(alunoRaul);

        User alunoRodrigo = new User();
        alunoRodrigo.setNome("Rodrigo");
        alunoRodrigo.setEmail("rodrigo@aluno.ifsp.edu.br");
        alunoRodrigo.setSenha(senhaPadrao);
        alunoRodrigo.setPerfilUser(PerfilUser.ALUNO);
        alunoRodrigo.setStatusUser(StatusUser.ATIVO);
        userRepository.save(alunoRodrigo);
        
        User comissao = new User();
        comissao.setNome("Comissão Técnica Aluno");
        comissao.setEmail("comissao@aluno.ifsp.edu.br");
        comissao.setSenha(senhaPadrao);
        comissao.setPerfilUser(PerfilUser.COMISSAO_TECNICA);
        comissao.setStatusUser(StatusUser.ATIVO);
        userRepository.save(comissao);

        logger.info("Gerando 20 alunos com Faker...");
        List<User> alunosFaker = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String nome = faker.name().firstName();
            String sobrenome = faker.name().lastName();
            String username = faker.name().username(); 

            User alunoFaker = new User();
            alunoFaker.setNome(nome + " " + sobrenome);
            alunoFaker.setEmail(username + "@aluno.ifsp.edu.br");
            alunoFaker.setSenha(senhaPadrao);
            alunoFaker.setPerfilUser(PerfilUser.ALUNO);
            alunoFaker.setStatusUser(StatusUser.ATIVO);
            alunosFaker.add(userRepository.save(alunoFaker));
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
        } else {
            logger.warn("Pular criação de inscrições para evento atual: " +
                        "Não há times faker suficientes (necessário 3).");
        }

         try {
            logger.info("Gerando chave MATA-MATA para o Evento ID: {}", torneioAtual.getId());
            partidaService.gerarChaveParaEvento(torneioAtual.getId(), FormatoEventoModalidade.MATA_MATA);
            logger.info("Chave gerada com sucesso para o evento {}", torneioAtual.getId());
        } catch (Exception e) {
            logger.error("Falha ao gerar chave para evento {}: {}", torneioAtual.getId(), e.getMessage());
        }

        // add do evento finalizado partidas com placares e vencedores

        logger.info("Dados de teste carregados com sucesso (incluindo dados do Faker).");
    }

}
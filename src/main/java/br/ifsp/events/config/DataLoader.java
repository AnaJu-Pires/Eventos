package br.ifsp.events.config;

import br.ifsp.events.model.Modalidade;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.Time;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.ModalidadeRepository;
import br.ifsp.events.repository.TimeRepository; // <-- 1. IMPORTAR
import br.ifsp.events.repository.UserRepository;

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

    public DataLoader(UserRepository userRepository, ModalidadeRepository modalidadeRepository, 
                      TimeRepository timeRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modalidadeRepository = modalidadeRepository;
        this.timeRepository = timeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0 || modalidadeRepository.count() > 0 || timeRepository.count() > 0) {
            logger.info("O banco de dados já está populado. Pulando o carregamento de dados de teste.");
            return;
        }

        logger.info("Populando o banco de dados com dados de teste...");

        //senha padrao
        String senhaPadrao = passwordEncoder.encode("123456");


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

        logger.info("Adicionando interesses aos usuários...");
        alunoAna.getInteresses().add(futsal);
        alunoAna.getInteresses().add(xadrez);
        userRepository.save(alunoAna);

        alunoNat.getInteresses().add(volei);
        userRepository.save(alunoNat);

        logger.info("Criando times de teste...");
        
    
        Time timeFutsal = new Time();
        timeFutsal.setNome("IFSP Câmpus BTV");
        timeFutsal.setCapitao(alunoRaul);
        timeFutsal.setModalidade(futsal);
        timeFutsal.getMembros().add(alunoRaul);
        timeFutsal.getMembros().add(alunoRodrigo);
        timeRepository.save(timeFutsal);

       
        Time timeVolei = new Time();
        timeVolei.setNome("As Poderosas");
        timeVolei.setCapitao(alunoNat);
        timeVolei.setModalidade(volei);
        timeVolei.getMembros().add(alunoNat);
        timeVolei.getMembros().add(alunoAna);
        timeRepository.save(timeVolei);

        Time timeXadrez = new Time();
        timeXadrez.setNome("IFSP Câmpus BTV");
        timeXadrez.setCapitao(alunoRaul);
        timeXadrez.setModalidade(xadrez);
        timeXadrez.getMembros().add(alunoRaul);
        timeXadrez.getMembros().add(alunoRodrigo);
        timeXadrez.getMembros().add(alunoAna);
        timeRepository.save(timeXadrez);

        logger.info("Dados de teste carregados com sucesso.");
    }
}
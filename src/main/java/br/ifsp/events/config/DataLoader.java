package br.ifsp.events.config;

import br.ifsp.events.model.Modalidade;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.ModalidadeRepository;
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
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, ModalidadeRepository modalidadeRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modalidadeRepository = modalidadeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0 || modalidadeRepository.count() > 0) {
            logger.info("O banco de dados já está populado. Pulando o carregamento de dados de teste.");
            return;
        }

        logger.info("Populando o banco de dados com dados de teste...");

        // senha padrao
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

        User aluno = new User();
        aluno.setNome("Ana");
        aluno.setEmail("ana@aluno.ifsp.edu.br");
        aluno.setSenha(senhaPadrao);
        aluno.setPerfilUser(PerfilUser.ALUNO);
        aluno.setStatusUser(StatusUser.ATIVO);
        userRepository.save(aluno);

        User aluno1 = new User();
        aluno1.setNome("Nat");
        aluno1.setEmail("nat@aluno.ifsp.edu.br");
        aluno1.setSenha(senhaPadrao);
        aluno1.setPerfilUser(PerfilUser.ALUNO);
        aluno1.setStatusUser(StatusUser.ATIVO);
        userRepository.save(aluno1);

        User aluno2 = new User();
        aluno2.setNome("Raul");
        aluno2.setEmail("raul@aluno.ifsp.edu.br");
        aluno2.setSenha(senhaPadrao);
        aluno2.setPerfilUser(PerfilUser.ALUNO);
        aluno2.setStatusUser(StatusUser.ATIVO);
        userRepository.save(aluno2);

        User aluno3 = new User();
        aluno3.setNome("Rodrigo");
        aluno3.setEmail("rodrigo@aluno.ifsp.edu.br");
        aluno3.setSenha(senhaPadrao);
        aluno3.setPerfilUser(PerfilUser.ALUNO);
        aluno3.setStatusUser(StatusUser.ATIVO);
        userRepository.save(aluno3);
        
        User comissao = new User();
        comissao.setNome("Comissão Técnica Aluno");
        comissao.setEmail("comissao@aluno.ifsp.edu.br");
        comissao.setSenha(senhaPadrao);
        comissao.setPerfilUser(PerfilUser.COMISSAO_TECNICA);
        comissao.setStatusUser(StatusUser.ATIVO);
        userRepository.save(comissao);

        Modalidade futsal = new Modalidade();
        futsal.setNome("Futsal");
        futsal.setDescricao("Exclusivo para equipes masculinas.");
        modalidadeRepository.save(futsal);

        Modalidade volei = new Modalidade();
        volei.setNome("Voleibol");
        volei.setDescricao("Exclusivo para equipes femininas.");
        modalidadeRepository.save(volei);

        Modalidade xadrez = new Modalidade();
        xadrez.setNome("Xadrez");
        xadrez.setDescricao("Misto");
        modalidadeRepository.save(xadrez);


        logger.info("Dados de teste carregados com sucesso.");
    }
}
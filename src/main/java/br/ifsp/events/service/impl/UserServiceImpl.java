package br.ifsp.events.service.impl;

import java.io.PrintWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.ifsp.events.dto.modalidade.ModalidadeResponseDTO;
import br.ifsp.events.dto.user.UserInteresseResponseDTO;
import br.ifsp.events.dto.user.UserInteresseUpdateDTO;
import br.ifsp.events.dto.user.UserLoginDTO;
import br.ifsp.events.dto.user.UserLoginResponseDTO;
import br.ifsp.events.dto.user.UserRegisterDTO;
import br.ifsp.events.dto.user.UserResponseDTO;
import br.ifsp.events.dto.user.UserRoleUpdateDTO;
import br.ifsp.events.exception.BusinessRuleException;
import br.ifsp.events.exception.CsvGenerationException;
import br.ifsp.events.exception.ResourceNotFoundException;
import br.ifsp.events.model.Modalidade;
import br.ifsp.events.model.PerfilUser;
import br.ifsp.events.model.StatusUser;
import br.ifsp.events.model.User;
import br.ifsp.events.repository.ModalidadeRepository;
import br.ifsp.events.repository.UserRepository;
import br.ifsp.events.service.EmailService;
import br.ifsp.events.service.JwtService;
import br.ifsp.events.service.UserService;
import br.ifsp.events.util.CsvGenerator;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private static final int EXPIRATION_HOURS = 24;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ModalidadeRepository modalidadeRepository;
    private final ModelMapper modelMapper;


    public UserServiceImpl(UserRepository userRepository,  PasswordEncoder passwordEncoder, EmailService emailService, @Lazy AuthenticationManager authenticationManager, JwtService jwtService, ModalidadeRepository modalidadeRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.modalidadeRepository = modalidadeRepository;
        this.modelMapper = modelMapper;
    }


    @Override
    @Transactional
    public void registerUser(UserRegisterDTO registerDTO) {
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new BusinessRuleException("Este email já está em uso.");
        }
        
        User user = modelMapper.map(registerDTO, User.class);
        user.setSenha(passwordEncoder.encode(registerDTO.getSenha()));
        
        user.setPerfilUser(determinePerfilFromEmail(registerDTO.getEmail()));
        user.setStatusUser(StatusUser.INATIVO);

        String token = UUID.randomUUID().toString();
        user.setTokenConfirmacao(token);
        user.setDataExpiracaoTokenConfirmacao(LocalDateTime.now().plusHours(EXPIRATION_HOURS));

        userRepository.save(user);

        emailService.sendConfirmationEmail(user.getEmail(), token, user.getNome());
    }

    @Override
    @Transactional
    public UserResponseDTO confirmUser(String token) {

        User user = userRepository.findByTokenConfirmacao(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token de confirmação inválido."));

        if (user.getStatusUser() == StatusUser.ATIVO) {
            throw new BusinessRuleException("Esta conta já foi ativada.");
        }

        if (LocalDateTime.now().isAfter(user.getDataExpiracaoTokenConfirmacao())) {
            throw new BusinessRuleException("Token de confirmação expirado. Por favor, registre-se novamente.");
        }

        user.setStatusUser(StatusUser.ATIVO);
        user.setTokenConfirmacao(null);
        user.setDataExpiracaoTokenConfirmacao(null);

        User activatedUser = userRepository.save(user);
        return toResponseDTO(activatedUser);
    }

 
    @Override
    public UserLoginResponseDTO login(UserLoginDTO loginDTO) {

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginDTO.getEmail(),
                loginDTO.getSenha()
            )
        );

        User user = (User) authentication.getPrincipal();

        String token = jwtService.generateToken(user);
        return new UserLoginResponseDTO(token);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }

    @Override
    public UserResponseDTO getMyInfo(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return toResponseDTO(user);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public UserResponseDTO updateUserRole(Long userId, UserRoleUpdateDTO roleUpdateDTO) {
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + userId + " não encontrado."));

        PerfilUser newRole = roleUpdateDTO.getPerfilUser();

        validateRoleAssignment(userToUpdate, newRole);

        userToUpdate.setPerfilUser(newRole);
        User updatedUser = userRepository.save(userToUpdate);
        
        return toResponseDTO(updatedUser);
    }

    @Override
    public List<UserResponseDTO> listarPerfisUsuarios() {
        List<User> usuarios = userRepository.findAll();
        if (usuarios.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum Usuário encontrado.");
        }
        
        return usuarios.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

   @Override
    @Transactional(readOnly = true)
    public UserInteresseResponseDTO getUserInteresses(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + userId + " não encontrado."));
        List<ModalidadeResponseDTO> interessesList = user.getInteresses()
            .stream()
            .map(modalidade -> modelMapper.map(modalidade, ModalidadeResponseDTO.class)) // <-- MUDANÇA AQUI
            .collect(Collectors.toList());

        return new UserInteresseResponseDTO(interessesList);
    }

    @Override
    @Transactional
    public UserInteresseResponseDTO updateUserInteresses(Long userId, UserInteresseUpdateDTO interessesDTO) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + userId + " não encontrado."));
        
        Set<Modalidade> modalidades = interessesDTO.getModalidadeIds()
            .stream()
            .map(id -> modalidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modalidade com ID " + id + " não encontrada.")))
            .collect(Collectors.toSet());

        user.setInteresses(modalidades);

        User updatedUser = userRepository.save(user);

        List<ModalidadeResponseDTO> interessesList = updatedUser.getInteresses()
            .stream()
            .map(modalidade -> modelMapper.map(modalidade, ModalidadeResponseDTO.class)) // <-- MUDANÇA AQUI
            .collect(Collectors.toList());

        return new UserInteresseResponseDTO(interessesList);
    }

    @Override
    public void writeUsersCsv(Writer writer) {
        List<UserResponseDTO> userProfiles = this.listarPerfisUsuarios();
        
        PrintWriter printWriter = new PrintWriter(writer);

        CsvGenerator.generateUserProfilesCsv(userProfiles, printWriter);

        printWriter.flush();

        if (printWriter.checkError()) {
            throw new CsvGenerationException("Erro ao escrever dados no arquivo CSV.");
        }
    }

    private UserResponseDTO toResponseDTO(User user) {
        return modelMapper.map(user, UserResponseDTO.class);
    }

    private PerfilUser determinePerfilFromEmail(String email) {
        if (email.contains("@ifsp.edu.br")) {
            return PerfilUser.FUNCIONARIO;
        } else if (email.contains("@aluno.ifsp.edu.br")) {
            return PerfilUser.ALUNO;
        } else {
            throw new BusinessRuleException("Domínio de email não reconhecido.");
        }
    }

    private void validateRoleAssignment(User user, PerfilUser newRole) {
        String email = user.getEmail();
        
        if (newRole == PerfilUser.ADMIN || newRole == PerfilUser.GESTOR_EVENTOS) {
            if (!email.endsWith("@ifsp.edu.br")) {
                throw new BusinessRuleException("Apenas funcionários podem ser atribuídos como Administrador ou Gestor de Eventos.");
            }
        }
        if (newRole == PerfilUser.FUNCIONARIO) {
            if (email.endsWith("@aluno.ifsp.edu.br")) {
                throw new BusinessRuleException("Um usuário com e-mail de aluno não pode ser atribuído ao perfil de Funcionário.");
            }
        }
        if (newRole == PerfilUser.ALUNO) {
            if (!email.endsWith("@aluno.ifsp.edu.br") && email.contains("@ifsp.edu.br")) {
                throw new BusinessRuleException("Um usuário com e-mail institucional sem @aluno nao pode ser atribuído ao perfil de Aluno.");
            }
        }
    }
}
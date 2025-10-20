package br.ifsp.events.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

import br.ifsp.events.dto.modalidade.ModalidadeDTO;
import br.ifsp.events.dto.user.UserInteresseResponseDTO;
import br.ifsp.events.dto.user.UserInteresseUpdateDTO;
import br.ifsp.events.dto.user.UserLoginDTO;
import br.ifsp.events.dto.user.UserLoginResponseDTO;
import br.ifsp.events.dto.user.UserRegisterDTO;
import br.ifsp.events.dto.user.UserResponseDTO;
import br.ifsp.events.dto.user.UserRoleUpdateDTO;
import br.ifsp.events.exception.BusinessRuleException;
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
    public void registerUser(UserRegisterDTO registerDTO) {

        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new BusinessRuleException("Este email já está em uso.");
        }

        User user = new User();
        user.setNome(registerDTO.getNome());
        user.setEmail(registerDTO.getEmail());
        user.setSenha(passwordEncoder.encode(registerDTO.getSenha()));

        if(registerDTO.getEmail().contains("@ifsp.edu.br")) {
            user.setPerfilUser(PerfilUser.FUNCIONARIO);
        }else if(registerDTO.getEmail().contains("@aluno.ifsp.edu.br")) {
            user.setPerfilUser(PerfilUser.ALUNO);
        }else {
            throw new BusinessRuleException("Domínio de email não reconhecido.");
        } 
        user.setStatusUser(StatusUser.INATIVO);

        String token = UUID.randomUUID().toString();
        user.setTokenConfirmacao(token);
        user.setDataExpiracaoTokenConfirmacao(LocalDateTime.now().plusHours(EXPIRATION_HOURS));

        userRepository.save(user);
    
        emailService.sendConfirmationEmail(user.getEmail(), token, user.getNome());
    }

    @Override
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
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginDTO.getEmail(),
                loginDTO.getSenha()
            )
        );
        
        User user = userRepository.findByEmail(loginDTO.getEmail()).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
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

    //regras de negócio:
    //funcionario->gestor_eventos, admin, comissao_tecnica
    //aluno->comissao_tecnica
    //aluno nao pode ser funcionario nem vice versa
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO updateUserRole(Long userId, UserRoleUpdateDTO roleUpdateDTO) {
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + userId + " não encontrado."));

        PerfilUser newRole = roleUpdateDTO.getPerfilUser();

        if (newRole == PerfilUser.ADMIN || newRole == PerfilUser.GESTOR_EVENTOS) {
            if (!userToUpdate.getEmail().endsWith("@ifsp.edu.br")) {
                throw new BusinessRuleException("Apenas funcionários podem ser atribuídos como Administrador ou Gestor de Eventos.");
            }
        }
        if (newRole == PerfilUser.FUNCIONARIO) {
            if (userToUpdate.getEmail().endsWith("@aluno.ifsp.edu.br")) {
                throw new BusinessRuleException("Um usuário com e-mail de aluno não pode ser atribuído ao perfil de Funcionário.");
            }
        }
        if (newRole == PerfilUser.ALUNO) {
            if (!userToUpdate.getEmail().endsWith("@aluno.ifsp.edu.br")) {
                throw new BusinessRuleException("Um usuário com e-mail institucional sem @aluno nao pode ser atribuído ao perfil de Aluno.");
            }
        }
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
                .map(u -> new UserResponseDTO(
                        u.getId(),
                        u.getNome(),
                        u.getEmail(),
                        u.getPerfilUser()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserInteresseResponseDTO getUserInteresses(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + userId + " não encontrado."));
        
        Set<ModalidadeDTO> interessesDTO = user.getInteresses()
            .stream()
            .map(modalidade -> modelMapper.map(modalidade, ModalidadeDTO.class))
            .collect(Collectors.toSet());

        List<ModalidadeDTO> interessesList = new ArrayList<>(interessesDTO);
        return new UserInteresseResponseDTO(interessesList);
    }

    @Override
    @Transactional
    public UserInteresseResponseDTO updateUserInteresses(Long userId, UserInteresseUpdateDTO interessesDTO) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + userId + " não encontrado."));
        
        // Busca as Modalidades válidas pelos IDs informados
        Set<Modalidade> modalidades = interessesDTO.getModalidadeIds()
            .stream()
            .map(id -> modalidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modalidade com ID " + id + " não encontrada.")))
            .collect(Collectors.toSet());

        user.setInteresses(modalidades);
        User updatedUser = userRepository.save(user);

        Set<ModalidadeDTO> interessesAtualizadosDTO = updatedUser.getInteresses()
            .stream()
            .map(modalidade -> modelMapper.map(modalidade, ModalidadeDTO.class))
            .collect(Collectors.toSet());

        List<ModalidadeDTO> interessesList = new ArrayList<>(interessesAtualizadosDTO);
        return new UserInteresseResponseDTO(interessesList);
    }

    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
            user.getId(),
            user.getNome(),
            user.getEmail(),
            user.getPerfilUser()
        );
    }
}
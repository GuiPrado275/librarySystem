package com.guilherme.librarySystem.services;

import com.guilherme.librarySystem.models.User;
import com.guilherme.librarySystem.models.dto.UserCreateDTO;
import com.guilherme.librarySystem.models.dto.UserUpdateDTO;
import com.guilherme.librarySystem.models.enums.ProfileEnum;
import com.guilherme.librarySystem.repositories.UserRepository;
import com.guilherme.librarySystem.security.UserSpringSecurity;
import com.guilherme.librarySystem.services.exceptions.AuthorizationException;
import com.guilherme.librarySystem.services.exceptions.DataBindingViolationException;
import com.guilherme.librarySystem.services.exceptions.ObjectNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    public User findById(Long id){
        UserSpringSecurity userSpringSecurity = autheticated();
        if (Objects.isNull(userSpringSecurity)
                || !userSpringSecurity.hasRole(ProfileEnum.ADMIN) && !id.equals(userSpringSecurity.getId())) {
            throw new AuthorizationException("Acesso negado");
        }
        Optional<User> user = this.userRepository.findById(id);
        return user.orElseThrow(() -> new ObjectNotFoundException(
                "Usuário não encontrado! Id: " + id + ", Tipo: " + User.class.getName()));

    }

    @Transactional
    public User create(User user){
        if (this.userRepository.existsByEmail(user.getEmail())) {
            throw new AuthorizationException("E-mail já cadastrado!");
        }
        user.setId(null);
        user.setPassword(this.bCryptPasswordEncoder.encode(user.getPassword()));
        user.setProfiles(Stream.of(ProfileEnum.USER.getCode()).collect(Collectors.toSet()));
        user = this.userRepository.save(user);
        return user;
    }

    @Transactional
    public User update(User updatedUser){
        User existingUser = findById(updatedUser.getId());
        existingUser.setPassword(this.bCryptPasswordEncoder.encode(updatedUser.getPassword()));
        return this.userRepository.save(existingUser);
    }

    public void delete(Long id){
        findById(id);
        try{
            this.userRepository.deleteById(id);
        } catch (Exception e){
            throw new DataBindingViolationException("O usuário não pode ser deletado, pois ele tem empréstimos abertos!");
        }
    }

    public static UserSpringSecurity autheticated(){
        try{
            return (UserSpringSecurity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e){
            return null;
        }
    }

    public User fromDTO(@Valid UserCreateDTO obj) {
        User user = new User();
        user.setName(obj.name());
        user.setEmail(obj.email());
        user.setPassword(obj.password());
        return user;
    }

    public User fromDTO(@Valid UserUpdateDTO obj){
        User user = new User();
        user.setId(obj.id());
        user.setPassword(obj.password());
        return user;
    }

}

package com.guilherme.librarySystem.models;

import com.fasterxml.jackson.annotation.JsonProperty;
//import com.guilherme.librarySystem.models.enums.ProfileEnum;
import com.guilherme.librarySystem.models.enums.ProfileEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.Email;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = User.TABLE_NAME) //database table
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {

    public static final String TABLE_NAME = "users";

    @Column(name = "name", length = 100, nullable = false)
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres.")
    @NotBlank(message = "O nome não pode estar em branco.")
    private String name;

    @Id
    @Column(name = "id", unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY) //id is randomly generated
    private Long id;

    @Email(message = "O e-mail deve ser válido!")
    @Column(name = "email", length = 50, nullable = false, unique = true)
    @Size(min = 5, max = 50, message = "O e-mail deve ter entre 5 e 50 caracteres.")
    @NotBlank(message = "O e-mail não pode estar em branco.")
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password", nullable = false)
    @Size(min = 8, max = 60, message = "A senha deve ter entre 8 e 60 caracteres.")
    @NotBlank(message = "A senha não pode estar em branco.")
    private String password;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "profile", nullable = false)
    @ElementCollection(fetch = FetchType.EAGER) //load roles
    @CollectionTable(name = "user_profile")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Set<Integer> profiles = new HashSet<>(); //list of unique values

    public Set<ProfileEnum> getProfiles() {
        return this.profiles.stream().map(x -> ProfileEnum.toEnum(x)).collect(Collectors.toSet());
        // Transforms the Set<Integer> into a manipulable stream
        // .map() applies the function to each item (x)
        // (x -> ProfileEnum.toEnum(x)) is the function that transforms the value x (a numeric code) into a ProfileEnum value
        // Example: If x is 1, the function will call ProfileEnum.toEnum(1), which returns ProfileEnum.ADMIN
        // collect() is a terminal operation that converts the Stream back into a collection
        // Collectors.toSet() gathers the results of the stream into a Set, i.e.,
        // a collection that does not allow duplicate elements

    }

    public void AddProfile(ProfileEnum profileEnum) {
        this.profiles.add(profileEnum.getCode());
    }
    // In other words, the code 1 or 2 (depending on which ProfileEnum value was passed)
    // will be added to the profiles Set<Integer>, allowing the user to have a role
    // Basically, it adds a profile to the user and saves it
}

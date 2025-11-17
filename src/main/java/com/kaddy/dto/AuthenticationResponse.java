package com.kaddy.dto;

import com.kaddy.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {

    private String token;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private Long userId;
    private List<String> permissions;
}

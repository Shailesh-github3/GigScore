package com.org.gigscore.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDTO {
    private Long userId;
    private String name;
    private String email;
    private String token;
}

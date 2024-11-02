package com.pequla.dlaw.model.backend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BanModel {
    private Integer id;
    private UserModel user;
    private AdminModel admin;
    private String reason;
    private String createdAt;
}

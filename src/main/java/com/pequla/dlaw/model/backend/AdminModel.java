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
public class AdminModel {
    private Integer id;
    private UserModel user;
    private GuildModel guild;
    private LocalDateTime createdAt;
}

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
public class DataModel {
    private Integer id;
    private String uuid;
    private UserModel user;
    private GuildModel guild;
    private String createdAt;
}

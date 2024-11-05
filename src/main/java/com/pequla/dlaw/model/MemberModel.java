package com.pequla.dlaw.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberModel {
    private String id;
    private String name;
    private String joinedAt;
}
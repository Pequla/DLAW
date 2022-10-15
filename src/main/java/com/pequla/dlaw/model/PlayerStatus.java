package com.pequla.dlaw.model;

import lombok.*;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PlayerStatus {

    private int max;
    private int online;
    private Set<PlayerData> list;
}

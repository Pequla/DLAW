package com.pequla.dlaw.model.backend;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ErrorModel {
    private String name;
    private String message;
    private String path;
    private Long timestamp;
}

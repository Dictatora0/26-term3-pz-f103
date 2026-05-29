package com.iteaj.framework.logger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PushParams {

    private String type;

    private String subType;

    private String message;

    private String level;

    private String thread;

    private String timestamp;

    private List<String> stackTrace;
}

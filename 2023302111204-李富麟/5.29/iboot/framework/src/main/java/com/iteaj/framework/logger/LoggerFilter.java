package com.iteaj.framework.logger;

import com.iteaj.framework.IVOption;

import java.util.Collections;
import java.util.List;

public interface LoggerFilter {

    String type();

    String name();

    boolean filter(PushParams params);

    default List<IVOption> subOptions() {
        return Collections.emptyList();
    }
}

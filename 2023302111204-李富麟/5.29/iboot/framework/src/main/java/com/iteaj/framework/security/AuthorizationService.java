package com.iteaj.framework.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface AuthorizationService {

    default List<String> getRoles(Serializable userId) {
        return Collections.EMPTY_LIST;
    }

    List<String> getPermissions(Serializable userId);
}

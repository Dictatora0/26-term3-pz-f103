package com.iteaj.framework.spi.auth.handle;

import com.iteaj.framework.consts.CoreConst;
import com.iteaj.framework.spi.auth.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * create time: 2021/3/24
 *  普通的账号密码认证
 * @author iteaj
 * @since 1.0
 */
public class AccountAuthHandler extends DefaultAuthHandler {

    public AccountAuthHandler(List<WebAuthAction> actions) {
        super(actions);
    }

}

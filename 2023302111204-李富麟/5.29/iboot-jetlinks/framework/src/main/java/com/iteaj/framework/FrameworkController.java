package com.iteaj.framework;

import com.iteaj.framework.logger.LoggerManager;
import com.iteaj.framework.result.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 框架层提供的功能
 */
@Controller
@RequestMapping("/core/framework")
public class FrameworkController extends BaseController{

    /**
     * 日志过滤列表
     * @return 列表
     */
    @ResponseBody
    @GetMapping("loggers")
    public Result<List<IVOption>> loggers() {
        return success(LoggerManager.filterOptions());
    }
}

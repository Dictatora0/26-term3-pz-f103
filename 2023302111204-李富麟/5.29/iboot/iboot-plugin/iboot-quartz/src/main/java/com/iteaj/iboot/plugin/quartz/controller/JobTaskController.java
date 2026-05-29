package com.iteaj.iboot.plugin.quartz.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.PageResult;
import com.iteaj.framework.result.Result;
import com.iteaj.iboot.plugin.quartz.entity.JobTask;
import com.iteaj.iboot.plugin.quartz.jobs.SpringSupportJob;
import com.iteaj.iboot.plugin.quartz.scheduler.SchedulerManager;
import com.iteaj.iboot.plugin.quartz.service.IJobTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/quartz/task")
public class JobTaskController extends BaseController {

    private final IJobTaskService jobTaskService;
    private final SpringSupportJob springSupportJob;
    private final SchedulerManager schedulerManager;

    public JobTaskController(IJobTaskService jobTaskService
            , SpringSupportJob springSupportJob, SchedulerManager schedulerManager) {
        this.jobTaskService = jobTaskService;
        this.springSupportJob = springSupportJob;
        this.schedulerManager = schedulerManager;
    }

    /**
     * 获取定时任务列表
     * @param page
     * @param entity
     * @return
     */
    @GetMapping("view")
    public PageResult<Page<JobTask>> view(Page page, JobTask entity) {
        return this.jobTaskService.page(page, entity);
    }

    /**
     * 获取定时任务详情
     * @param id
     * @return
     */
    @GetMapping("detail")
    public DetailResult<JobTask> detail(Long id) {
        return this.jobTaskService.getById(id);
    }

    /**
     * 新增或者修改定时任务
     * @param entity
     * @return
     */
    @PostMapping("addOrUpdate")
    public BooleanResult addOrUpdate(@RequestBody JobTask entity) {
        return this.jobTaskService.saveOrUpdate(entity);
    }

    /**
     * 删除定时任务
     * @param ids
     * @return
     */
    @PostMapping("del")
    public BooleanResult del(@RequestBody List<Long> ids) {
        return this.jobTaskService.removeByIds(ids);
    }

    /**
     * 修改状态
     * @param status 状态
     * @param entity
     * @return
     */
    @PostMapping("/status/{status}")
    public Result<Object> status(@PathVariable String status, @RequestBody JobTask entity) {
        if("paused".equals(status)) {
            this.schedulerManager.pauseJob(entity.getJobName());
        } else if("resume".equals(status)) {
            this.schedulerManager.resumeJob(entity.getJobName());
        } else {
            return fail("不支持的操作");
        }

        return success("修改状态成功");
    }

    /**
     * 运行一次
     * @param entity
     * @return
     */
    @PostMapping("once")
    public Result once(@RequestBody JobTask entity) {
        this.schedulerManager.fireJob(entity.getJobName());
        return success("触发成功");
    }

    /**
     * 可执行方法列表
     * @return
     */
    @GetMapping("taskMethods")
    public Result<List<Map<String, String>>> taskMethods() {
        return success(springSupportJob.getOptionValues());
    }

}

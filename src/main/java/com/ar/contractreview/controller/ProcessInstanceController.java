package com.ar.contractreview.controller;

import com.ar.contractreview.entity.ProcessInstance;
import com.ar.contractreview.entity.ProcessTask;
import com.ar.contractreview.result.R;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.service.ProcessInstanceService;
import com.ar.contractreview.service.ProcessTaskService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 流程实例表 前端控制器
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/process/instances")
public class ProcessInstanceController {

    private static final java.time.format.DateTimeFormatter DATE_TIME_FORMATTER =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Autowired
    private ProcessInstanceService processInstanceService;
    @Autowired
    private ProcessTaskService processTaskService;


    @GetMapping
    public R list(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "size",defaultValue = "10") Integer size,
            @RequestParam(value = "contractId",required = false) Long contractId,
            @RequestParam(value = "contractNo",required = false) String contractNo,
            @RequestParam(value = "status",required = false) String status
    ){
        QueryWrapper<ProcessInstance> wrapper = new QueryWrapper<>();
        wrapper.eq(contractId != null,"contract_id",contractId)
                .like(StringUtils.hasText(contractNo),"contract_no",contractNo)
                .eq(StringUtils.hasText(status),"status",status);

        Page<ProcessInstance> pageResult = processInstanceService.page(new Page<>(page,size),wrapper);
        
        List<Map<String, Object>> list = new ArrayList<>();
        for (ProcessInstance ins : pageResult.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", ins.getId());
            item.put("instanceCode", ins.getInstanceCode());
            item.put("processName", ins.getProcessName());
            item.put("contractId", ins.getContractId());
            item.put("contractNo", ins.getContractNo());
            item.put("status", ins.getStatus());
            item.put("currentNodeName", ins.getCurrentNodeName());
            item.put("startTime", ins.getStartTime() == null ? null : ins.getStartTime().format(DATE_TIME_FORMATTER));
            item.put("creatorName", ins.getCreatorName());
            list.add(item);
        }

        return R.ok()
                .data("list", list)
                .data("total", pageResult.getTotal())
                .data("page", pageResult.getCurrent())
                .data("size", pageResult.getSize());
    }

    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Long id){
        ProcessInstance instance = processInstanceService.getById(id);
        if (instance == null){
            return R.fail(ResponseCode.DATA_NOT_EXIST);
        }
        QueryWrapper<ProcessTask> taskWrapper = new QueryWrapper<>();
        taskWrapper.
                eq("instance_id",id);
        List<ProcessTask> tasks = processTaskService.list(taskWrapper);
        // ③ 组装返回：主记录 13 字段 + tasks 8 字段
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", instance.getId());
        data.put("instanceCode", instance.getInstanceCode());
        data.put("processId", instance.getProcessId());
        data.put("processName", instance.getProcessName());
        data.put("contractId", instance.getContractId());
        data.put("contractNo", instance.getContractNo());
        data.put("status", instance.getStatus());
        data.put("currentNodeId", instance.getCurrentNodeId());
        data.put("currentNodeName", instance.getCurrentNodeName());
        data.put("startTime", instance.getStartTime() == null ? null
                : instance.getStartTime().format(DATE_TIME_FORMATTER));
        data.put("endTime", instance.getEndTime() == null ? null
                : instance.getEndTime().format(DATE_TIME_FORMATTER));
        data.put("creatorId", instance.getCreatorId());
        data.put("creatorName", instance.getCreatorName());

        // tasks 子列表（8 字段）
        List<Map<String, Object>> taskList = new ArrayList<>();
        for (ProcessTask task : tasks) {
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("id", task.getId());
            t.put("taskCode", task.getTaskCode());
            t.put("nodeName", task.getNodeName());
            t.put("assigneeName", task.getAssigneeName());
            t.put("status", task.getStatus());
            t.put("result", task.getResult());
            t.put("startTime", task.getStartTime() == null ? null
                    : task.getStartTime().format(DATE_TIME_FORMATTER));
            t.put("completeTime", task.getCompleteTime() == null ? null
                    : task.getCompleteTime().format(DATE_TIME_FORMATTER));
            t.put("comment", task.getComment());
            taskList.add(t);
        }
        data.put("tasks", taskList);

        return R.ok().data(data);
    }
}

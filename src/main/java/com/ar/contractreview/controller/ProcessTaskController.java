package com.ar.contractreview.controller;

import com.ar.contractreview.entity.*;
import com.ar.contractreview.result.R;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * <p>
 * 流程任务表 前端控制器
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/process/tasks")
public class ProcessTaskController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ProcessTaskService processTaskService;
    @Autowired
    private ContractContractService contractContractService;
    @Autowired
    private ProcessNodeService processNodeService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ProcessTaskAssignService processTaskAssignService;




    @GetMapping("/pending")
    public R list(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "size",defaultValue = "10") Integer size,
            @RequestParam(value = "userId",required = false) Long userId
    ){
        QueryWrapper<ProcessTask> wrapper = new QueryWrapper<>();
        wrapper.eq(userId != null,"assignee_id",userId);

        Page<ProcessTask> pageResult = processTaskService.page(new Page<>(page,size),wrapper);

        List<Long> contractIds = new ArrayList<>();
        for (ProcessTask tasks : pageResult.getRecords()){
            if (tasks.getContractId() != null && !contractIds.contains(tasks.getContractId())){
                contractIds.add(tasks.getContractId());
            }
        }

        Map<Long,String> contractNameMap = new LinkedHashMap<>();
        if (!contractIds.isEmpty()){
            for (ContractContract c : contractContractService.listByIds(contractIds)){
                contractNameMap.put(c.getId(),c.getContractName());
            }
        }

        List<Map<String,Object>> list = new ArrayList<>();
        for (ProcessTask task : pageResult.getRecords()){
            Map<String,Object> item = new LinkedHashMap<>();
            item.put("id",task.getId());
            item.put("taskCode",task.getTaskCode());
            item.put("nodeName",task.getNodeName());
            item.put("contractId",task.getContractId());
            item.put("contractNo",task.getContractNo());
            item.put("contractName", contractNameMap.get(task.getContractId()));
            item.put("assigneeName",task.getAssigneeName());
            item.put("status",task.getStatus());
            item.put("priority",task.getPriority());
            item.put("deadline",task.getDeadline() == null ? null : task.getDeadline().format(DATE_TIME_FORMATTER));
            item.put("startTime",task.getStartTime() == null ? null : task.getStartTime().format(DATE_TIME_FORMATTER));
            list.add(item);
        }

        return R.ok()
                .data("list",list)
                .data("total",pageResult.getTotal())
                .data("page",pageResult.getCurrent())
                .data("size",pageResult.getSize());
    }

    @PostMapping("/{id}/handle")
    public R handle(@PathVariable("id") Long id, @RequestBody Map<String,Object> body){
        ProcessTask task = processTaskService.getById(id);
        if (task == null){
            return R.fail(ResponseCode.DATA_NOT_EXIST);
        }

        String result = (String) body.get("result");
        if (!StringUtils.hasText(result)){
            return R.fail(ResponseCode.PARAMETER_EXCEPTION);
        }
        String comment = (String) body.get("comment");

        task.setStatus("COMPLETED");
        task.setResult(result);
        task.setCompleteTime(LocalDateTime.now());
        processTaskService.updateById(task);


        ProcessNode curNode = processNodeService.getById(task.getNodeId());
        QueryWrapper<ProcessNode> nodeWrapper = new QueryWrapper<>();
        nodeWrapper.eq("process_id",task.getProcessId())
                .gt("sort_order",curNode.getSortOrder())
                .orderByAsc("sort_order")
                .last("LIMIT 1");
        ProcessNode nextNode = processNodeService.getOne(nodeWrapper);
        return R.ok()
                .data("id",id)
                .data("status",task.getStatus())
                .data("result",task.getResult())
                .data("completeTime",task.getCompleteTime() == null ? null : task.getCompleteTime().format(DATE_TIME_FORMATTER))
                .data("nextNodeId",nextNode == null ? null : nextNode.getId())
                .data("nextNodeName",nextNode == null ? null : nextNode.getNodeName())
                .data("nextAssigneeName",nextNode == null ? null : nextNode.getAssigneeName());
    }

    @PostMapping("/{id}/assign")
    public R assign(@PathVariable("id") Long id,@RequestBody Map<String,Object> body){

        ProcessTask task = processTaskService.getById(id);
        if (task == null){
            return R.fail(ResponseCode.DATA_NOT_EXIST);
        }


        if (body.get("userId") == null){
            return R.fail(ResponseCode.PARAMETER_EXCEPTION);
        }
        Long userId = ((Number)body.get("userId")).longValue();

        SysUser user = sysUserService.getById(userId);
        if (user == null){
            return R.fail(ResponseCode.PARAMETER_EXCEPTION);
        }

        task.setAssigneeId(userId);
        task.setAssigneeName(user.getName());
        task.setStatus("PROCESSING");
        processTaskService.updateById(task);

        String assignType = (String) body.getOrDefault("assignType","PRIMARY");

        ProcessTaskAssign assign = new ProcessTaskAssign();
        assign.setTaskId(id);
        assign.setUserId(userId);
        assign.setUserName(user.getName());
        assign.setAssignType(assignType);
        processTaskAssignService.save(assign);

        return R.ok()
                .data("id",id)
                .data("assigneeId",userId)
                .data("assigneeName",user.getName())
                .data("assignType",assignType)
                .data("status",task.getStatus());
    }
}

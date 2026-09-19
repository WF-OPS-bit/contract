package com.ar.contractreview.controller;

import com.ar.contractreview.entity.ProcessConfig;
import com.ar.contractreview.entity.ProcessNode;
import com.ar.contractreview.result.R;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.service.ProcessConfigService;
import com.ar.contractreview.service.ProcessNodeService;
import com.ar.contractreview.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * <p>
 * 流程配置表 前端控制器
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/process/configs")
public class ProcessConfigController {

    @Autowired
    private ProcessConfigService processConfigService;
    @Autowired
    private ProcessNodeService processNodeService;


    //获取流程配置列表
    @GetMapping
    public R list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "processName", required = false) String processName,
            @RequestParam(value = "contractType", required = false) String contractType,
            @RequestParam(value = "status", required = false) String status
    ){
        QueryWrapper<ProcessConfig> wrapper = new QueryWrapper<>();

        if (!StringUtils.isEmpty(processName)){
            wrapper.like("process_name",processName);
        }
        if (!StringUtils.isEmpty(contractType)){
            wrapper.like("contract_type",contractType);
        }
        if (!StringUtils.isEmpty(status)){
            wrapper.eq("status",status);
        }
        IPage<ProcessConfig> pageResult = processConfigService.page(new Page<>(page,size),wrapper);
        return R.ok().data("list",pageResult.getRecords()).data("total",pageResult.getTotal());
    }

    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Long id){
        ProcessConfig config = processConfigService.getById(id);
        if (config == null){
            return R.fail(ResponseCode.PARAMETER_EXCEPTION);
        }

        List<ProcessNode> nodes = processNodeService.list(
                new QueryWrapper<ProcessNode>().eq("process_id",id).orderByAsc("sort_order")
        );

        return R.ok().data("data",config).data("nodes",nodes);
    }

    @Transactional
    @PostMapping
    public R add(@RequestBody ProcessConfig config){

        if (StringUtils.isEmpty(config.getStatus())) {
            config.setStatus("ACTIVE");
        }

        processConfigService.save(config);

        List<ProcessNode> nodes = config.getNodes();
        if (nodes != null && !nodes.isEmpty()){
            for (ProcessNode node:nodes){
                node.setProcessId(config.getId());
            }
            processNodeService.saveBatch(nodes);
        }
        return R.ok()
                .data("id",config.getId())
                .data("processCode",config.getProcessCode())
                .data("processName",config.getProcessName())
                .data("status",config.getStatus());
    }

    @PutMapping("/{id}")
    public R update(@PathVariable("id") Long id,@RequestBody ProcessConfig config){
        config.setId(id);
        processConfigService.updateById(config);
        return R.ok()
                .data("id",config.getId())
                .data("processName",config.getProcessName())
                .data("status",config.getStatus());
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable("id") Long id){
        processConfigService.removeById(id);
        return R.ok().data("data",true);
    }

}


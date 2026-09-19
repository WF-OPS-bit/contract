package com.ar.contractreview.controller;

import com.ar.contractreview.entity.ProcessNode;
import com.ar.contractreview.result.R;
import com.ar.contractreview.service.ProcessNodeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 流程节点表 前端控制器
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/process/nodes")
public class ProcessNodeController {

    @Autowired
    private ProcessNodeService processNodeService;

    @GetMapping
    public R list(@RequestParam(value = "processId") Long processId){
        List<ProcessNode> nodes = processNodeService.list(
                new QueryWrapper<ProcessNode>()
                        .eq("process_id",processId)
                        .orderByAsc("sort_order")
        );
        return R.ok().data(nodes);
    }

}

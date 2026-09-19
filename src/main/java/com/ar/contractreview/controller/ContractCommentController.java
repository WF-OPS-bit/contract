package com.ar.contractreview.controller;

import com.ar.contractreview.entity.ContractComment;
import com.ar.contractreview.result.R;
import com.ar.contractreview.service.ContractCommentService;
import com.ar.contractreview.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 合同批注表 前端控制器
 * </p>
 * 说明:响应字段严格按接口文档第 7 章输出(文档名 createTime 对应实体 createdTime)。
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/comments")
public class ContractCommentController {

    @Autowired
    private ContractCommentService commentService;

    /**
     * 7.1 获取批注列表
     * GET /comments/{contractId}
     * 文档响应字段:id/contractId/userId/userName/userRole/commentType/content/page/position/status/createTime
     */
    @GetMapping("/{contractId}")
    public R list(@PathVariable Long contractId) {
        List<Map<String, Object>> list = commentService.listByContract(contractId).stream()
                .map(this::toListVO)
                .collect(Collectors.toList());
        return R.ok().data(list);
    }

    /**
     * 7.2 添加批注
     * POST /comments
     * 文档响应字段:id/contractId/userName/commentType/content/status/createTime
     */
    @PostMapping
    public R add(@RequestBody ContractComment comment) {
        // 服务端统一填充批注人信息与状态,避免前端伪造
        comment.setUserId(SecurityUtils.currentUserId())
                .setUserName(SecurityUtils.currentUsername())
                .setStatus("ACTIVE")
                .setCreatedTime(LocalDateTime.now())
                .setUpdatedTime(LocalDateTime.now());
        commentService.save(comment);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", comment.getId());
        data.put("contractId", comment.getContractId());
        data.put("userName", comment.getUserName());
        data.put("commentType", comment.getCommentType());
        data.put("content", comment.getContent());
        data.put("status", comment.getStatus());
        data.put("createTime", comment.getCreatedTime());
        return R.ok().data(data);
    }

    /**
     * 7.3 更新批注
     * PUT /comments/{id}
     * 文档响应字段:id/content/status
     */
    @PutMapping("/{id}")
    public R update(@PathVariable Long id, @RequestBody ContractComment comment) {
        comment.setId(id);
        comment.setUpdatedTime(LocalDateTime.now());
        commentService.updateById(comment);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("content", comment.getContent());
        data.put("status", comment.getStatus());
        return R.ok().data(data);
    }

    /**
     * 7.4 删除批注(逻辑删除:status 置为 DELETED,保留数据用于审计)
     * DELETE /comments/{id}
     */
    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        ContractComment c = commentService.getById(id);
        if (c != null) {
            c.setStatus("DELETED");
            commentService.updateById(c);
        }
        // 幂等设计:重复删除不报错
        return R.ok().data(true);
    }

    /**
     * 列表项转换为接口文档约定字段(文档名 createTime ↔ 实体字段 createdTime)
     */
    private Map<String, Object> toListVO(ContractComment c) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", c.getId());
        item.put("contractId", c.getContractId());
        item.put("userId", c.getUserId());
        item.put("userName", c.getUserName());
        item.put("userRole", c.getUserRole());
        item.put("commentType", c.getCommentType());
        item.put("content", c.getContent());
        item.put("page", c.getPage());
        item.put("position", c.getPosition());
        item.put("status", c.getStatus());
        item.put("createTime", c.getCreatedTime());
        return item;
    }
}

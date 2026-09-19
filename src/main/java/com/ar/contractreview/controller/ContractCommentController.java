package com.ar.contractreview.controller;

import com.ar.contractreview.entity.ContractComment;
import com.ar.contractreview.result.R;
import com.ar.contractreview.service.ContractCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 合同批注表 前端控制器
 * </p>
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
     * 查询指定合同下的所有有效批注
     * <p>
     * 请求方式：GET
     * 请求路径：/comments/{contractId}
     *
     * @param contractId 合同ID（路径参数）
     * @return 批注列表（已过滤掉已删除的批注，按创建时间升序）
     */
    @GetMapping("/{contractId}")
    public R list(@PathVariable Long contractId) {
        // 调用 Service 层查询该合同下未删除的批注列表
        List<ContractComment> list = commentService.listByContract(contractId);
        // 若前端需要 userName/userRole（实体已有 user_name/user_role 字段），直接返回实体即可
        // 说明：实体类中已包含 userName、userRole 字段，前端可直接读取，无需额外 VO 转换
        return R.ok().data(list);
    }

    /**
     * 新增一条合同批注
     * <p>
     * 请求方式：POST
     * 请求路径：/comments
     * 请求体：ContractComment JSON 对象
     *
     * @param comment 前端传入的批注对象（无需传 userId、userName、status 等字段）
     * @return 保存后的批注对象（包含数据库生成的 ID）
     */
    @PostMapping
    public R add(@RequestBody ContractComment comment) {
        // 从当前登录用户的上下文中获取用户ID和用户名，避免前端伪造
        comment.setUserId(com.ar.contractreview.utils.SecurityUtils.currentUserId())
                .setUserName(com.ar.contractreview.utils.SecurityUtils.currentUsername())
                // 初始化状态为 ACTIVE（有效）
                .setStatus("ACTIVE")
                // 设置创建时间和更新时间（由服务端统一生成，保证时间一致性）
                .setCreatedTime(LocalDateTime.now())
                .setUpdatedTime(LocalDateTime.now());
        // 持久化到数据库
        commentService.save(comment);
        // 返回保存后的对象（含自动生成的 ID），方便前端后续操作
        return R.ok().data(comment);
    }

    /**
     * 根据 ID 更新批注信息
     * <p>
     * 请求方式：PUT
     * 请求路径：/comments/{id}
     * 请求体：ContractComment JSON 对象（需包含要更新的字段）
     *
     * @param id      批注ID（路径参数）
     * @param comment 要更新的批注对象
     * @return 更新后的批注对象
     */
    @PutMapping("/{id}")
    public R update(@PathVariable Long id, @RequestBody ContractComment comment) {
        // 以路径参数 id 为准，防止前端传入的 id 与路径不一致
        comment.setId(id);
        // 根据 ID 更新非空字段（MyBatis-Plus 默认策略）
        commentService.updateById(comment);
        return R.ok().data(comment);
    }

    /**
     * 逻辑删除批注（软删除）
     * <p>
     * 请求方式：DELETE
     * 请求路径：/comments/{id}
     * 说明：并非物理删除，而是将 status 字段置为 "DELETED"，保留数据用于审计/恢复
     *
     * @param id 批注ID（路径参数）
     * @return 操作结果（true 表示成功）
     */
    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        // 先查询该批注是否存在
        ContractComment c = commentService.getById(id);
        if (c != null) {
            // 存在则进行逻辑删除：修改状态字段，而非物理删除记录
            c.setStatus("DELETED");
            commentService.updateById(c);
        }
        // 无论是否存在，都返回成功（幂等设计：重复删除不报错）
        return R.ok().data(true);
    }
}
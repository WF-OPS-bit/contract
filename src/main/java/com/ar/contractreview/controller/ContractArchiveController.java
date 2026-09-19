package com.ar.contractreview.controller;

import com.ar.contractreview.result.R;
import com.ar.contractreview.service.ContractArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 合同归档表 前端控制器
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/archives")
public class ContractArchiveController {

    /**
     * 合同归档业务服务，负责归档记录的分页查询等业务逻辑处理
     */
    @Autowired
    private ContractArchiveService archiveService;

    /**
     * 分页查询合同归档列表
     * <p>
     * 支持按合同编号、合同名称、合同类型、归档编号、存储方式等条件进行模糊/精确筛选，
     * 查询结果按分页返回，并附带总记录数、当前页码、每页条数等分页信息。
     * </p>
     *
     * @param page         当前页码，从 1 开始，默认值为 1
     * @param size         每页显示条数，默认值为 20
     * @param contractNo   合同编号（可选，模糊查询条件）
     * @param contractName 合同名称（可选，模糊查询条件）
     * @param contractType 合同类型（可选，查询条件）
     * @param archiveCode  归档编号（可选，模糊查询条件）
     * @param storageType  存储方式（可选，查询条件）
     * @return 统一响应结果 {@link R}，包含以下数据：
     *         <ul>
     *             <li>{@code list}  - 当前页的归档记录列表</li>
     *             <li>{@code total} - 符合条件的总记录数</li>
     *             <li>{@code page}  - 当前页码</li>
     *             <li>{@code size}  - 每页条数</li>
     *         </ul>
     */
    @GetMapping
    public R list(@RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer size,
                  @RequestParam(value = "contractNO", required = false) String contractNo, @RequestParam(required = false) String contractName,
                  @RequestParam(value = "contractType", required = false) String contractType, @RequestParam(required = false) String archiveCode,
                  @RequestParam(value = "storageType", required = false) String storageType) {
        // 调用业务层执行分页查询，获取归档记录分页对象
        var p = archiveService.pageArchive(page, size, contractNo, contractName, contractType, archiveCode, storageType);
        // 组装统一响应结果，返回列表数据及分页元信息
        return R.ok().data("list", p.getRecords()).data("total", p.getTotal()).data("page", p.getCurrent()).data("size", p.getSize());
    }
}
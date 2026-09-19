package com.ar.contractreview.controller;

import com.ar.contractreview.entity.ContractContract;
import com.ar.contractreview.entity.ContractReviewResult;
import com.ar.contractreview.entity.ContractRiskClause;
import com.ar.contractreview.result.R;
import com.ar.contractreview.service.ContractContractService;
import com.ar.contractreview.service.ContractReviewResultService;
import com.ar.contractreview.service.ContractRiskClauseService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * <p>
 * 统计报表 前端控制器（接口文档 14.1-14.3：概览/风险类型/月度趋势）
 * </p>
 *
 * @author wyh
 * @since 2026-09-04
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Autowired
    private ContractContractService contractContractService;
    @Autowired
    private ContractRiskClauseService contractRiskClauseService;
    @Autowired
    private ContractReviewResultService contractReviewResultService;

    @GetMapping("/overview")
    public R overview(){
        long contractTotal = contractContractService.count();

        long finished = contractContractService.count(
                new QueryWrapper<ContractContract>().in("status","APPROVED", "ARCHIVED")
        );
        int completedRate = (contractTotal == 0 ? 0 : (int)(finished * 100.0 / contractTotal));

        List<ContractContract> contracts = contractContractService.list();
        Double totalMinutes = 0.0;
        int reviewCount = 0;
        for (ContractContract contract : contracts){
            if (contract.getReviewTime() != null && contract.getUploadTime() != null){
                totalMinutes += Duration.between(contract.getUploadTime(),contract.getReviewTime()).toMinutes();
                reviewCount++;
            }
        }
        Integer avgReviewTime = (reviewCount == 0 ? null : (int)(totalMinutes/reviewCount));

        List<ContractReviewResult> reviewResults = contractReviewResultService.list();
        Map<Long,String> contractRiskMap = new HashMap<>();
        for (ContractContract c : contracts){
            contractRiskMap.put(c.getId(),c.getRiskLevel());
        }
        int matched = 0;
        int compared = 0;
        for (ContractReviewResult r : reviewResults){
            String finalRisk = contractRiskMap.get(r.getContractId());
            if (finalRisk != null){
                compared++;
                if (finalRisk.equals(r.getRiskLevel())){
                    matched++;
                }
            }
        }
        Double aiAccuracy = (compared == 0 ? null : (matched * 100.0 / compared));

        int high=0,medium=0,low=0,none=0;
        for (ContractContract c : contracts){
            String level = c.getRiskLevel();
            if ("HIGH".equals(level)){
                high++;
            } else if ("MEDIUM".equals(level)) {
                medium++;
            } else if ("LOW".equals(level)) {
                low++;
            } else if ("NONE".equals(level)) {
                none++;
            }
        }
        Map<String,Object> riskDistribution = new LinkedHashMap<>();
        riskDistribution.put("high",high);
        riskDistribution.put("medium",medium);
        riskDistribution.put("low",low);
        riskDistribution.put("none",none);

        return R.ok()
                .data("contractTotal",contractTotal)
                .data("completedRate",completedRate)
                .data("avgReviewTime",avgReviewTime)
                .data("aiAccuracy",aiAccuracy)
                .data("riskDistribution",riskDistribution);

    }

    @GetMapping("risk-types")
    public R riskTypes(){

        List<ContractRiskClause> clauses = contractRiskClauseService.list();

        Map<String,Integer> countMap = new LinkedHashMap<>();
        for (ContractRiskClause clause : clauses){
            String type = clause.getRiskType();
            if (type != null){
                countMap.put(type,countMap.getOrDefault(type,0)+1);
            }
        }

        List<Map<String,Object>> list = new ArrayList<>();
        for (Map.Entry<String,Integer> entry : countMap.entrySet()){
            Map<String,Object> item = new LinkedHashMap<>();
            item.put("name",entry.getKey());
            item.put("count",entry.getValue());
            list.add(item);
        }

        return R.ok().data(list);
    }

    @GetMapping("monthly-trend")
    public R monthlyTrend(@RequestParam(value = "year",required = false) Integer year){

        int targetYear = year == null ? LocalDate.now().getYear() : year;

        List<ContractContract> contracts = contractContractService.list();
        Map<String,Integer> monthMap = new TreeMap<>();
        for (ContractContract contract : contracts){
            if (contract.getCreatedTime() != null && contract.getCreatedTime().getYear() == targetYear) {
                String month = contract.getCreatedTime().format(MONTH_FORMATTER);
                monthMap.put(month,monthMap.getOrDefault(month,0)+1);
            }
        }

        List<Map<String,Object>> list = new ArrayList<>();
        for (Map.Entry<String,Integer> entry : monthMap.entrySet()){
            Map<String,Object> item = new LinkedHashMap<>();
            item.put("month",entry.getKey());
            item.put("count",entry.getValue());
            list.add(item);
        }

        return R.ok().data(list);

    }


}

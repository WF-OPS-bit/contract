package com.ar.contractreview.service.impl;

import com.ar.contractreview.entity.ProcessInstance;
import com.ar.contractreview.mapper.ProcessInstanceMapper;
import com.ar.contractreview.service.ProcessInstanceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 流程实例表 服务实现类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Service
public class ProcessInstanceServiceImpl extends ServiceImpl<ProcessInstanceMapper, ProcessInstance> implements ProcessInstanceService {

}

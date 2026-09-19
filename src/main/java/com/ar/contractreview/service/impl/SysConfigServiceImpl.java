package com.ar.contractreview.service.impl;

import com.ar.contractreview.entity.SysConfig;
import com.ar.contractreview.exception.BusinessException;
import com.ar.contractreview.mapper.SysConfigMapper;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.security.dto.ConfigSaveRequest;
import com.ar.contractreview.service.SysConfigService;
import com.ar.contractreview.vo.ConfigVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统配置表 服务实现类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    @Override
    public List<ConfigVO> listConfigs(String configType) {
        List<SysConfig> configs = list(
                Wrappers.<SysConfig>lambdaQuery()
                        .eq(configType != null && !configType.trim().isEmpty(), SysConfig::getConfigType, configType)
                        .orderByAsc(SysConfig::getId)
        );
        return configs.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ConfigVO getConfigDetail(Long id) {
        return toVO(requireConfig(id));
    }

    @Override
    public SysConfig addConfig(ConfigSaveRequest request) {
        validateSaveRequest(request, true);
        // 配置键唯一性校验
        checkConfigKeyUnique(request.getConfigKey().trim(), null);

        String operator = currentUserName();
        LocalDateTime now = LocalDateTime.now();
        SysConfig config = new SysConfig();
        config.setConfigKey(request.getConfigKey().trim());
        config.setConfigValue(request.getConfigValue());
        config.setConfigName(request.getConfigName().trim());
        config.setConfigType(request.getConfigType());
        config.setDescription(request.getDescription());
        config.setStatus(request.getStatus() == null ? (byte) 1 : request.getStatus());
        config.setCreatedBy(operator);
        config.setCreatedTime(now);
        config.setUpdatedBy(operator);
        config.setUpdatedTime(now);
        boolean saved = save(config);
        if (!saved) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "新增配置失败");
        }
        return config;
    }

    @Override
    public SysConfig updateConfig(Long id, ConfigSaveRequest request) {
        SysConfig config = requireConfig(id);
        validateSaveRequest(request, false);
        // 配置键唯一性校验（排除自身）
        if (request.getConfigKey() != null && !request.getConfigKey().trim().isEmpty()) {
            checkConfigKeyUnique(request.getConfigKey().trim(), id);
        }

        if (request.getConfigKey() != null && !request.getConfigKey().trim().isEmpty()) {
            config.setConfigKey(request.getConfigKey().trim());
        }
        if (request.getConfigValue() != null) {
            config.setConfigValue(request.getConfigValue());
        }
        if (request.getConfigName() != null && !request.getConfigName().trim().isEmpty()) {
            config.setConfigName(request.getConfigName().trim());
        }
        if (request.getConfigType() != null) {
            config.setConfigType(request.getConfigType());
        }
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            config.setStatus(request.getStatus());
        }
        config.setUpdatedBy(currentUserName());
        config.setUpdatedTime(LocalDateTime.now());
        boolean updated = updateById(config);
        if (!updated) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "更新配置失败");
        }
        return config;
    }

    @Override
    public void deleteConfig(Long id) {
        requireConfig(id);
        boolean removed = removeById(id);
        if (!removed) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "删除配置失败");
        }
    }

    /**
     * 查询配置，不存在则抛出业务异常
     */
    private SysConfig requireConfig(Long id) {
        if (id == null) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "配置ID不能为空");
        }
        SysConfig config = getById(id);
        if (config == null) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "配置不存在");
        }
        return config;
    }

    /**
     * 校验新增/更新配置的公共参数
     *
     * @param request 请求体
     * @param isAdd   true-新增（configKey / configName 必填），false-更新（configValue 必填）
     */
    private void validateSaveRequest(ConfigSaveRequest request, boolean isAdd) {
        if (request == null) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "请求参数不能为空");
        }
        if (isAdd && (request.getConfigKey() == null || request.getConfigKey().trim().isEmpty())) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "配置键不能为空");
        }
        if (isAdd && (request.getConfigName() == null || request.getConfigName().trim().isEmpty())) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "配置名称不能为空");
        }
        if (request.getConfigValue() == null || request.getConfigValue().trim().isEmpty()) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "配置值不能为空");
        }
    }

    /**
     * 配置键唯一性校验
     *
     * @param configKey 配置键
     * @param excludeId 需要排除的配置ID（更新时传，新增传 null）
     */
    private void checkConfigKeyUnique(String configKey, Long excludeId) {
        SysConfig exist = getOne(
                Wrappers.<SysConfig>lambdaQuery().eq(SysConfig::getConfigKey, configKey)
        );
        if (exist != null && !exist.getId().equals(excludeId)) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "配置键已存在：" + configKey);
        }
    }

    /**
     * 实体转响应对象
     */
    private ConfigVO toVO(SysConfig config) {
        ConfigVO vo = new ConfigVO();
        vo.setId(config.getId());
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigValue(config.getConfigValue());
        vo.setConfigName(config.getConfigName());
        vo.setConfigType(config.getConfigType());
        vo.setDescription(config.getDescription());
        vo.setStatus(config.getStatus());
        return vo;
    }

    /**
     * 获取当前登录用户名，未登录（内部调用等场景）时返回 system
     */
    private String currentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.ar.contractreview.security.bo.SecurityUser securityUser) {
            return securityUser.getUsername();
        }
        return "system";
    }
}

-- 由 tools/convert_sql_to_h2.py 从 contract.sql 自动生成，请勿手工修改。

INSERT INTO sys_role (id, role_name, role_code, description, status, created_by, created_time) VALUES
(1, '系统管理员', 'ADMIN', '系统配置、用户管理、权限分配，拥有全部功能', 1, 'system', NOW()),
(2, '法务主管', 'LEGAL_MANAGER', '审核规则配置、模板管理、审核报告查看、团队管理，查看/审核所有合同', 1, 'system', NOW()),
(3, '法务专员', 'LEGAL_OFFICER', '合同上传、AI审核、人工复核、提交审核意见，上传/审核/提交自己负责的合同', 1, 'system', NOW()),
(4, '业务人员', 'BUSINESS_USER', '提交合同审核申请、查看审核进度、下载审核结果，提交申请、查看自己提交的合同', 1, 'system', NOW()),
(5, '外部用户', 'EXTERNAL_USER', '提交合同、查看审核状态，仅提交和查看自己的合同', 1, 'system', NOW());
INSERT INTO sys_user (id, username, password, name, role_id, role_name, department, status, created_by, created_time) VALUES
(1, 'admin', '$2a$10$WfDftC39ADmgF0RHxFlOdu9/vsXXHKTygPfpIFb0nU/UeB8B2OWu6', '管理员', 1, '系统管理员', '技术部', 1, 'system', NOW()),
(2, 'fawuzhuguan', '$2a$10$WfDftC39ADmgF0RHxFlOdu9/vsXXHKTygPfpIFb0nU/UeB8B2OWu6', '法务主管', 2, '法务主管', '法务部', 1, 'system', NOW()),
(3, 'fawuzhuanyuan', '$2a$10$WfDftC39ADmgF0RHxFlOdu9/vsXXHKTygPfpIFb0nU/UeB8B2OWu6', '法务专员', 3, '法务专员', '法务部', 1, 'system', NOW()),
(4, 'yewurenyuan', '$2a$10$WfDftC39ADmgF0RHxFlOdu9/vsXXHKTygPfpIFb0nU/UeB8B2OWu6', '业务人员', 4, '业务人员', '业务部', 1, 'system', NOW()),
(5, 'waibukehu', '$2a$10$WfDftC39ADmgF0RHxFlOdu9/vsXXHKTygPfpIFb0nU/UeB8B2OWu6', '外部客户', 5, '外部用户', '外部', 1, 'system', NOW());
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, icon, permission, sort_order, status, created_by, created_time) VALUES
(1, 0, '工作台', 'MENU', '/', 'views/Dashboard.vue', 'HomeFilled', 'dashboard:view', 1, 1, 'system', NOW()),
(2, 0, '合同管理', 'CATALOG', '/contract', '', 'Document', '', 2, 1, 'system', NOW()),
(3, 2, '合同上传', 'MENU', '/contract/upload', 'views/contract/Upload.vue', 'Upload', 'contract:upload', 1, 1, 'system', NOW()),
(4, 2, '合同列表', 'MENU', '/contract/list', 'views/contract/List.vue', 'Document', 'contract:list', 2, 1, 'system', NOW()),
(13, 2, '合同归档', 'MENU', '/contract/archive', 'views/contract/Archive.vue', 'FolderOpened', 'contract:archive', 3, 1, 'system', NOW()),
(14, 0, '流程管理', 'CATALOG', '/process', '', 'Setting', '', 3, 1, 'system', NOW()),
(15, 14, '流程配置', 'MENU', '/process/config', 'views/process/Config.vue', 'Setting', 'process:config', 1, 1, 'system', NOW()),
(16, 14, '任务分配', 'MENU', '/process/assign', 'views/process/Assign.vue', 'UserFilled', 'process:assign', 2, 1, 'system', NOW()),
(17, 14, '流程跟踪', 'MENU', '/process/track', 'views/process/Track.vue', 'Time', 'process:track', 3, 1, 'system', NOW()),
(5, 0, '模板管理', 'CATALOG', '/template', '', 'FolderOpened', '', 4, 1, 'system', NOW()),
(18, 5, '模板列表', 'MENU', '/template', 'views/template/List.vue', 'FolderOpened', 'template:list', 1, 1, 'system', NOW()),
(19, 5, '条款库', 'MENU', '/template/clause', 'views/template/Clause.vue', 'Document', 'template:clause', 2, 1, 'system', NOW()),
(6, 0, '规则配置', 'CATALOG', '/rules', '', 'Setting', '', 5, 1, 'system', NOW()),
(20, 6, '规则列表', 'MENU', '/rules', 'views/rules/List.vue', 'Setting', 'rules:list', 1, 1, 'system', NOW()),
(21, 6, '风险等级', 'MENU', '/rules/risk-level', 'views/rules/RiskLevel.vue', 'Warning', 'rules:risk', 2, 1, 'system', NOW()),
(7, 0, '统计报表', 'MENU', '/statistics', 'views/statistics/Index.vue', 'Histogram', 'statistics:view', 5, 1, 'system', NOW()),
(8, 0, '系统管理', 'CATALOG', '/system', '', 'User', '', 6, 1, 'system', NOW()),
(9, 8, '用户管理', 'MENU', '/system/user', 'views/system/User.vue', 'User', 'system:user:list', 1, 1, 'system', NOW()),
(10, 8, '角色权限', 'MENU', '/system/role', 'views/system/Role.vue', 'Key', 'system:role:list', 2, 1, 'system', NOW()),
(11, 8, '菜单管理', 'MENU', '/system/menu', 'views/system/Menu.vue', 'Menu', 'system:menu:list', 3, 1, 'system', NOW()),
(12, 8, '操作日志', 'MENU', '/system/log', 'views/system/Log.vue', 'Monitor', 'system:log:list', 4, 1, 'system', NOW()),
(22, 8, '系统配置', 'MENU', '/system/config', 'views/system/Config.vue', 'Setting', 'system:config', 5, 1, 'system', NOW()),
(23, 8, '数据备份', 'MENU', '/system/backup', 'views/system/Backup.vue', 'Download', 'system:backup', 6, 1, 'system', NOW()),
(24, 0, '外部对接', 'CATALOG', '/integration', '', 'Link', '', 7, 1, 'system', NOW()),
(25, 24, 'OA对接', 'MENU', '/integration/oa', 'views/integration/OA.vue', 'Link', 'integration:oa', 1, 1, 'system', NOW()),
(26, 24, '电子签章', 'MENU', '/integration/sign', 'views/integration/Sign.vue', 'Stamp', 'integration:sign', 2, 1, 'system', NOW()),
(27, 24, 'API开放', 'MENU', '/integration/api', 'views/integration/API.vue', 'Code', 'integration:api', 3, 1, 'system', NOW());
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17), (1, 5), (1, 18), (1, 19),
(1, 6), (1, 20), (1, 21), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 22), (1, 23),
(1, 24), (1, 25), (1, 26), (1, 27),
(2, 1), (2, 2), (2, 3), (2, 4), (2, 13), (2, 14), (2, 15), (2, 16), (2, 17), (2, 5), (2, 18), (2, 19),
(2, 6), (2, 20), (2, 21), (2, 7), (2, 8), (2, 12), (2, 24), (2, 25), (2, 26), (2, 27),
(3, 1), (3, 2), (3, 4), (3, 13), (3, 14), (3, 16), (3, 17), (3, 5), (3, 18), (3, 6), (3, 20), (3, 7),
(4, 1), (4, 2), (4, 3), (4, 4),
(5, 1), (5, 2), (5, 3), (5, 4);
INSERT INTO sys_config (id, config_key, config_value, config_name, config_type, description, status, created_by, created_time) VALUES
(1, 'system.name', '合同智能审批系统', '系统名称', 'SYSTEM', '系统显示名称', 1, 'system', NOW()),
(2, 'system.version', '1.0.0', '系统版本', 'SYSTEM', '系统当前版本号', 1, 'system', NOW()),
(3, 'contract.max_size', '52428800', '合同最大上传大小', 'BUSINESS', '合同文件最大上传大小（字节），默认50MB', 1, 'system', NOW()),
(4, 'contract.support_types', 'pdf,doc,docx', '支持的文件类型', 'BUSINESS', '支持上传的合同文件类型', 1, 'system', NOW()),
(5, 'ai.review.enabled', 'true', 'AI审核启用', 'BUSINESS', '是否启用AI自动审核功能', 1, 'system', NOW()),
(6, 'ai.review.timeout', '300', 'AI审核超时时间', 'BUSINESS', 'AI审核超时时间（秒）', 1, 'system', NOW()),
(7, 'process.auto_start', 'true', '流程自动启动', 'BUSINESS', '合同上传后是否自动启动审核流程', 1, 'system', NOW()),
(8, 'archive.retention_period', '36', '归档保留期限', 'BUSINESS', '合同归档默认保留期限（月）', 1, 'system', NOW());
INSERT INTO contract_risk_level (id, risk_code, risk_name, risk_color, score_min, score_max, description, handling_suggestion, status, sort_order, created_by, created_time) VALUES
(1, 'NONE', '无风险', '#67C23A', 0, 10, '合同条款无明显风险，符合法律法规要求', '无需修改，可直接通过审核', 1, 1, 'system', NOW()),
(2, 'LOW', '低风险', '#E6A23C', 11, 30, '合同条款存在轻微风险，影响较小', '建议关注，可根据实际情况决定是否修改', 1, 2, 'system', NOW()),
(3, 'MEDIUM', '中风险', '#F56C6C', 31, 60, '合同条款存在一定风险，可能造成损失', '建议修改后重新提交审核', 1, 3, 'system', NOW()),
(4, 'HIGH', '高风险', '#E74C3C', 61, 85, '合同条款存在较高风险，可能造成较大损失', '必须修改后重新提交审核', 1, 4, 'system', NOW()),
(5, 'CRITICAL', '严重风险', '#9B59B6', 86, 100, '合同条款存在严重风险，可能造成重大损失或违法', '禁止通过，必须修改后重新提交', 1, 5, 'system', NOW());
INSERT INTO contract_template (id, template_no, template_name, contract_type, template_version, description, status, created_by, created_time) VALUES
(1, 'TPL-PURCHASE-001', '标准采购合同模板', '采购合同', 'v2.3', '适用于一般采购业务的标准合同模板', 'ACTIVE', '张三', NOW()),
(2, 'TPL-SERVICE-001', '标准服务合同模板', '服务合同', 'v1.2', '适用于服务外包业务的标准合同模板', 'ACTIVE', '李四', NOW()),
(3, 'TPL-LABOR-001', '标准劳动合同模板', '劳动合同', 'v3.0', '适用于企业员工劳动合同', 'ACTIVE', '王五', NOW()),
(4, 'TPL-LEASE-001', '标准租赁合同模板', '租赁合同', 'v1.1', '适用于设备租赁业务的标准合同模板', 'ACTIVE', '张三', NOW()),
(5, 'TPL-SALES-001', '标准销售合同模板', '销售合同', 'v2.0', '适用于产品销售业务的标准合同模板', 'ACTIVE', '李四', NOW());
INSERT INTO contract_clause (id, clause_code, clause_name, clause_category, contract_type, content, standard_content, risk_level, sort_order, status, description, legal_basis, created_by, created_time) VALUES
(1, 'CLAUSE-001', '合同双方当事人', 'COMMON', '', '甲方：[甲方名称]\\n乙方：[乙方名称]', '甲方：[甲方名称]\\n乙方：[乙方名称]\\n双方应提供真实有效的营业执照或身份证明文件', 'LOW', 1, 1, '合同基本信息条款', '《民法典》第464条', 'system', NOW()),
(2, 'CLAUSE-002', '合同标的', 'COMMON', '', '本合同项下标的为：[标的描述]', '本合同项下标的为：[标的描述]\\n标的应符合国家相关标准和双方约定的技术要求', 'LOW', 2, 1, '合同标的条款', '《民法典》第509条', 'system', NOW()),
(3, 'CLAUSE-003', '合同价款及支付方式', 'COMMON', '', '合同总价款为：[金额]元\\n支付方式：[支付方式]', '合同总价款为：[金额]元\\n支付方式：[支付方式]\\n付款条件应明确约定，预付款比例不得超过50%', 'MEDIUM', 3, 1, '合同价款条款', '《民法典》第509条', 'system', NOW()),
(4, 'CLAUSE-004', '违约责任', 'RISK', '', '违约方应向守约方支付违约金：[金额]', '违约方应向守约方支付合同总额30%以内的违约金\\n约定的违约金过分高于造成的损失的，当事人可以请求人民法院或者仲裁机构予以适当减少', 'HIGH', 4, 1, '违约责任条款', '《民法典》第585条', 'system', NOW()),
(5, 'CLAUSE-005', '争议解决', 'RISK', '', '如发生争议，由[管辖地]法院管辖', '如发生争议，双方应协商解决；协商不成的，由甲方所在地有管辖权的人民法院管辖', 'MEDIUM', 5, 1, '争议解决条款', '《民事诉讼法》第35条', 'system', NOW()),
(6, 'CLAUSE-006', '知识产权归属', 'SPECIAL', '', '合同履行过程中产生的知识产权归[归属方]所有', '合同履行过程中产生的知识产权应明确约定归属\\n甲方委托乙方完成的工作成果，知识产权归甲方所有，乙方享有使用权', 'HIGH', 6, 1, '知识产权条款', '《民法典》第464条', 'system', NOW()),
(7, 'CLAUSE-007', '保密条款', 'COMMON', '', '双方应对合同内容保密', '双方应对合同内容及履行过程中知悉的商业秘密保密\\n保密期限应至少为合同终止后3年', 'MEDIUM', 7, 1, '保密条款', '《反不正当竞争法》第9条', 'system', NOW()),
(8, 'CLAUSE-008', '不可抗力', 'COMMON', '', '因不可抗力导致合同无法履行的，双方互不承担责任', '因不可抗力导致合同无法履行的，受影响方应及时通知对方并提供证明\\n不可抗力消除后，双方应继续履行合同', 'LOW', 8, 1, '不可抗力条款', '《民法典》第590条', 'system', NOW());
INSERT INTO contract_review_rule (id, rule_no, rule_name, rule_type, contract_type, clause_keyword, rule_config, risk_level, priority, status, description, created_by, created_time) VALUES
(1, 'RULE-001', '违约金比例范围', 'VALUE_RANGE', '', '违约金', '{"min": 5, "max": 30, "unit": "%"}', 'HIGH', 1, 1, '违约金比例不应超过合同金额的30%', 'system', NOW()),
(2, 'RULE-002', '预付款比例限制', 'VALUE_RANGE', '采购合同', '预付款', '{"min": 0, "max": 50, "unit": "%"}', 'MEDIUM', 2, 1, '预付款比例不应超过合同金额的50%', 'system', NOW()),
(3, 'RULE-003', '争议解决条款必填', 'REQUIRED', '', '争议解决', '{}', 'HIGH', 1, 1, '合同必须包含争议解决条款', 'system', NOW()),
(4, 'RULE-004', '知识产权归属检查', 'REQUIRED', '', '知识产权', '{}', 'HIGH', 1, 1, '合同必须明确知识产权归属', 'system', NOW()),
(5, 'RULE-005', '保密期限检查', 'VALUE_RANGE', '', '保密期限', '{"min": 36, "unit": "month"}', 'MEDIUM', 2, 1, '保密期限不应少于3年', 'system', NOW()),
(6, 'RULE-006', '合同期限检查', 'VALUE_RANGE', '', '合同期限', '{"max": 360, "unit": "day"}', 'LOW', 3, 1, '合同期限一般不应超过1年', 'system', NOW()),
(7, 'RULE-007', '税率合规检查', 'REGEX', '', '税率', '{"pattern": "^(0\\\\.|3\\\\.|5\\\\.|6\\\\.|9\\\\.|13\\\\.)\\\\d*%?$"}', 'MEDIUM', 2, 1, '税率应符合国家税收政策', 'system', NOW()),
(8, 'RULE-008', '竞业限制条款检查', 'REQUIRED', '劳动合同', '竞业限制', '{}', 'MEDIUM', 2, 1, '劳动合同应包含竞业限制条款', 'system', NOW());
INSERT INTO contract_contract (id, contract_no, contract_name, contract_type, party_a, party_b, amount, sign_date, start_date, end_date, file_type, status, risk_level, risk_score, upload_user_id, upload_user_name, upload_time) VALUES
(1, 'CT20260706001', 'XX科技有限公司采购合同', '采购合同', 'XX科技有限公司', 'XX供应商', 500000.00, '2026-07-01', '2026-07-01', '2027-06-30', 'pdf', 'REVIEWING', 'HIGH', 75, 2, '法务主管', '2026-07-06 09:30:00'),
(2, 'CT20260706002', 'XX服务合同', '服务合同', 'XX科技有限公司', 'XX服务公司', 80000.00, '2026-07-02', '2026-07-02', '2026-12-31', 'pdf', 'REVIEWING', 'MEDIUM', 45, 3, '法务专员', '2026-07-06 10:15:00'),
(3, 'CT20260705003', 'XX租赁合同', '租赁合同', 'XX科技有限公司', 'XX租赁公司', 120000.00, '2026-07-01', '2026-07-01', '2027-06-30', 'pdf', 'APPROVED', 'LOW', 25, 4, '业务人员', '2026-07-05 14:20:00'),
(4, 'CT20260705004', 'XX劳动合同', '劳动合同', 'XX科技有限公司', '孙七', 360000.00, '2026-07-01', '2026-07-01', '2029-06-30', 'pdf', 'ARCHIVED', 'NONE', 5, 1, '管理员', '2026-07-05 16:45:00'),
(5, 'CT20260704005', 'XX销售合同', '销售合同', 'XX科技有限公司', 'XX客户公司', 800000.00, '2026-07-01', '2026-07-01', '2026-12-31', 'pdf', 'AI_REVIEWING', NULL, NULL, 2, '法务主管', '2026-07-04 11:00:00');
INSERT INTO contract_review_result (id, contract_id, risk_level, risk_score, total_risks, high_risks, medium_risks, low_risks, conclusion, matching_count, modified_count, missing_count, added_count) VALUES
(1, 1, 'HIGH', 75, 5, 2, 2, 1, '建议修改高风险条款后重新提交', 10, 4, 2, 1),
(2, 3, 'LOW', 25, 2, 0, 1, 1, '风险较低，可正常通过', 15, 2, 0, 0),
(3, 4, 'NONE', 5, 0, 0, 0, 0, '无风险，已归档', 20, 0, 0, 0);
INSERT INTO contract_risk_clause (id, contract_id, review_result_id, clause_no, clause_title, clause_content, page, risk_level, risk_type, risk_description, suggestion, legal_basis, status) VALUES
(1, 1, 1, '第四条', '违约责任', '违约方应向守约方支付合同总额200%的违约金，无论违约情节轻重。', 3, 'HIGH', '违约金比例过高', '违约金比例200%远超合理范围，存在显失公平风险，可能被法院调低。', '建议调整为合同总额的30%，或参照《民法典》第585条合理约定。', '《民法典》第585条', 'PENDING'),
(2, 1, 1, '第八条', '知识产权归属', '合同履行过程中产生的知识产权归乙方所有。', 5, 'HIGH', '知识产权归属不明', '未明确约定甲方在合同履行过程中的知识产权归属，可能导致甲方无法使用相关成果。', '补充约定：甲方在合同履行过程中产生的知识产权归甲方所有，乙方享有使用权。', '《民法典》第464条', 'PENDING'),
(3, 1, 1, '第二条', '付款方式', '合同签订后甲方需预付80%货款。', 2, 'MEDIUM', '预付款比例过高', '预付比例80%过高，甲方资金风险较大，建议降低预付比例。', '建议调整预付比例为30%，剩余款项在验收合格后支付。', '《民法典》第509条', 'PENDING'),
(4, 1, 1, '第六条', '保密条款', '双方应对合同内容保密，保密期限为合同期限。', 4, 'MEDIUM', '保密期限过短', '保密期限仅为合同期限，合同终止后保密义务即失效，可能导致商业秘密泄露。', '建议将保密期限延长至合同终止后3年。', '《反不正当竞争法》第9条', 'PENDING'),
(5, 1, 1, '第十条', '争议解决', '如发生争议，由乙方所在地法院管辖。', 6, 'LOW', '管辖地不利', '管辖法院约定在乙方所在地，甲方诉讼成本较高。', '建议约定由甲方所在地法院管辖或双方协商解决。', '《民事诉讼法》第35条', 'PENDING');
INSERT INTO contract_comment (id, contract_id, user_id, user_name, user_role, comment_type, content, page, status, created_time) VALUES
(1, 1, 2, '法务主管', 'LEGAL_MANAGER', 'REVIEW', '建议修改违约责任条款，违约金比例过高。', 3, 'ACTIVE', NOW()),
(2, 1, 3, '法务专员', 'LEGAL_OFFICER', 'SUGGESTION', '预付款比例建议降低至30%以下。', 2, 'ACTIVE', NOW()),
(3, 1, 3, '法务专员', 'LEGAL_OFFICER', 'TEXT', '此条款需要进一步审查。', 5, 'RESOLVED', NOW());
INSERT INTO contract_archive (id, contract_id, contract_no, contract_name, contract_type, party_a, party_b, amount, archive_code, archive_time, archive_user_id, archive_user_name, storage_type, retention_period, expire_date) VALUES
(1, 4, 'CT20260705004', 'XX劳动合同', '劳动合同', 'XX科技有限公司', '孙七', 360000.00, 'ARC20260705001', '2026-07-05 17:00:00', 1, '管理员', 'ELECTRONIC', 36, '2029-07-05');
INSERT INTO process_config (id, process_code, process_name, contract_type, description, status, sort_order, created_by, created_time) VALUES
(1, 'PROCESS-DEFAULT', '默认审核流程', '', '适用于所有合同类型的默认审核流程', 'ACTIVE', 1, 'system', NOW()),
(2, 'PROCESS-PURCHASE', '采购合同审核流程', '采购合同', '适用于采购合同的专用审核流程', 'ACTIVE', 2, 'system', NOW()),
(3, 'PROCESS-LABOR', '劳动合同审核流程', '劳动合同', '适用于劳动合同的专用审核流程', 'ACTIVE', 3, 'system', NOW());
INSERT INTO process_node (id, process_id, node_name, node_type, assignee_type, assignee_id, assignee_name, sort_order, timeout_days, is_required, description) VALUES
(1, 1, '开始', 'START', 'ROLE', NULL, '', 1, 0, 1, '流程开始节点'),
(2, 1, 'AI自动审核', 'REVIEW', 'SYSTEM', NULL, '系统', 2, 1, 1, 'AI自动审核节点'),
(3, 1, '法务专员审核', 'REVIEW', 'ROLE', 3, '法务专员', 3, 3, 1, '法务专员人工审核'),
(4, 1, '法务主管审批', 'APPROVE', 'ROLE', 2, '法务主管', 4, 5, 1, '法务主管审批'),
(5, 1, '结束', 'END', 'ROLE', NULL, '', 5, 0, 1, '流程结束节点');
INSERT INTO process_instance (id, instance_code, process_id, process_name, contract_id, contract_no, status, current_node_id, current_node_name, start_time, creator_id, creator_name) VALUES
(1, 'INSTANCE-20260706001', 1, '默认审核流程', 1, 'CT20260706001', 'RUNNING', 3, '法务专员审核', '2026-07-06 09:30:00', 2, '法务主管'),
(2, 'INSTANCE-20260706002', 1, '默认审核流程', 2, 'CT20260706002', 'RUNNING', 3, '法务专员审核', '2026-07-06 10:15:00', 3, '法务专员'),
(3, 'INSTANCE-20260705003', 1, '默认审核流程', 3, 'CT20260705003', 'COMPLETED', 5, '结束', '2026-07-05 14:20:00', 4, '业务人员');
INSERT INTO process_task (id, task_code, instance_id, process_id, node_id, node_name, node_type, contract_id, contract_no, assignee_id, assignee_name, status, priority, title, start_time, creator_id, creator_name) VALUES
(1, 'TASK-20260706001-001', 1, 1, 2, 'AI自动审核', 'REVIEW', 1, 'CT20260706001', 0, '系统', 'COMPLETED', 'NORMAL', 'AI自动审核任务', '2026-07-06 09:30:00', 2, '法务主管'),
(2, 'TASK-20260706001-002', 1, 1, 3, '法务专员审核', 'REVIEW', 1, 'CT20260706001', 3, '法务专员', 'PENDING', 'NORMAL', '法务专员审核任务', '2026-07-06 09:35:00', 2, '法务主管'),
(3, 'TASK-20260706002-001', 2, 1, 2, 'AI自动审核', 'REVIEW', 2, 'CT20260706002', 0, '系统', 'COMPLETED', 'NORMAL', 'AI自动审核任务', '2026-07-06 10:15:00', 3, '法务专员'),
(4, 'TASK-20260706002-002', 2, 1, 3, '法务专员审核', 'REVIEW', 2, 'CT20260706002', 3, '法务专员', 'PENDING', 'NORMAL', '法务专员审核任务', '2026-07-06 10:20:00', 3, '法务专员');
INSERT INTO integration_oa (id, oa_type, oa_name, status, app_id, app_secret, sync_status, description, created_by, created_time) VALUES
(1, 'WECOM', '企业微信对接', 'CONNECTED', 'wwxxxxxxxxxxxx', 'xxxxxxxxxxxxxxxx', 'ENABLED', '企业微信消息推送和审批同步', 'system', NOW());
INSERT INTO integration_sign (id, sign_type, sign_name, status, app_id, app_key, description, created_by, created_time) VALUES
(1, 'CA', 'CA证书电子签章', 'ACTIVE', 'CA-001', 'xxxxxxxx', '基于CA证书的电子签章服务', 'system', NOW());
INSERT INTO integration_api (id, api_name, api_path, http_method, api_version, status, auth_type, description, created_by, created_time) VALUES
(1, '合同查询API', '/api/v1/contracts', 'GET', '1.0', 'ACTIVE', 'API_KEY', '查询合同列表', 'system', NOW()),
(2, '合同详情API', '/api/v1/contracts/{id}', 'GET', '1.0', 'ACTIVE', 'API_KEY', '查询合同详情', 'system', NOW()),
(3, '合同上传API', '/api/v1/contracts/upload', 'POST', '1.0', 'ACTIVE', 'API_KEY', '上传合同文件', 'system', NOW());

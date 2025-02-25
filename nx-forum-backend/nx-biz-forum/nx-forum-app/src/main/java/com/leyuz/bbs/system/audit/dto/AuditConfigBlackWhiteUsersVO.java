package com.leyuz.bbs.system.audit.dto;

import com.leyuz.uc.user.dto.UserVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AuditConfigBlackWhiteUsersVO {
    // 自动通过白名单用户（用户ID集合）
    private List<UserVO> whiteListUsers = new ArrayList<>();
    // 黑名单用户（用户ID集合）
    private List<UserVO> blackListUsers = new ArrayList<>();
} 
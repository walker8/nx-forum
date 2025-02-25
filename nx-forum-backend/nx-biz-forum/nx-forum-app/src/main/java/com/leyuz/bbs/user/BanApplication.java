package com.leyuz.bbs.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.forum.ForumApplication;
import com.leyuz.bbs.forum.ForumPO;
import com.leyuz.bbs.forum.dto.ForumUserRoleCmd;
import com.leyuz.bbs.user.ban.BanMapper;
import com.leyuz.bbs.user.ban.BanPO;
import com.leyuz.bbs.user.dto.BanCreateCmd;
import com.leyuz.bbs.user.dto.BanPageQuery;
import com.leyuz.bbs.user.dto.BanVO;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.utils.BaseEntityUtils;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.UserE;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BanApplication {

    private final BanMapper banMapper;
    private final UserApplication userApplication;
    private final ForumApplication forumApplication;

    @Transactional
    public Boolean createBan(BanCreateCmd cmd) {
        BanPO banDO = new BanPO();
        banDO.setUserId(cmd.getUserId());
        banDO.setForumId(cmd.getForumId());
        banDO.setReason(cmd.getReason());
        banDO.setExpireTime(cmd.getExpireTime());
        banDO.setOperationType(0); // 0表示禁言操作
        BaseEntityUtils.setCreateBaseEntity(banDO);

        ForumUserRoleCmd forumUserRoleCmd = new ForumUserRoleCmd();
        forumUserRoleCmd.setUserId(cmd.getUserId());
        forumUserRoleCmd.setForumId(cmd.getForumId());
        forumUserRoleCmd.setRoleKey("MUTED_USER");
        forumUserRoleCmd.setExpireTime(cmd.getExpireTime());
        forumApplication.assignAuthorization(forumUserRoleCmd);

        return banMapper.insert(banDO) > 0;
    }

    @Transactional
    public Boolean unbanUser(BanCreateCmd cmd) {
        BanPO banDO = new BanPO();
        banDO.setUserId(cmd.getUserId());
        banDO.setForumId(cmd.getForumId());
        banDO.setReason(cmd.getReason());
        banDO.setOperationType(1); // 1表示解禁操作
        BaseEntityUtils.setCreateBaseEntity(banDO);

        forumApplication.cancelAuthorization(cmd.getUserId(), cmd.getForumId(), "MUTED_USER");

        return banMapper.insert(banDO) > 0;
    }

    public CustomPage<BanVO> queryBans(BanPageQuery query) {
        Page<BanPO> banPOPage = banMapper.queryBans(query.getForumId(), query.toPageQuery());
        return DataBaseUtils.createCustomPage(banPOPage, this::convertToVO);
    }

    private BanVO convertToVO(BanPO banPO) {
        BanVO vo = new BanVO();
        vo.setId(banPO.getId());
        UserE userE = userApplication.getByIdFromCache(banPO.getUserId());
        vo.setUserName(userE.getUserName());
        vo.setUserId(banPO.getUserId());
        Integer forumId = banPO.getForumId();
        if (forumId != null && forumId > 0) {
            ForumPO forumPO = forumApplication.getForumById(forumId);
            if (forumPO != null) {
                vo.setForumName(forumPO.getNickName());
            }
        } else {
            vo.setForumName("全局");
        }
        vo.setForumId(forumId);
        vo.setReason(banPO.getReason());
        vo.setExpireTime(banPO.getExpireTime());
        vo.setOperationType(banPO.getOperationType());
        UserE operationUserE = userApplication.getByIdFromCache(banPO.getCreateBy());
        vo.setOperationUserName(operationUserE.getUserName());
        vo.setCreateTime(banPO.getCreateTime());
        return vo;
    }
} 

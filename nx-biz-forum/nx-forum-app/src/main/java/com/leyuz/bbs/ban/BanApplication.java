package com.leyuz.bbs.ban;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.ban.dto.BanCreateCmd;
import com.leyuz.bbs.ban.dto.BanPageQuery;
import com.leyuz.bbs.ban.dto.BanVO;
import com.leyuz.bbs.ban.mybatis.IBanService;
import com.leyuz.bbs.forum.ForumApplication;
import com.leyuz.bbs.forum.ForumPO;
import com.leyuz.bbs.forum.dto.ForumUserRoleCmd;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.utils.BaseEntityUtils;
import com.leyuz.uc.domain.user.UserE;
import com.leyuz.uc.user.UserApplication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BanApplication {

    private final IBanService banService;
    private final UserApplication userApplication;
    private final ForumApplication forumApplication;

    @Transactional
    public Boolean createBan(BanCreateCmd cmd) {
        BanDO banDO = new BanDO();
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

        return banService.save(banDO);
    }

    @Transactional
    public Boolean unbanUser(BanCreateCmd cmd) {
        BanDO banDO = new BanDO();
        banDO.setUserId(cmd.getUserId());
        banDO.setForumId(cmd.getForumId());
        banDO.setReason(cmd.getReason());
        banDO.setOperationType(1); // 1表示解禁操作
        BaseEntityUtils.setCreateBaseEntity(banDO);

        forumApplication.cancelAuthorization(cmd.getUserId(), cmd.getForumId(), "MUTED_USER");

        return banService.save(banDO);
    }

    public CustomPage<BanVO> queryBans(BanPageQuery query) {
        Page<BanDO> banDOPage = banService.queryBans(query.getForumId(), query.toPageQuery());
        return DataBaseUtils.createCustomPage(banDOPage, this::convertToVO);
    }

    private BanVO convertToVO(BanDO banDO) {
        BanVO vo = new BanVO();
        vo.setId(banDO.getId());
        UserE userE = userApplication.getByIdFromCache(banDO.getUserId());
        vo.setUserName(userE.getUserName());
        vo.setUserId(banDO.getUserId());
        Integer forumId = banDO.getForumId();
        if (forumId != null && forumId > 0) {
            ForumPO forumPO = forumApplication.getForumById(forumId);
            if (forumPO != null) {
                vo.setForumName(forumPO.getNickName());
            }
        } else {
            vo.setForumName("全局");
        }
        vo.setForumId(forumId);
        vo.setReason(banDO.getReason());
        vo.setExpireTime(banDO.getExpireTime());
        vo.setOperationType(banDO.getOperationType());
        UserE operationUserE = userApplication.getByIdFromCache(banDO.getCreateBy());
        vo.setOperationUserName(operationUserE.getUserName());
        vo.setCreateTime(banDO.getCreateTime());
        return vo;
    }
} 
package com.leyuz.bbs.forum;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.TypeReference;
import com.leyuz.bbs.auth.ForumPermissionResolver;
import com.leyuz.bbs.config.ForumConfigApplication;
import com.leyuz.bbs.forum.access.ForumAccessPO;
import com.leyuz.bbs.forum.access.ForumAccessService;
import com.leyuz.bbs.forum.dto.*;
import com.leyuz.bbs.forum.mybatis.IForumService;
import com.leyuz.bbs.thread.mybatis.IThreadService;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.utils.BaseEntityUtils;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.module.cache.GenericCache;
import com.leyuz.uc.auth.AuthApplication;
import com.leyuz.uc.auth.dto.UserRoleCreateCmd;
import com.leyuz.uc.auth.dto.UserRolePageQuery;
import com.leyuz.uc.auth.dto.UserRoleVO;
import com.leyuz.uc.auth.role.dto.RoleDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumApplication {
    private final IForumService forumService;
    private final ForumAccessService forumAccessService;
    private final ForumConfigApplication forumConfigApplication;
    private final AuthApplication authApplication;
    private final IThreadService threadService;

    private static final Pattern PATTERN_NAME = Pattern.compile("^[a-zA-Z0-9]{2,20}$");
    private static final Pattern PATTERN_NICK_NAME = Pattern.compile("^\\S{2,20}$");

    private final GenericCache<Integer, ForumPO> forumIdCache;
    private final GenericCache<String, ForumPO> forumNameCache;
    private final GenericCache<String, List<ForumMenuItemVO>> forumMenuItemCache;
    private final GenericCache<String, ForumMenuVO> forumMenuCache;
    private final GenericCache<String, String> commonCache;

    public ForumPO getForumById(Integer forumId) {
        return forumIdCache.computeIfAbsent(forumId, forumService::getByForumId);
    }

    public ForumVO getForumInfoByName(String forumName) {
        ForumPO forumPO = getForumByNameWithDefault(forumName);
        ForumVO forumVO = new ForumVO();
        BeanUtils.copyProperties(forumPO, forumVO);
        if (StringUtils.isBlank(forumVO.getSeoTitle())) {
            forumVO.setSeoTitle(forumPO.getNickName() + " - " + forumConfigApplication.getWebsiteBaseInfo().getWebsiteName());
        }
        return forumVO;
    }

    public ForumPO getForumByNameWithDefault(String forumName) {
        ForumPO forumPO;
        if (StringUtils.isNotBlank(forumName)) {
            forumPO = getByName(forumName);
        } else {
            forumPO = forumService.getByForumId(forumConfigApplication.getDefaultForumId());
        }
        if (forumPO == null) {
            throw new ValidationException("版块不存在");
        }
        return forumPO;
    }

    public ForumPO getByName(String forumName) {
        return forumNameCache.computeIfAbsent(forumName, forumService::getByName);
    }

    public Integer getForumIdByName(String forumName) {
        Integer forumId = 0;
        if (StringUtils.isNotBlank(forumName)) {
            ForumPO forumPO = getByName(forumName);
            if (forumPO == null) {
                throw new ValidationException("版块不存在");
            }
            return forumPO.getForumId();
        }
        return forumId;
    }

    public void createForum(ForumCmd forumCmd) {
        ForumPO forumPO = new ForumPO();
        checkName(forumCmd.getName(), 0);
        checkNickName(forumCmd.getNickName());
        forumCmd.setNickName(forumCmd.getNickName().trim());
        BeanUtils.copyProperties(forumCmd, forumPO);
        forumPO.setForumId(null);
        BaseEntityUtils.setCreateBaseEntity(forumPO);
        forumService.save(forumPO);
        forumMenuItemCache.remove("all");
    }

    public void updateForum(ForumCmd forumCmd) {
        ForumPO forumPO = new ForumPO();
        checkForumId(forumCmd.getForumId());
        checkName(forumCmd.getName(), forumCmd.getForumId());
        checkNickName(forumCmd.getNickName());
        forumCmd.setNickName(forumCmd.getNickName().trim());
        BeanUtils.copyProperties(forumCmd, forumPO);
        BaseEntityUtils.setUpdateBaseEntity(forumPO);
        forumService.updateById(forumPO);
        deleteForumCache(forumCmd.getForumId(), forumCmd.getName());
    }

    private void checkForumId(Integer forumId) {
        if (forumId == null || forumId <= 0) {
            throw new ValidationException("版块ID不能为空");
        }
    }

    private void checkNickName(String nickName) {
        if (StringUtils.isBlank(nickName)) {
            throw new ValidationException("版块名称不能为空");
        }
        Matcher matcher = PATTERN_NICK_NAME.matcher(nickName);
        boolean isValid = matcher.matches();
        if (!isValid) {
            throw new ValidationException("版块名称格式不正确");
        }
    }

    private void checkName(String name, Integer forumId) {
        if (StringUtils.isBlank(name)) {
            throw new ValidationException("版块号不能为空");
        }
        Matcher matcher = PATTERN_NAME.matcher(name);
        boolean isValid = matcher.matches();
        if (!isValid) {
            throw new ValidationException("版块号格式不正确");
        }
        ForumPO forumPO = getByName(name);
        if (forumPO != null && !Objects.equals(forumPO.getForumId(), forumId)) {
            throw new ValidationException("版块号已存在");
        }
    }

    public ForumMenuVO getForumShowMenu() {
        return forumMenuCache.computeIfAbsent("all", menu -> {
            ForumMenuVO forumMenuVO = new ForumMenuVO();
            List<ForumPO> forumPOList = forumService.getAllForumShowMenu();
            ForumPO forumPO = forumService.getByForumId(forumConfigApplication.getDefaultForumId());
            forumMenuVO.setDefaultForumName(forumPO.getName());
            forumMenuVO.setRecords(Optional.ofNullable(forumPOList).orElse(new ArrayList<>()).stream().map(this::convertToForumMenuItemVO).collect(Collectors.toList()));
            ForumMenuItemVO allForumMenuItemVO = new ForumMenuItemVO();
            allForumMenuItemVO.setForumId(0);
            allForumMenuItemVO.setName("all");
            allForumMenuItemVO.setNickName("全部版块");
            allForumMenuItemVO.setIconName("tabler:category");
            forumMenuVO.getRecords().add(allForumMenuItemVO);
            return forumMenuVO;
        });
    }

    private ForumMenuItemVO convertToForumMenuItemVO(ForumPO forumPO) {
        ForumMenuItemVO forumMenuItemVO = new ForumMenuItemVO();
        BeanUtils.copyProperties(forumPO, forumMenuItemVO);
        return forumMenuItemVO;
    }

    public List<ForumMenuItemVO> getUserForumMenu() {
        return forumMenuItemCache.computeIfAbsent("all", menu -> {
            List<ForumPO> forumPOList = forumService.getAllForumShowMenu();
            return Optional.ofNullable(forumPOList).orElse(new ArrayList<>())
                    .stream().filter(f -> !f.getIsSystem()).map(this::convertToForumMenuItemVO).collect(Collectors.toList());
        });
    }

    public List<ForumItemVO> getAllForumByAdmin() {
        List<ForumPO> forumPOList = forumService.getAllForum();
        return Optional.ofNullable(forumPOList).orElse(new ArrayList<>())
                .stream().map(this::convertToForumItemVO).collect(Collectors.toList());
    }

    private ForumItemVO convertToForumItemVO(ForumPO forumPO) {
        ForumItemVO forumItemVO = BeanUtil.toBean(forumPO, ForumItemVO.class);
        Integer defaultForumId = forumConfigApplication.getDefaultForumId();
        forumItemVO.setDefaultForum(Objects.equals(forumPO.getForumId(), defaultForumId));
        return forumItemVO;
    }

    public void updateDefaultForum(Integer forumId) {
        forumConfigApplication.updateDefaultForum(forumId);
    }

    public boolean updateBaseForum(ForumItemVO forumItemVO) {
        // 只允许更新用户创建的版块
        checkUserForumExist(forumItemVO.getForumId());
        // 禁止修改的内容
        forumItemVO.setIsSystem(null);
        forumItemVO.setCreateTime(null);
        ForumPO forumPO = BeanUtil.toBean(forumItemVO, ForumPO.class);
        BaseEntityUtils.setUpdateBaseEntity(forumPO);
        boolean updated = forumService.updateById(forumPO);
        deleteForumCache(forumItemVO.getForumId(), forumItemVO.getName());
        return updated;
    }

    public boolean deleteForumById(Integer forumId) {
        // 只允许删除用户创建的版块
        checkUserForumExist(forumId);
        Long threadCount = threadService.getThreadCount(forumId);
        if (threadCount > 0) {
            throw new ValidationException("版块下有帖子，请先删除帖子后再删除版块");
        }
        ForumPO forumPO = new ForumPO();
        forumPO.setForumId(forumId);
        forumPO.setIsDeleted(true);
        BaseEntityUtils.setUpdateBaseEntity(forumPO);
        deleteForumCache(forumId, forumPO.getName());
        return forumService.updateById(forumPO);
    }

    private void deleteForumCache(Integer forumId, String forumName) {
        forumIdCache.remove(forumId);
        forumNameCache.remove(forumName);
        forumMenuCache.remove("all");
        forumMenuItemCache.remove("all");
    }

    /**
     * 校验用户创建的版块是否存在
     *
     * @param forumId 版块id
     */
    private void checkUserForumExist(Integer forumId) {
        List<ForumItemVO> forumItemVOList = getAllForumByAdmin();
        List<Integer> forumIdList = forumItemVOList.stream().filter(f -> !f.getIsSystem()).map(ForumItemVO::getForumId).collect(Collectors.toList());
        // 只允许操作用户创建的版块
        if (!forumIdList.contains(forumId)) {
            throw new ValidationException("版块不存在！");
        }
    }

    /**
     * 获取版块权限配置
     */
    public List<ForumAccessDTO> getForumAccess(Integer forumId) {
        List<ForumAccessPO> accessList = forumAccessService.getByForumId(forumId);
        Map<String, ForumAccessPO> forumAccessMap = accessList.stream().collect(Collectors.toMap(ForumAccessPO::getRoleKey, Function.identity()));
        List<RoleDTO> roleDTOList = authApplication.listAllRoles();
        return roleDTOList.stream().map(roleDTO -> {
            ForumAccessDTO dto = new ForumAccessDTO();
            ForumAccessPO forumAccessPO = forumAccessMap.get(roleDTO.getRoleKey());
            if (forumAccessPO == null) {
                dto.setRoleKey(roleDTO.getRoleKey());
                dto.setRoleName(roleDTO.getRoleName());
            } else {
                dto.setId(forumAccessPO.getId());
                dto.setRoleKey(forumAccessPO.getRoleKey());
                dto.setRoleName(roleDTO.getRoleName());
                dto.setPerms(JSONArray.parseArray(forumAccessPO.getPerms(), String.class));
                dto.setRemark(forumAccessPO.getRemark());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 批量更新版块权限
     */
    @Transactional
    public void updateForumAccess(Integer forumId, List<ForumAccessDTO> accessList, boolean enableForumAccess) {
        ForumPO forumPO = getForumById(forumId);
        forumPO.setForumAccess(enableForumAccess);
        BaseEntityUtils.setUpdateBaseEntity(forumPO);
        forumService.updateById(forumPO);
        List<ForumAccessPO> updateForumAccessList = accessList.stream()
                .filter(dto -> dto.getId() != null)
                .map(dto -> {
                    ForumAccessPO po = new ForumAccessPO();
                    po.setId(dto.getId());
                    po.setForumId(forumId);
                    po.setRoleKey(dto.getRoleKey());
                    po.setPerms(dto.getPerms() == null ? "[]" : JSON.toJSONString(dto.getPerms()));
                    po.setRemark(dto.getRemark());
                    BaseEntityUtils.setUpdateBaseEntity(po);
                    return po;
                }).collect(Collectors.toList());
        forumAccessService.batchUpdateForumAccess(updateForumAccessList);
        List<ForumAccessPO> createForumAccessList = accessList.stream()
                .filter(dto -> dto.getId() == null)
                .map(dto -> {
                    ForumAccessPO po = new ForumAccessPO();
                    po.setForumId(forumId);
                    po.setRoleKey(dto.getRoleKey());
                    po.setPerms(dto.getPerms() == null ? "[]" : JSON.toJSONString(dto.getPerms()));
                    po.setRemark(dto.getRemark());
                    BaseEntityUtils.setCreateBaseEntity(po);
                    return po;
                }).collect(Collectors.toList());
        forumAccessService.batchCreateForumAccess(createForumAccessList);
        deleteForumCache(forumId, forumPO.getName());
    }

    /**
     * 删除版块权限
     */
    public boolean deleteForumAccess(Integer forumId) {
        return forumAccessService.deleteByForumId(forumId);
    }

    /**
     * 批量新增版块权限
     */
    public boolean createForumAccess(Integer forumId, List<ForumAccessDTO> accessList) {
        List<ForumAccessPO> poList = accessList.stream().map(dto -> {
            ForumAccessPO po = new ForumAccessPO();
            po.setForumId(forumId);
            po.setRoleKey(dto.getRoleKey());
            po.setPerms(String.join(",", dto.getPerms()));
            po.setRemark(dto.getRemark());
            BaseEntityUtils.setCreateBaseEntity(po);
            return po;
        }).collect(Collectors.toList());
        return forumAccessService.batchCreateForumAccess(poList);
    }

    public CustomPage<UserRoleVO> queryForumUserRoles(ForumUserRoleQuery query) {
        UserRolePageQuery pageQuery = new UserRolePageQuery();
        BeanUtils.copyProperties(query, pageQuery);
        pageQuery.setRoleScope(ForumPermissionResolver.getForumRoleScope(query.getForumId()));
        return authApplication.queryUserRoles(pageQuery);
    }

    public boolean assignAuthorization(ForumUserRoleCmd cmd) {
        String roleScope = ForumPermissionResolver.getForumRoleScope(cmd.getForumId());
        if (StringUtils.isNotEmpty(roleScope)) {
            // 校验用户在当前版块下是否已有其他用户组
            ForumUserRoleQuery query = new ForumUserRoleQuery();
            query.setUserId(cmd.getUserId());
            query.setForumId(cmd.getForumId());
            CustomPage<UserRoleVO> roleVOCustomPage = queryForumUserRoles(query);
            if (roleVOCustomPage.getTotal() > 0) {
                throw new ValidationException("用户在当前版块下已有其他用户组");
            }
        }

        UserRoleCreateCmd userRoleCreateCmd = new UserRoleCreateCmd();
        BeanUtils.copyProperties(cmd, userRoleCreateCmd);
        userRoleCreateCmd.setRoleScope(roleScope);
        return authApplication.assignAuthorization(userRoleCreateCmd);
    }

    public boolean cancelAuthorization(Integer forumId, String roleKey) {
        return cancelAuthorization(HeaderUtils.getUserId(), forumId, roleKey);
    }

    public boolean cancelAuthorization(Long userId, Integer forumId, String roleKey) {
        return authApplication.cancelAuthorization(userId, ForumPermissionResolver.getForumRoleScope(forumId), roleKey);
    }

    public List<UserForumVO> getUserForumList() {
        List<ForumPO> userForums = forumService.getAllForum().stream()
                .filter(forumPO -> !forumPO.getIsSystem())
                .sorted(Comparator.comparing(ForumPO::getMenuOrder))
                .toList();
        Long userId = HeaderUtils.getUserId();
        Map<String, List<Long>> moderatorMap = getModeratorIdsByRoleScope();
        return userForums.stream().map(forumPO -> {
            UserForumVO userForumVO = new UserForumVO();
            BeanUtils.copyProperties(forumPO, userForumVO);
            String roleScope = ForumPermissionResolver.getForumRoleScope(forumPO.getForumId());
            if (userId > 0 && moderatorMap.getOrDefault(roleScope, Collections.emptyList()).contains(userId)) {
                userForumVO.setIsAdmin(true);
            }
            return userForumVO;
        }).collect(Collectors.toList());
    }

    /**
     * 获取所有版主 ID，并按 roleScope 分类
     *
     * @return 按 roleScope 分类的版主 ID 映射
     */
    private Map<String, List<Long>> getModeratorIdsByRoleScope() {
        String cacheKey = "FORUM_MODERATOR_IDS_BY_ROLE_SCOPE";
        String result = commonCache.computeIfAbsent(cacheKey, key -> {
            UserRolePageQuery query = new UserRolePageQuery();
            query.setPageSize(100);
            query.setRoleKey("MODERATOR");
            CustomPage<UserRoleVO> customPage = authApplication.queryUserRoles(query);

            // 按 roleScope 分组
            Map<String, List<Long>> moderatorIdsByRoleScope = customPage.getRecords().stream()
                    .filter(userRoleVO -> StringUtils.isNotEmpty(userRoleVO.getRoleScope()))
                    .collect(Collectors.groupingBy(
                            UserRoleVO::getRoleScope,
                            Collectors.mapping(UserRoleVO::getUserId, Collectors.toList())
                    ));

            return JSON.toJSONString(moderatorIdsByRoleScope);
        });

        // 将缓存中的 JSON 字符串解析为 Map
        return JSON.parseObject(result, new TypeReference<>() {
        });
    }

}

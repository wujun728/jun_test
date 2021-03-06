package cn.iocoder.yudao.adminserver.modules.system.service.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.adminserver.modules.infra.service.file.InfFileService;
import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.profile.SysUserProfileUpdatePasswordReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.profile.SysUserProfileUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.user.*;
import cn.iocoder.yudao.adminserver.modules.system.convert.user.SysUserConvert;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.dept.SysPostDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.mysql.user.SysUserMapper;
import cn.iocoder.yudao.adminserver.modules.system.service.dept.SysDeptService;
import cn.iocoder.yudao.adminserver.modules.system.service.dept.SysPostService;
import cn.iocoder.yudao.adminserver.modules.system.service.permission.SysPermissionService;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.*;


/**
 * ?????? Service ?????????
 *
 * @author ????????????
 */
@Service
@Slf4j
public class SysUserServiceImpl implements SysUserService {

    @Value("${sys.user.init-password:yudaoyuanma}")
    private String userInitPassword;

    @Resource
    private SysUserMapper userMapper;

    @Resource
    private SysDeptService deptService;
    @Resource
    private SysPostService postService;
    @Resource
    private SysPermissionService permissionService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private InfFileService fileService;

    @Override
    public Long createUser(SysUserCreateReqVO reqVO) {
        // ???????????????
        this.checkCreateOrUpdate(null, reqVO.getUsername(), reqVO.getMobile(), reqVO.getEmail(),
            reqVO.getDeptId(), reqVO.getPostIds());
        // ????????????
        SysUserDO user = SysUserConvert.INSTANCE.convert(reqVO);
        user.setStatus(CommonStatusEnum.ENABLE.getStatus()); // ????????????
        user.setPassword(passwordEncoder.encode(reqVO.getPassword())); // ????????????
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public void updateUser(SysUserUpdateReqVO reqVO) {
        // ???????????????
        this.checkCreateOrUpdate(reqVO.getId(), reqVO.getUsername(), reqVO.getMobile(), reqVO.getEmail(),
            reqVO.getDeptId(), reqVO.getPostIds());
        // ????????????
        SysUserDO updateObj = SysUserConvert.INSTANCE.convert(reqVO);
        userMapper.updateById(updateObj);
    }

    @Override
    public void updateUserProfile(Long id, SysUserProfileUpdateReqVO reqVO) {
        // ???????????????
        this.checkUserExists(id);
        this.checkEmailUnique(id, reqVO.getEmail());
        this.checkMobileUnique(id, reqVO.getMobile());
        // ????????????
        userMapper.updateById(SysUserConvert.INSTANCE.convert(reqVO).setId(id));
    }

    @Override
    public void updateUserPassword(Long id, SysUserProfileUpdatePasswordReqVO reqVO) {
        // ?????????????????????
        this.checkOldPassword(id, reqVO.getOldPassword());
        // ????????????
        SysUserDO updateObj = new SysUserDO().setId(id);
        updateObj.setPassword(passwordEncoder.encode(reqVO.getNewPassword())); // ????????????
        userMapper.updateById(updateObj);
    }

    @Override
    public void updateUserAvatar(Long id, InputStream avatarFile) {
        this.checkUserExists(id);
        // ????????????
        String avatar = fileService.createFile(IdUtil.fastUUID(), IoUtil.readBytes(avatarFile));
        // ????????????
        SysUserDO sysUserDO = new SysUserDO();
        sysUserDO.setId(id);
        sysUserDO.setAvatar(avatar);
        userMapper.updateById(sysUserDO);
    }

    @Override
    public void updateUserPassword(Long id, String password) {
        // ??????????????????
        this.checkUserExists(id);
        // ????????????
        SysUserDO updateObj = new SysUserDO();
        updateObj.setId(id);
        updateObj.setPassword(passwordEncoder.encode(password)); // ????????????
        userMapper.updateById(updateObj);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        // ??????????????????
        this.checkUserExists(id);
        // ????????????
        SysUserDO updateObj = new SysUserDO();
        updateObj.setId(id);
        updateObj.setStatus(status);
        userMapper.updateById(updateObj);
    }

    @Override
    public void deleteUser(Long id) {
        // ??????????????????
        this.checkUserExists(id);
        // ????????????
        userMapper.deleteById(id);
        // ????????????????????????
        permissionService.processUserDeleted(id);
    }

    @Override
    public SysUserDO getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public SysUserDO getUser(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public PageResult<SysUserDO> getUserPage(SysUserPageReqVO reqVO) {
        return userMapper.selectPage(reqVO, this.getDeptCondition(reqVO.getDeptId()));
    }

    @Override
    public List<SysUserDO> getUsers(SysUserExportReqVO reqVO) {
        return userMapper.selectList(reqVO, this.getDeptCondition(reqVO.getDeptId()));
    }

    @Override
    public List<SysUserDO> getUsers(Collection<Long> ids) {
        return userMapper.selectBatchIds(ids);
    }

    @Override
    public List<SysUserDO> getUsersByNickname(String nickname) {
        return userMapper.selectListByNickname(nickname);
    }

    @Override
    public List<SysUserDO> getUsersByUsername(String username) {
        return userMapper.selectListByUsername(username);
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????
     *
     * @param deptId ????????????
     * @return ??????????????????
     */
    private Set<Long> getDeptCondition(Long deptId) {
        if (deptId == null) {
            return Collections.emptySet();
        }
        Set<Long> deptIds = CollectionUtils.convertSet(deptService.getDeptsByParentIdFromCache(
            deptId, true), SysDeptDO::getId);
        deptIds.add(deptId); // ????????????
        return deptIds;
    }

    private void checkCreateOrUpdate(Long id, String username, String mobile, String email,
                                     Long deptId, Set<Long> postIds) {
        // ??????????????????
        this.checkUserExists(id);
        // ?????????????????????
        this.checkUsernameUnique(id, username);
        // ?????????????????????
        this.checkMobileUnique(id, mobile);
        // ??????????????????
        this.checkEmailUnique(id, email);
        // ??????????????????????????????
        this.checkDeptEnable(deptId);
        // ??????????????????????????????
        this.checkPostEnable(postIds);
    }

    @VisibleForTesting
    void checkUserExists(Long id) {
        if (id == null) {
            return;
        }
        SysUserDO user = userMapper.selectById(id);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
    }

    @VisibleForTesting
    void checkUsernameUnique(Long id, String username) {
        if (StrUtil.isBlank(username)) {
            return;
        }
        SysUserDO user = userMapper.selectByUsername(username);
        if (user == null) {
            return;
        }
        // ?????? id ?????????????????????????????????????????? id ?????????
        if (id == null) {
            throw exception(USER_USERNAME_EXISTS);
        }
        if (!user.getId().equals(id)) {
            throw exception(USER_USERNAME_EXISTS);
        }
    }

    @VisibleForTesting
    void checkEmailUnique(Long id, String email) {
        if (StrUtil.isBlank(email)) {
            return;
        }
        SysUserDO user = userMapper.selectByEmail(email);
        if (user == null) {
            return;
        }
        // ?????? id ?????????????????????????????????????????? id ?????????
        if (id == null) {
            throw exception(USER_EMAIL_EXISTS);
        }
        if (!user.getId().equals(id)) {
            throw exception(USER_EMAIL_EXISTS);
        }
    }

    @VisibleForTesting
    void checkMobileUnique(Long id, String mobile) {
        if (StrUtil.isBlank(mobile)) {
            return;
        }
        SysUserDO user = userMapper.selectByMobile(mobile);
        if (user == null) {
            return;
        }
        // ?????? id ?????????????????????????????????????????? id ?????????
        if (id == null) {
            throw exception(USER_MOBILE_EXISTS);
        }
        if (!user.getId().equals(id)) {
            throw exception(USER_MOBILE_EXISTS);
        }
    }

    @VisibleForTesting
    void checkDeptEnable(Long deptId) {
        if (deptId == null) { // ???????????????
            return;
        }
        SysDeptDO dept = deptService.getDept(deptId);
        if (dept == null) {
            throw exception(DEPT_NOT_FOUND);
        }
        if (!CommonStatusEnum.ENABLE.getStatus().equals(dept.getStatus())) {
            throw exception(DEPT_NOT_ENABLE);
        }
    }

    @VisibleForTesting
    void checkPostEnable(Set<Long> postIds) {
        if (CollUtil.isEmpty(postIds)) { // ???????????????
            return;
        }
        List<SysPostDO> posts = postService.getPosts(postIds, null);
        if (CollUtil.isEmpty(posts)) {
            throw exception(POST_NOT_FOUND);
        }
        Map<Long, SysPostDO> postMap = CollectionUtils.convertMap(posts, SysPostDO::getId);
        postIds.forEach(postId -> {
            SysPostDO post = postMap.get(postId);
            if (post == null) {
                throw exception(POST_NOT_FOUND);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(post.getStatus())) {
                throw exception(POST_NOT_ENABLE, post.getName());
            }
        });
    }

    /**
     * ???????????????
     *
     * @param id          ?????? id
     * @param oldPassword ?????????
     */
    @VisibleForTesting
    void checkOldPassword(Long id, String oldPassword) {
        SysUserDO user = userMapper.selectById(id);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw exception(USER_PASSWORD_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // ??????????????????????????????????????????
    public SysUserImportRespVO importUsers(List<SysUserImportExcelVO> importUsers, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importUsers)) {
            throw exception(USER_IMPORT_LIST_IS_EMPTY);
        }
        SysUserImportRespVO respVO = SysUserImportRespVO.builder().createUsernames(new ArrayList<>())
            .updateUsernames(new ArrayList<>()).failureUsernames(new LinkedHashMap<>()).build();
        importUsers.forEach(importUser -> {
            // ??????????????????????????????????????????
            try {
                checkCreateOrUpdate(null, null, importUser.getMobile(), importUser.getEmail(),
                    importUser.getDeptId(), null);
            } catch (ServiceException ex) {
                respVO.getFailureUsernames().put(importUser.getUsername(), ex.getMessage());
                return;
            }
            // ???????????????????????????????????????
            SysUserDO existUser = userMapper.selectByUsername(importUser.getUsername());
            if (existUser == null) {
                userMapper.insert(SysUserConvert.INSTANCE.convert(importUser)
                    .setPassword(passwordEncoder.encode(userInitPassword))); // ??????????????????
                respVO.getCreateUsernames().add(importUser.getUsername());
                return;
            }
            // ???????????????????????????????????????
            if (!isUpdateSupport) {
                respVO.getFailureUsernames().put(importUser.getUsername(), USER_USERNAME_EXISTS.getMsg());
                return;
            }
            SysUserDO updateUser = SysUserConvert.INSTANCE.convert(importUser);
            updateUser.setId(existUser.getId());
            userMapper.updateById(updateUser);
            respVO.getUpdateUsernames().add(importUser.getUsername());
        });
        return respVO;
    }

}

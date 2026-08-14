package com.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.blog.constant.DelStatusConstant;
import com.blog.constant.MessageConstant;
import com.blog.constant.StatusConstant;
import com.blog.mapper.SysMenuMapper;
import com.blog.mapper.SysRoleMapper;
import com.blog.mapper.SysRoleMenuMapper;
import com.blog.mapper.SysUserMapper;
import com.blog.mapper.SysUserRoleMapper;
import com.blog.pojo.dto.RoleDTO;
import com.blog.pojo.dto.RoleMenuAssignDTO;
import com.blog.pojo.dto.RolePageQueryDTO;
import com.blog.pojo.entity.SysMenu;
import com.blog.pojo.entity.SysRole;
import com.blog.pojo.entity.SysRoleMenu;
import com.blog.pojo.vo.MenuTreeVO;
import com.blog.pojo.vo.RoleMenuTreeVO;
import com.blog.pojo.vo.SysRoleVo;
import com.blog.exception.CustomException;
import com.blog.result.PageResult;
import com.blog.service.SysRoleService;
import com.blog.utils.MenuTreeUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl implements SysRoleService {

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysMenuMapper menuMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    /**
     * 分页查询角色
     *
     * @param rolePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(RolePageQueryDTO rolePageQueryDTO) {
        PageHelper.startPage(rolePageQueryDTO.getPage(), rolePageQueryDTO.getPageSize());
        List<SysRoleVo> sysRoleVoList = roleMapper.pageQuery(rolePageQueryDTO);
        PageInfo<SysRoleVo> page = new PageInfo<>(sysRoleVoList);
        return new PageResult(page.getTotal(), page.getList());
    }

    /**
     * 新增角色
     *
     * @param roleDTO
     */
    @Override
    public void add(RoleDTO roleDTO) {
        SysRole sysRole = new SysRole();
        BeanUtils.copyProperties(roleDTO, sysRole);
        sysRole.setDeleteFlag(DelStatusConstant.ENABLE);
        sysRole.setCreateTime(LocalDateTime.now());
        sysRole.setUpdateTime(LocalDateTime.now());
        roleMapper.add(sysRole);
    }

    /**
     * 修改角色
     *
     * @param roleDTO
     */
    @Override
    public void update(RoleDTO roleDTO) {
        isAdmin(roleDTO.getId());
        SysRole sysRole = new SysRole();
        BeanUtils.copyProperties(roleDTO, sysRole);
        sysRole.setUpdateTime(LocalDateTime.now());
        roleMapper.update(sysRole);
    }

    /**
     * 逻辑删除角色
     *
     * @param id
     */
    @Override
    public void logicDelete(Long id) {
        isAdmin(id);
        SysRole sysRole = new SysRole();
        sysRole.setId(id);
        sysRole.setDeleteFlag(DelStatusConstant.DISABLE);
        sysRole.setUpdateTime(LocalDateTime.now());
        roleMapper.update(sysRole);
    }

    /**
     * 查询逻辑删除的角色列表
     *
     * @return
     */
    @Override
    public List<SysRoleVo> getLogicDelete() {
        return roleMapper.getLogicDelete();
    }

    /**
     * 恢复角色
     *
     * @param id
     */
    @Override
    public void recover(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new CustomException("角色不存在");
        }
        SysRole sysRole = new SysRole();
        sysRole.setId(id);
        sysRole.setDeleteFlag(DelStatusConstant.ENABLE);
        sysRole.setUpdateTime(LocalDateTime.now());
        roleMapper.update(sysRole);
    }

    /**
     * 彻底删除角色
     *
     * @param id
     */
    @Override
    @Transactional
    public void delete(Long id) {
        isAdmin(id);
        // 删除角色关联的用户、菜单权限数据
        userRoleMapper.deleteByRoleId(id);
        roleMenuMapper.deleteByRoleId(id);
        // 删除角色主表数据
        roleMapper.delete(id);
    }

    /**
     * 获取角色权限树
     *
     * @param id
     * @return
     */
    @Override
    public RoleMenuTreeVO getRoleMenuTree(Long id) {
        // 1. 查询所有菜单权限
        List<SysMenu> menuList = menuMapper.selectAllMenuList(StatusConstant.ENABLE,DelStatusConstant.ENABLE);
        // 2.转VO
        List<MenuTreeVO> allMenuList = BeanUtil.copyToList(menuList, MenuTreeVO.class);
        // 3.转树形结构
        List<MenuTreeVO> menuTreeVOList = MenuTreeUtil.buildTree(allMenuList);

        List<Long> checkedKeys = roleMenuMapper.getMenuByRoleId(id);

        return RoleMenuTreeVO.builder()
                .menuTreeVOList(menuTreeVOList)
                .checkedKeys(checkedKeys).build();
    }

    /**
     * 分配权限
     *
     * @param roleMenuAssignDTO
     */
    @Override
    @Transactional
    public void assignRoleMenu(RoleMenuAssignDTO roleMenuAssignDTO) {
        Long roleId = roleMenuAssignDTO.getRoleId();
        isAdmin(roleId);
        List<Long> menuIdList = roleMenuAssignDTO.getMenuIdList();
        // 先删除旧数据
        roleMenuMapper.deleteByRoleId(roleId);

        // 后插入新数据
        if (menuIdList != null && !menuIdList.isEmpty()) {
            List<SysRoleMenu> roleMenuList = menuIdList.stream()
                    .map(menuId -> SysRoleMenu.builder()
                            .roleId(roleId).menuId(menuId).createTime(LocalDateTime.now()).build())
                    .toList();

            roleMenuMapper.batchInsert(roleMenuList);
        }
    }

    public void isAdmin(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        if ("admin".equals(role.getRoleKey())) {
            throw new RuntimeException(MessageConstant.NOT_UPDATE_ADMIN);
        }
    }
}

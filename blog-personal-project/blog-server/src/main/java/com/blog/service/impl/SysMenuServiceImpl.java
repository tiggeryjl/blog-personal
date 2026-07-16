package com.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.blog.constant.*;
import com.blog.exception.CustomException;
import com.blog.mapper.SysMenuMapper;
import com.blog.mapper.SysRoleMapper;
import com.blog.mapper.SysRoleMenuMapper;
import com.blog.pojo.dto.MenuDTO;
import com.blog.pojo.entity.SysMenu;
import com.blog.pojo.entity.SysRoleMenu;
import com.blog.pojo.vo.SysMenuVo;
import com.blog.service.SysMenuService;
import com.blog.utils.MenuTreeUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl implements SysMenuService {

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private SysMenuMapper menuMapper;

    private static final Set<String> PROTECT_PERM_SET = new HashSet<>() {{
        add("sys:manage");
        add("sys:recycle");
    }};

    /**
     * 获取权限树
     *
     * @return
     */
    @Override
    public List<SysMenuVo> getMenuTree() {
        List<SysMenu> menuList = menuMapper.selectMenuList();
        List<SysMenuVo> menuVoList = BeanUtil.copyToList(menuList, SysMenuVo.class);
        return MenuTreeUtil.buildTree(menuVoList);
    }

    /**
     * 新增权限
     *
     * @param menuDTO
     */
    @Override
    @Transactional
    public void add(MenuDTO menuDTO) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(menuDTO, menu);
        if (LayoutConstant.PARENTID.equals(menu.getParentId())) {
            menu.setComponent(LayoutConstant.COMPONENT_LAYOUT);
        }
        menu.setDeleteFlag(DelStatusConstant.ENABLE);
        menu.setCreateTime(LocalDateTime.now());
        menu.setUpdateTime(LocalDateTime.now());
        menuMapper.add(menu);

        //给超管赋权
        SysRoleMenu roleMenu = SysRoleMenu.builder()
                .roleId(SystemConstant.SUPER_ADMIN_ROLE_ID)
                .menuId(menu.getId())
                .createTime(LocalDateTime.now()).build();
        roleMenuMapper.addMenu(roleMenu);
    }

    /**
     * 修改权限
     *
     * @param menuDTO
     */
    @Override
    @Transactional
    public void update(MenuDTO menuDTO) {

        SysMenu originMenu = menuMapper.selectById(menuDTO.getId());
        if (originMenu == null) {
            throw new CustomException("该菜单权限不存在");
        }

        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(menuDTO, menu);
        menu.setUpdateTime(LocalDateTime.now());

        menuMapper.update(menu);

        if (StatusConstant.ENABLE.equals(originMenu.getStatus())
                && StatusConstant.DISABLE.equals(menuDTO.getStatus())) {
            // 批量更新当前+所有子菜单为禁用
            menuMapper.updateChildMenuStatus(menuDTO.getId(), menuDTO.getStatus());
        }
    }

    /**
     * 逻辑删除权限
     *
     * @param id
     */
    @Override
    @Transactional
    public void logicDelete(Long id) {
        List<Long> allMenuIds = checkMenu(id);

        SysMenu menu = new SysMenu();
        menu.setDeleteFlag(DelStatusConstant.DISABLE);
        menu.setUpdateTime(LocalDateTime.now());
        menuMapper.batchUpdateByIds(menu, allMenuIds);
    }

    /**
     * 彻底删除权限
     *
     * @param id
     */
    @Override
    @Transactional
    public void delete(Long id) {
        List<Long> allMenuIds = checkMenu(id);

        // 删除角色菜单中间表的所有关联权限
        roleMenuMapper.deleteByMenuId(allMenuIds);
        // 删除菜单主表数据
        menuMapper.delete(id);
    }

    /**
     * 查询逻辑删除的权限树
     *
     * @return
     */
    @Override
    @Transactional
    public List<SysMenuVo> getdeleteMenuTree() {
        List<SysMenu> menuList = menuMapper.selectAllMenuList(null, DelStatusConstant.DISABLE);
        List<SysMenuVo> menuVoList = BeanUtil.copyToList(menuList, SysMenuVo.class);

        // 1. 提取所有非0的父ID，去重
        Set<Long> parentIdSet = menuVoList.stream()
                .map(SysMenuVo::getParentId)
                .filter(pid -> pid != null && pid != 0L)
                .collect(Collectors.toSet());

        Map<Long, String> parentNameMap = new HashMap<>();
        if (!parentIdSet.isEmpty()) {
            List<SysMenu> parentMenuList = menuMapper.selectBatchIds(new ArrayList<>(parentIdSet));
            parentNameMap = parentMenuList.stream()
                    .collect(Collectors.toMap(SysMenu::getId, SysMenu::getMenuName));
        }

        for (SysMenuVo vo : menuVoList) {
            Long pid = vo.getParentId();
            if (pid == 0L) {
                vo.setParentMenuName("顶级菜单");
            } else {
                vo.setParentMenuName(parentNameMap.getOrDefault(pid, "未知菜单"));
            }
        }

        return MenuTreeUtil.buildTree(menuVoList);
    }

    /**
     * 恢复删除权限
     *
     * @param id
     */
    @Override
    public void recover(Long id) {
        if (menuMapper.selectById(id) == null) {
            throw new CustomException("该菜单权限不存在");
        }

        List<Long> allMenuIds = menuMapper.getAllChildMenuIds(id);

        SysMenu menu = new SysMenu();
        menu.setDeleteFlag(DelStatusConstant.ENABLE);
        menu.setUpdateTime(LocalDateTime.now());
        menuMapper.batchUpdateByIds(menu, allMenuIds);
    }

    /**
     * 校验权限菜单是否在保护范围内
     *
     * @param id
     * @return
     */
    private List<Long> checkMenu(Long id) {
        SysMenu targetMenu = menuMapper.selectById(id);
        if (targetMenu == null) {
            throw new CustomException("该菜单权限不存在");
        }

        if (MultiStatusConstant.TWO.equals(targetMenu.getMenuType())
                && StatusConstant.DISABLE.equals(targetMenu.getStatus())) {
            return Collections.singletonList(id);
        }

        // 1. 获取本次要删除的所有菜单（当前+所有子节点）
        List<Long> allMenuIds = menuMapper.getAllChildMenuIds(id);

        // 2. 检查根节点的祖先（包括自身）是否有保护
        if (menuMapper.existsProtectedAncestor(id)) {
            throw new CustomException("当前菜单属于系统保护目录，禁止删除");
        }

        // 3. 检查待删除菜单中是否直接包含保护菜单
        if (menuMapper.countProtectedInIds(allMenuIds) > 0) {
            throw new CustomException("待删除菜单中包含系统保护目录，禁止删除");
        }

        return allMenuIds;
    }

}

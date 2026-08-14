package com.blog.controller.admin;

import com.blog.pojo.dto.RoleDTO;
import com.blog.pojo.dto.RoleMenuAssignDTO;
import com.blog.pojo.dto.RolePageQueryDTO;
import com.blog.pojo.vo.RoleMenuTreeVO;
import com.blog.pojo.vo.SysRoleVo;
import com.blog.pojo.vo.UserInfoVO;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/admin/role")
public class SysRoleController {

    @Autowired
    private SysRoleService roleService;

    /**
     * 查询角色
     * @param rolePageQueryDTO
     * @return
     */
    @PreAuthorize("hasAuthority('sys:role:list')")
    @GetMapping("/getRoleList")
    public Result<PageResult> getRoleList(RolePageQueryDTO rolePageQueryDTO) {
        log.info("分页查询角色:{}", rolePageQueryDTO);
        PageResult pageResult = roleService.pageQuery(rolePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 新增角色
     * @return
     */
    @PreAuthorize("hasAuthority('sys:role:add')")
    @PostMapping("/add")
    public Result addRole(@RequestBody RoleDTO roleDTO){
        log.info("新增角色:{}", roleDTO);
        roleService.add(roleDTO);
        return Result.success();
    }

    /**
     * 修改角色
     * @param roleDTO
     * @return
     */
    @PreAuthorize("hasAuthority('sys:role:edit')")
    @PutMapping("/update")
    public Result update(@RequestBody RoleDTO roleDTO){
        log.info("修改角色:{}", roleDTO);
        roleService.update(roleDTO);
        return Result.success();
    }

    /**
     * 逻辑删除角色
     * @param id
     * @return
     */
    @PreAuthorize("hasAuthority('sys:role:delete')")
    @DeleteMapping("/logicDelete")
    public Result logicDelete(@RequestParam Long id){
        log.info("逻辑删除角色的id{}",id);
        roleService.logicDelete(id);
        return Result.success();
    }

    /**
     * 查询逻辑删除的角色列表
     * @return
     */
    @PreAuthorize("hasAuthority('sys:recycleRole:list')")
    @GetMapping("/getLogicDelete")
    public Result<List<SysRoleVo>> getLogicDelete() {
        log.info("查询逻辑删除的角色列表");
        List<SysRoleVo> roleVoList = roleService.getLogicDelete();
        return Result.success(roleVoList);
    }

    /**
     * 恢复角色
     * @param id
     * @return
     */
    @PreAuthorize("hasAuthority('sys:recycleRole:recycle')")
    @PutMapping("/recover")
    public Result recover(@RequestParam Long id) {
        log.info("恢复角色的id{}", id);
        roleService.recover(id);
        return Result.success();
    }


    /**
     * 彻底删除角色
     * @param id
     * @return
     */
    @PreAuthorize("hasAuthority('sys:recycleRole:delete')")
    @DeleteMapping()
    public Result delete(@RequestParam Long id){
        log.info("彻底删除角色的id{}",id);
        roleService.delete(id);
        return Result.success();
    }

    /**
     * 获取角色权限树
     * @return
     */
    @PreAuthorize("hasAuthority('sys:role:assign')")
    @GetMapping("/menuTreeSelect/{id}")
    public Result<RoleMenuTreeVO> getRoleMenuTree(@PathVariable Long id) {
        log.info("获取角色权限树的角色id为:{}",id);
        RoleMenuTreeVO roleMenuTreeVO =roleService.getRoleMenuTree(id);
        return Result.success(roleMenuTreeVO);
    }

    /**
     * 分配权限
     * @return
     */
    @PreAuthorize("hasAuthority('sys:role:assign')")
    @PutMapping("/assignPermission")
    public Result<UserInfoVO> assignRoleMenu(@RequestBody RoleMenuAssignDTO roleMenuAssignDTO) {
        log.info("分配权限:{}",roleMenuAssignDTO);
        roleService.assignRoleMenu(roleMenuAssignDTO);
        return Result.success();
    }


}

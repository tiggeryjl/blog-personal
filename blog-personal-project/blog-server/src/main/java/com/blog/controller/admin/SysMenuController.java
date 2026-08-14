package com.blog.controller.admin;

import com.blog.pojo.dto.MenuDTO;
import com.blog.pojo.vo.SysMenuVo;
import com.blog.result.Result;
import com.blog.service.SysMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/admin/menu")
public class SysMenuController {

    @Autowired
    private SysMenuService menuService;

    /**
     * 获取权限树
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:menu:list')")
    @GetMapping("/getMenuTree")
    public Result<List<SysMenuVo>> getMenuTree() {
        log.info("获取权限树");
        List<SysMenuVo> menuTreeList = menuService.getMenuTree();
        return Result.success(menuTreeList);
    }

    /**
     * 新增权限
     * @param menuDTO
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:menu:add')")
    @PostMapping("/add")
    public Result addRole(@RequestBody MenuDTO menuDTO){
        log.info("新增权限:{}", menuDTO);
        menuService.add(menuDTO);
        return Result.success();
    }

    /**
     * 修改权限
     * @param menuDTO
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:menu:edit')")
    @PutMapping("/update")
    public Result update(@RequestBody MenuDTO menuDTO){
        log.info("修改权限:{}", menuDTO);
        menuService.update(menuDTO);
        return Result.success();
    }

    /**
     * 逻辑删除权限
     * @param id
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:menu:delete')")
    @DeleteMapping("/logicDelete")
    public Result logicDelete(@RequestParam Long id){
        log.info("逻辑删除权限的id{}",id);
        menuService.logicDelete(id);
        return Result.success();
    }


    /**
     * 查询逻辑删除的权限树
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:recycleMenu:list')")
    @GetMapping("/getLogicDelete")
    public Result<List<SysMenuVo>> getdeleteMenuTree() {
        log.info("查询逻辑删除的权限树");
        List<SysMenuVo> menuTreeList = menuService.getdeleteMenuTree();
        return Result.success(menuTreeList);
    }

    /**
     * 彻底删除权限
     * @param id
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:recycleMenu:delete')")
    @DeleteMapping()
    public Result delete(@RequestParam Long id){
        log.info("彻底删除权限的id{}",id);
        menuService.delete(id);
        return Result.success();
    }

    /**
     * 恢复删除权限
     * @param id
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:recycleMenu:recycle')")
    @PutMapping("/recover")
    public Result recover(@RequestParam Long id){
        log.info("恢复删除权限:{}", id);
        menuService.recover(id);
        return Result.success();
    }

}

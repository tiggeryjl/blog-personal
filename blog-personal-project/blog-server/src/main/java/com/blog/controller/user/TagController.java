package com.blog.controller.user;

import com.blog.pojo.dto.RecyclePageQueryDTO;
import com.blog.pojo.dto.TagDTO;
import com.blog.pojo.vo.OptionVO;
import com.blog.pojo.vo.TagFrontVo;
import com.blog.pojo.vo.TagVo;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签管理
 */
@Slf4j
@RestController("userTagController")
@RequestMapping("/user/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * 查询所有标签信息
     * @return
     */
    @GetMapping("tag")
    public Result<List<TagFrontVo>> getTagList() {
        log.info("查询所有启用标签信息");
        List<TagFrontVo> list=tagService.getTagList();
        return Result.success(list);
    }

}

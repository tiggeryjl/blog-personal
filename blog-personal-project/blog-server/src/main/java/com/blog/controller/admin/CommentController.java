package com.blog.controller.admin;

import com.blog.pojo.dto.CommentPageQueryDTO;
import com.blog.pojo.dto.CommentReplyDTO;
import com.blog.pojo.dto.CommentStatusDTO;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.CommentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 分页查询文章评论
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @PreAuthorize("hasPermission(null,'sys:comment:article:list')")
    @GetMapping("/article/list")
    public Result<PageResult> getArticleCommentList(CommentPageQueryDTO param) {
        param.setType(0);
        log.info("分页查询文章评论:{}", param);
        return Result.success(commentService.pageQuery(param));
    }

    /**
     * 分页查询日常评论
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @PreAuthorize("hasPermission(null,'sys:comment:daily:list')")
    @GetMapping("/daily/list")
    public Result<PageResult> getDailyCommentList(CommentPageQueryDTO param) {
        param.setType(1);
        log.info("分页查询日常评论:{}", param);
        return Result.success(commentService.pageQuery(param));
    }

    /**
     * 分页查询留言评论
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @PreAuthorize("hasPermission(null,'sys:comment:message:list')")
    @GetMapping("/message/list")
    public Result<PageResult> getMessageCommentList(CommentPageQueryDTO param) {
        param.setType(2);
        log.info("分页查询留言评论:{}", param);
        return Result.success(commentService.pageQuery(param));
    }

    /**
     * 审核/隐藏评论
     *
     * @param commentStatusDTO 评论ID与目标状态
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:comment:audit')")
    @PutMapping("/status")
    public Result updateStatus(@Valid @RequestBody CommentStatusDTO commentStatusDTO) {
        log.info("审核评论:{}", commentStatusDTO);
        commentService.updateStatus(commentStatusDTO);
        return Result.success();
    }

    /**
     * 后台回复评论
     *
     * @param commentReplyDTO 被回复评论ID与回复内容
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:comment:audit')")
    @PostMapping("/reply")
    public Result reply(@Valid @RequestBody CommentReplyDTO commentReplyDTO) {
        log.info("后台回复评论:{}", commentReplyDTO);
        commentService.addReply(commentReplyDTO);
        return Result.success();
    }

    /**
     * 置顶/取消置顶评论
     *
     * @param id 评论ID
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:comment:audit')")
    @PutMapping("/top/{id}")
    public Result updateTop(@PathVariable Long id) {
        log.info("置顶评论id:{}", id);
        commentService.updateTop(id);
        return Result.success();
    }

    /**
     * 批量逻辑删除评论
     *
     * @param ids 评论ID集合
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:comment:delete')")
    @DeleteMapping("/logicDelete")
    public Result logicDelete(@RequestParam List<Long> ids) {
        log.info("逻辑删除评论ids:{}", ids);
        commentService.logicDelete(ids);
        return Result.success();
    }

    /**
     * 批量彻底删除评论
     *
     * @param ids 评论ID集合
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:comment:delete')")
    @DeleteMapping()
    public Result delete(@RequestParam List<Long> ids) {
        log.info("彻底删除评论ids:{}", ids);
        commentService.delete(ids);
        return Result.success();
    }
}

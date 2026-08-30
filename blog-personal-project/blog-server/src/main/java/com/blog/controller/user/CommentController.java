package com.blog.controller.user;

import com.blog.pojo.dto.CommentPageQueryDTO;
import com.blog.pojo.dto.CommentReplyDTO;
import com.blog.pojo.dto.CommentStatusDTO;
import com.blog.pojo.entity.Comment;
import com.blog.pojo.vo.CommentVo;
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
@RestController("userCommentController")
@RequestMapping("/user/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 根据文章id查询文章评论
     * @param id
     * @return
     */
    @GetMapping("/article/{id}")
    public Result<List<CommentVo>> getArticleById(@PathVariable Long id) {
        log.info("查询文章id为{}的评论",id);
        List<CommentVo> commentList=commentService.getArticleById(id);
        return Result.success(commentList);
    }

    /**
     * 分页查询日常评论
     *
     * @param param 查询参数
     * @return 分页结果
     */
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
    @GetMapping("/message/list")
    public Result<PageResult> getMessageCommentList(CommentPageQueryDTO param) {
        param.setType(2);
        log.info("分页查询留言评论:{}", param);
        return Result.success(commentService.pageQuery(param));
    }


    /**
     * 回复评论
     *
     * @param commentReplyDTO 被回复评论ID与回复内容
     * @return 统一结果
     */
    @PostMapping("/reply")
    public Result reply(@Valid @RequestBody CommentReplyDTO commentReplyDTO) {
        log.info("后台回复评论:{}", commentReplyDTO);
        commentService.addReply(commentReplyDTO);
        return Result.success();
    }


}

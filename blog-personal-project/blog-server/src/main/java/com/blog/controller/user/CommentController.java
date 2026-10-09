package com.blog.controller.user;

import com.blog.pojo.dto.ArticleCommentDTO;
import com.blog.pojo.dto.CommentPageQueryDTO;
import com.blog.pojo.dto.CommentReplyDTO;
import com.blog.pojo.dto.DailyCommentDTO;
import com.blog.pojo.dto.MessageCommentDTO;
import com.blog.pojo.vo.CommentVo;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.CommentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Result<List<CommentVo>> getArticleById(@PathVariable("id") Long id) {
        log.info("查询文章id为{}的评论",id);
        List<CommentVo> commentList=commentService.getArticleById(id);
        return Result.success(commentList);
    }

    /**
     * 发表文章顶级评论
     *
     * @param articleId         文章ID
     * @param articleCommentDTO 评论内容
     * @return 新评论ID
     */
    @PostMapping("/article/{articleId}")
    public Result addArticleComment(@PathVariable("articleId") Long articleId,
                                    @Valid @RequestBody ArticleCommentDTO articleCommentDTO) {
        log.info("发表文章id为{}的评论:{}", articleId, articleCommentDTO);
        commentService.addArticleComment(articleId, articleCommentDTO.getContent());
        return Result.success();
    }

    /**
     * 根据日常ID查询用户端可见评论
     *
     * @param id 日常ID
     * @return 评论树
     */
    @GetMapping("/daily/{id}")
    public Result<List<CommentVo>> getDailyById(@PathVariable("id") Long id) {
        log.info("查询日常id为{}的评论", id);
        return Result.success(commentService.getDailyById(id));
    }

    /**
     * 发表日常顶级评论
     *
     * @param dailyId         日常ID
     * @param dailyCommentDTO 评论内容
     * @return 统一结果
     */
    @PostMapping("/daily/{dailyId}")
    public Result addDailyComment(@PathVariable("dailyId") Long dailyId,
                                  @Valid @RequestBody DailyCommentDTO dailyCommentDTO) {
        log.info("发表日常id为{}的评论:{}", dailyId, dailyCommentDTO);
        commentService.addDailyComment(dailyId, dailyCommentDTO.getContent());
        return Result.success();
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
        return Result.success(commentService.pageUserMessageQuery(param));
    }

    /**
     * 发表留言
     *
     * @param messageCommentDTO 留言类型与内容
     * @return 统一结果
     */
    @PostMapping("/message")
    public Result addMessageComment(@Valid @RequestBody MessageCommentDTO messageCommentDTO) {
        log.info("发表留言:{}", messageCommentDTO);
        commentService.addMessageComment(messageCommentDTO.getMsgType(), messageCommentDTO.getContent());
        return Result.success();
    }

    /**
     * 回复评论
     *
     * @param commentReplyDTO 被回复评论ID与回复内容
     * @return 统一结果
     */
    @PostMapping("/reply")
    public Result reply(@Valid @RequestBody CommentReplyDTO commentReplyDTO) {
        log.info("回复评论:{}", commentReplyDTO);
        commentService.addUserReply(commentReplyDTO);
        return Result.success();
    }


}

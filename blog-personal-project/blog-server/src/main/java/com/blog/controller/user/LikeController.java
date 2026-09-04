package com.blog.controller.user;

import com.blog.pojo.dto.LikeDTO;
import com.blog.pojo.vo.LikeVo;
import com.blog.result.Result;
import com.blog.service.LikeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户点赞管理
 */
@Slf4j
@RestController("userLikeController")
@RequestMapping("/user/like")
public class LikeController {

    @Autowired
    private LikeService likeService;

    /**
     * 点赞文章/评论
     *
     * @param likeDTO 点赞类型与目标ID
     * @return 点赞结果
     */
    @PostMapping
    public Result<LikeVo> like(@Valid @RequestBody LikeDTO likeDTO) {
        log.info("用户点赞:类型{} 目标ID:{}", likeDTO.getTargetType(), likeDTO.getTargetId());
        return Result.success(likeService.like(likeDTO.getTargetType(), likeDTO.getTargetId()));
    }
}

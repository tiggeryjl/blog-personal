package com.blog.controller.user;

import com.blog.pojo.vo.MusicVO;
import com.blog.result.Result;
import com.blog.service.MusicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端音乐
 */
@Slf4j
@RestController("userMusicController")
@RequestMapping("/user/music")
public class MusicController {

    @Autowired
    private MusicService musicService;

    /**
     * 获取上架音乐列表
     *
     * @return 音乐列表
     */
    @GetMapping("/list")
    public Result<List<MusicVO>> list() {
        return Result.success(musicService.listPublic());
    }
}

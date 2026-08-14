package com.blog.pojo.vo;

import com.blog.pojo.dto.SysNoticeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * WebSocket返回试图通知对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InitNoticeVO implements Serializable {

    //未读条数
    private long unreadTotal;

    //展示的最新N条记录
    private List<SysNoticeDTO> latestList;
}

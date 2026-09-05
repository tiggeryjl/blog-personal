package com.blog.mapper;

import com.blog.pojo.entity.UserLike;
import com.blog.pojo.vo.ArticleCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 用户点赞记录 Mapper
 */
@Mapper
public interface LikeMapper {

    /**
     * 查询当前用户已点赞的目标ID集合
     *
     * @param userId     用户ID
     * @param targetType 点赞目标类型
     * @param targetIds  目标ID集合
     * @return 已点赞的目标ID集合
     */
    List<Long> selectLikedIds(@Param("userId") Long userId,
                              @Param("targetType") Integer targetType,
                              @Param("targetIds") Collection<Long> targetIds);

    /**
     * 批量统计多个目标的点赞数
     *
     * @param targetType 点赞目标类型
     * @param targetIds  目标ID集合
     * @return 各目标ID对应的点赞数
     */
    List<ArticleCountVO> countByTargetIds(@Param("targetType") Integer targetType,
                                          @Param("targetIds") Collection<Long> targetIds);

    /**
     * 新增点赞记录，已存在正常点赞记录时忽略，避免重复点赞
     *
     * @param userLike 点赞记录
     * @return 实际新增行数
     */
    int insertIgnore(UserLike userLike);
}

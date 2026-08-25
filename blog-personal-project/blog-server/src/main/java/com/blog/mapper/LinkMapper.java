package com.blog.mapper;

import com.blog.pojo.dto.LinkPageQueryDTO;
import com.blog.pojo.entity.Link;
import com.blog.pojo.vo.LinkVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 友情链接 Mapper
 */
@Mapper
public interface LinkMapper {

    /**
     * 新增友链
     *
     * @param link 友链实体
     */
    void add(Link link);

    /**
     * 更新友链
     *
     * @param link 友链实体
     */
    void update(Link link);

    /**
     * 根据ID查询友链
     *
     * @param id 友链ID
     * @return 友链视图对象
     */
    @Select("select * from link where id = #{id}")
    LinkVo getById(Long id);

    /**
     * 分页查询友链列表
     *
     * @param dto 查询参数
     * @return 友链列表
     */
    List<LinkVo> pageQuery(LinkPageQueryDTO dto);

    /**
     * 统计各审核状态数量
     *
     * @return total/pending/approved/rejected 数量
     */
    Map<String, Long> countByAuditStatus();

    /**
     * 校验网站链接是否已存在（未删除数据）
     *
     * @param linkUrl 网站链接
     * @return 数量
     */
    int countByUrl(@Param("linkUrl") String linkUrl);

    /**
     * 根据网站链接查询未删除的友链
     *
     * @param linkUrl 网站链接
     * @return 友链视图对象
     */
    LinkVo getByUrl(@Param("linkUrl") String linkUrl);

    /**
     * 批量逻辑删除
     *
     * @param ids 友链ID集合
     */
    void logicDelete(@Param("ids") List<Long> ids);

    /**
     * 查询已通过且启用的友链（博客端展示）
     *
     * @return 友链列表
     */
    List<LinkVo> listPublic();

    /**
     * 查询全部待审核/已拒绝的友链申请（博客端展示）
     *
     * @return 友链列表
     */
    List<LinkVo> listApplications();
}

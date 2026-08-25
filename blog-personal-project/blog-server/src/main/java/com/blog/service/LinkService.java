package com.blog.service;

import com.blog.pojo.dto.LinkApplyDTO;
import com.blog.pojo.dto.LinkAuditDTO;
import com.blog.pojo.dto.LinkDTO;
import com.blog.pojo.dto.LinkPageQueryDTO;
import com.blog.pojo.vo.LinkVo;
import com.blog.result.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 友情链接服务
 */
public interface LinkService {

    /**
     * 分页查询友链列表
     *
     * @param dto 查询参数
     * @return 分页结果
     */
    PageResult pageQuery(LinkPageQueryDTO dto);

    /**
     * 统计各审核状态数量
     *
     * @return total/pending/approved/rejected 数量
     */
    Map<String, Long> stats();

    /**
     * 根据ID查询友链
     *
     * @param id 友链ID
     * @return 友链视图对象
     */
    LinkVo getById(Long id);

    /**
     * 新增友链
     *
     * @param dto 友链参数
     */
    void add(LinkDTO dto);

    /**
     * 编辑友链
     *
     * @param dto 友链参数
     */
    void update(LinkDTO dto);

    /**
     * 审核友链（通过/拒绝）
     *
     * @param dto 审核参数
     */
    void audit(LinkAuditDTO dto);

    /**
     * 启用/禁用友链
     *
     * @param id     友链ID
     * @param status 目标展示状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 批量逻辑删除友链
     *
     * @param ids 友链ID集合
     */
    void delete(List<Long> ids);

    /**
     * 博客端提交友链申请
     *
     * @param dto 申请参数
     */
    void apply(LinkApplyDTO dto);

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

    /**
     * 催促审核本人的友链申请
     *
     * @param id 友链申请ID
     */
    void urge(Long id);
}

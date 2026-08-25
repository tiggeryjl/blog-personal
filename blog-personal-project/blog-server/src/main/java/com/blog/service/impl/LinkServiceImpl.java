package com.blog.service.impl;

import com.blog.constant.DelStatusConstant;
import com.blog.constant.LinkStatusConstant;
import com.blog.constant.StatusConstant;
import com.blog.context.BaseContext;
import com.blog.exception.LinkException;
import com.blog.mapper.LinkMapper;
import com.blog.mapper.SysUserMapper;
import com.blog.pojo.dto.LinkApplyDTO;
import com.blog.pojo.dto.LinkAuditDTO;
import com.blog.pojo.dto.LinkDTO;
import com.blog.pojo.dto.LinkPageQueryDTO;
import com.blog.pojo.entity.Link;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.vo.LinkVo;
import com.blog.result.PageResult;
import com.blog.service.LinkService;
import com.blog.service.NoticeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 友链
 */
@Slf4j
@Service
public class LinkServiceImpl implements LinkService {

    @Autowired
    private LinkMapper linkMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private NoticeService noticeService;

    /**
     * 分页查询友链列表
     */
    @Override
    public PageResult pageQuery(LinkPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        List<LinkVo> linkList = linkMapper.pageQuery(dto);
        PageInfo<LinkVo> pageInfo = new PageInfo<>(linkList);
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 统计各审核状态数量
     */
    @Override
    public Map<String, Long> stats() {
        return linkMapper.countByAuditStatus();
    }

    /**
     * 根据ID查询友链
     */
    @Override
    public LinkVo getById(Long id) {
        return linkMapper.getById(id);
    }

    /**
     * 新增友链
     */
    @Override
    public void add(LinkDTO dto) {
        checkUrlUnique(dto.getLinkUrl(), null);
        Link link = new Link();
        BeanUtils.copyProperties(dto, link);
        link.setAuditStatus(dto.getAuditStatus() == null ? LinkStatusConstant.AUDIT_APPROVED : dto.getAuditStatus());
        link.setStatus(dto.getStatus() == null ? StatusConstant.ENABLE : dto.getStatus());
        link.setDeleteFlag(DelStatusConstant.ENABLE);
        link.setCreateTime(LocalDateTime.now());
        link.setUpdateTime(LocalDateTime.now());
        linkMapper.add(link);
    }

    /**
     * 编辑友链
     */
    @Override
    public void update(LinkDTO dto) {
        if (dto.getId() == null) {
            throw new LinkException("友链ID不能为空");
        }
        LinkVo exist = linkMapper.getById(dto.getId());
        if (exist == null || DelStatusConstant.DISABLE.equals(exist.getDeleteFlag())) {
            throw new LinkException("友链不存在");
        }
        checkUrlUnique(dto.getLinkUrl(), dto.getId());
        Link link = new Link();
        BeanUtils.copyProperties(dto, link);
        link.setUpdateTime(LocalDateTime.now());
        linkMapper.update(link);
    }

    /**
     * 审核友链
     * 通过时同步启用展示，拒绝时同步禁用展示
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(LinkAuditDTO dto) {
        if (dto.getAuditStatus() == null
                || (dto.getAuditStatus() != LinkStatusConstant.AUDIT_APPROVED && dto.getAuditStatus() != LinkStatusConstant.AUDIT_REJECTED)) {
            throw new LinkException("审核状态不合法");
        }
        for (Long id : dto.getIds()) {
            LinkVo exist = linkMapper.getById(id);
            if (exist == null || DelStatusConstant.DISABLE.equals(exist.getDeleteFlag())) {
                throw new LinkException("友链不存在，id=" + id);
            }
            Link link = Link.builder()
                    .id(id)
                    .auditStatus(dto.getAuditStatus())
                    .status(dto.getAuditStatus() == LinkStatusConstant.AUDIT_APPROVED
                            ? StatusConstant.ENABLE : StatusConstant.DISABLE)
                    .updateTime(LocalDateTime.now())
                    .build();
            linkMapper.update(link);
        }
    }

    /**
     * 启用/禁用友链
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        if (!StatusConstant.ENABLE.equals(status) && !StatusConstant.DISABLE.equals(status)) {
            throw new LinkException("展示状态不合法");
        }
        LinkVo exist = linkMapper.getById(id);
        if (exist == null || DelStatusConstant.DISABLE.equals(exist.getDeleteFlag())) {
            throw new LinkException("友链不存在");
        }
        Link link = Link.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .build();
        linkMapper.update(link);
    }

    /**
     * 批量逻辑删除友链
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new LinkException("请选择要删除的友链");
        }
        linkMapper.logicDelete(ids);
    }

    /**
     * 博客端提交友链申请，进入待审核状态
     */
    @Override
    public void apply(LinkApplyDTO dto) {
        String url = dto.getLinkUrl() == null ? "" : dto.getLinkUrl().trim();
        String name = dto.getLinkName() == null ? "" : dto.getLinkName().trim();
        String email = dto.getEmail() == null ? null : dto.getEmail().trim();

        // 已拒绝的同链接申请：更新回“待审核”，避免与唯一索引冲突
        LinkVo exist = linkMapper.getByUrl(url);
        if (exist != null) {
            Integer auditStatus = exist.getAuditStatus();
            if (auditStatus != null
                    && (auditStatus == LinkStatusConstant.AUDIT_PENDING || auditStatus == LinkStatusConstant.AUDIT_APPROVED)) {
                throw new LinkException("该网站链接已存在，请勿重复申请");
            }
            Link update = Link.builder()
                    .id(exist.getId())
                    .linkName(name)
                    .linkUrl(url)
                    .linkAvatar(dto.getLinkAvatar())
                    .linkDesc(dto.getLinkDesc())
                    .linkEmail(email)
                    .sort(0)
                    .auditStatus(LinkStatusConstant.AUDIT_PENDING)
                    .status(StatusConstant.DISABLE)
                    .updateTime(LocalDateTime.now())
                    .build();
            linkMapper.update(update);
            createApplyNotice(name, email, exist.getId());
            return;
        }

        Link link = Link.builder()
                .linkName(name)
                .linkUrl(url)
                .linkAvatar(dto.getLinkAvatar())
                .linkDesc(dto.getLinkDesc())
                .linkEmail(email)
                .sort(0)
                .auditStatus(LinkStatusConstant.AUDIT_PENDING)
                .status(StatusConstant.DISABLE)
                .deleteFlag(DelStatusConstant.ENABLE)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        linkMapper.add(link);
        createApplyNotice(name, email, link.getId());
    }

    /**
     * 生成“用户申请友链”通知
     */
    private void createApplyNotice(String linkName, String email, Long linkId) {
        String safeName = (linkName == null || linkName.trim().isEmpty()) ? "友链" : linkName.trim();
        // 博客端申请时无登录态，优先用邮箱前缀标识申请人
        String operatorName = "访客";
        if (email != null && !email.trim().isEmpty()) {
            String raw = email.trim();
            int atIndex = raw.indexOf('@');
            operatorName = atIndex > 0 ? raw.substring(0, atIndex) : raw;
        }
        noticeService.createNotice(
                "link",
                "友链申请",
                "申请",
                safeName,
                linkId,
                operatorName,
                "网站「" + safeName + "」提交了友链申请，正在等待审核，请及时处理");
    }

    /**
     * 查询已通过且启用的友链
     */
    @Override
    public List<LinkVo> listPublic() {
        return linkMapper.listPublic();
    }

    /**
     * 查询全部待审核/已拒绝的友链申请
     */
    @Override
    public List<LinkVo> listApplications() {
        return linkMapper.listApplications();
    }

    /**
     * 催促审核本人的友链申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void urge(Long id) {
        LinkVo link = linkMapper.getById(id);
        if (link == null || DelStatusConstant.DISABLE.equals(link.getDeleteFlag())) {
            throw new LinkException("友链申请不存在");
        }
        SysUser user = sysUserMapper.getByUserId(BaseContext.getCurrentId());
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new LinkException("当前账号未绑定邮箱，无法催促审核");
        }
        if (link.getLinkEmail() == null
                || !link.getLinkEmail().trim().equalsIgnoreCase(user.getEmail().trim())) {
            throw new LinkException("只能催促本人申请的友链");
        }
        String linkName = link.getLinkName() == null ? "友链" : link.getLinkName();
        String operatorName = user.getNickname() == null ? "访客" : user.getNickname();
        noticeService.createNotice(
                "link",
                "友链审核提醒",
                "催促审核",
                linkName,
                link.getId(),
                operatorName,
                "博主「"+operatorName+"」的友链「" + linkName + "」申请正在等待审核，请及时处理");
    }

    /**
     * 校验网站链接唯一性
     *
     * @param linkUrl 网站链接
     * @param excludeId 排除的友链ID（编辑时传）
     */
    private void checkUrlUnique(String linkUrl, Long excludeId) {
        if (linkUrl == null || linkUrl.trim().isEmpty()) {
            return;
        }
        String url = linkUrl.trim();
        if (linkMapper.countByUrl(url) > 0) {
            LinkVo same = null;
            // 编辑时若改动的 URL 与自身相同则放行
            if (excludeId != null) {
                same = linkMapper.getById(excludeId);
            }
            if (same == null || !url.equals(same.getLinkUrl())) {
                throw new LinkException("该网站链接已存在，请勿重复申请");
            }
        }
    }
}

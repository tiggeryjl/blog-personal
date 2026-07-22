package com.blog.service.impl;

import com.blog.constant.ArticleStatusConstant;
import com.blog.constant.DelStatusConstant;
import com.blog.constant.StatusConstant;
import com.blog.context.BaseContext;
import com.blog.exception.ArticleException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.SysUserMapper;
import com.blog.pojo.dto.ArticleDTO;
import com.blog.pojo.dto.ArticlePageQueryDTO;
import com.blog.pojo.dto.ArticleTagDTO;
import com.blog.pojo.entity.Article;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.vo.ArticleVo;
import com.blog.result.PageResult;
import com.blog.service.AiService;
import com.blog.service.ArticleService;
import com.blog.utils.ArticleUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private AiService aiService;

    /**
     * 新增文章
     *
     * @param articleDTO
     */
    @Override
    public void add(ArticleDTO articleDTO) {
        Article article = new Article();
        BeanUtils.copyProperties(articleDTO, article);

        try {
            String summary = aiService.generateSummary(article.getContent());
            if (StringUtils.hasText(summary)) {
                article.setSummary(summary);
            } else {
                throw new RuntimeException("AI 返回空摘要"); // 主动跳转到 catch
            }
        } catch (RuntimeException e) {
            if (article.getSummary() == null || article.getSummary().isEmpty()) {
                log.info("AI 生成摘要失败，使用本地自动截取。原因：{}", e.getMessage());
                String autoSummary = ArticleUtil.generateSummary(article.getContent(), 150);
                article.setSummary(autoSummary);
            }
        }

        article.setIsTop(StatusConstant.DISABLE);
        article.setIsHot(StatusConstant.DISABLE);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        if (ArticleStatusConstant.PUBLISHED.equals(articleDTO.getStatus())) {
            article.setPublishTime(LocalDateTime.now());
        }

        SysUser user = sysUserMapper.getByUserId(BaseContext.getCurrentId());
        article.setUserId(user.getId());
        article.setUserNickname(user.getNickname());
        article.setUserAvatar(user.getAvatar());
        article.setDeleteFlag(DelStatusConstant.ENABLE);

        articleMapper.add(article);
    }

    /**
     * 分页查询文章列表
     *
     * @param articlePageQueryDTO
     */
    @Override
    public PageResult pageQurey(ArticlePageQueryDTO articlePageQueryDTO) {
        PageHelper.startPage(articlePageQueryDTO.getPage(), articlePageQueryDTO.getPageSize());
        List<ArticleVo> articleList = articleMapper.pageQurey(articlePageQueryDTO);
        PageInfo<ArticleVo> pageInfo = new PageInfo<>(articleList);
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 根据ID查询文章
     *
     * @param id
     * @return
     */
    @Override
    public ArticleVo getArticleById(Long id) {
        Article article = articleMapper.getArticleById(id);
        List<Long> tagIds = articleTagMapper.selectRelationArticleId(id);
        ArticleVo articleVo = new ArticleVo();
        BeanUtils.copyProperties(article, articleVo);
        articleVo.setTags(tagIds);
        return articleVo;
    }

    /**
     * 修改文章
     * @param articleDTO
     */
    @Override
    @Transactional
    public void update(ArticleDTO articleDTO) {
        Article article = articleMapper.getArticleById(articleDTO.getId());

        if (article == null) {
            throw new ArticleException("文章不存在");
        }
        // 归档文章封存不可修改
        if (ArticleStatusConstant.ARCHIVED.equals(article.getStatus())){
            throw new ArticleException("已归档文章请先取消归档后再编辑");
        }

        BeanUtils.copyProperties(articleDTO, article, "status");
        try {
            String summary = aiService.generateSummary(article.getContent());
            if (StringUtils.hasText(summary)) {
                article.setSummary(summary);
            } else {
                throw new RuntimeException("AI 返回空摘要"); // 主动跳转到 catch
            }
        } catch (RuntimeException e) {
            if (article.getSummary() == null || article.getSummary().isEmpty()) {
                log.info("AI 生成摘要失败，使用本地自动截取。原因：{}", e.getMessage());
                String autoSummary = ArticleUtil.generateSummary(article.getContent(), 150);
                article.setSummary(autoSummary);
            }
        }
        article.setUpdateTime(LocalDateTime.now());

        Integer actionType = articleDTO.getStatus();
        Integer finalStatus = calculateFinalStatus(article.getStatus(), actionType);
        article.setStatus(finalStatus);

        if (ArticleStatusConstant.PUBLISHED.equals(finalStatus)) {
            // 转为已发布时，更新发布时间，清空定时时间
            article.setPublishTime(LocalDateTime.now());
            article.setTimedPublishTime(null);
        }

        articleMapper.update(article);

        // 修改标签
        if (articleDTO.getTags() != null) {
            articleTagMapper.deleteByArticleId(articleDTO.getId());
            if (!articleDTO.getTags().isEmpty()) {
                ArticleTagDTO articleTagDTO = ArticleTagDTO.builder()
                        .articleId(articleDTO.getId())
                        .tagIds(articleDTO.getTags())
                        .createTime(LocalDateTime.now())
                        .build();
                articleTagMapper.batchInsert(articleTagDTO);
            }
        }
    }

    /**
     * 根据传递的状态值判断状态操作
     * @param oldStatus 原文章状态
     * @param actionType 操作类型 0=保存草稿 1=发布
     * @return 最终入库状态
     */
    private Integer calculateFinalStatus(Integer oldStatus, Integer actionType) {
        if (ArticleStatusConstant.DRAFT.equals(actionType)
                && ArticleStatusConstant.PUBLISHED.equals(actionType)) {
            throw new ArticleException("非法操作类型");
        }
        // 发布操作：统一转为已发布（除了归档都可以发布）
        if (ArticleStatusConstant.PUBLISHED.equals(actionType)) {
            return ArticleStatusConstant.PUBLISHED;
        }

        // 草稿操作：保持原状态不变，已发布、已下架、定时、私密，保存草稿都不改变原状态
        return oldStatus;
    }

    /**
     * 设置定时发布
     */
    public void setTimedPublish(ArticleDTO articleDTO) {
        // 1. 校验文章是否存在
        Article oldArticle = articleMapper.getArticleById(articleDTO.getId());
        if (oldArticle == null) {
            throw new ArticleException("文章不存在");
        }

        // 2. 校验状态：只允许草稿、已下架设置定时
        Integer status = oldArticle.getStatus();
        if (status != 0 && status != 2) {
            throw new ArticleException("仅草稿和已下架状态的文章可设置定时发布");
        }

        // 3. 校验定时时间合法性
        if (articleDTO.getTimedPublishTime() == null) {
            throw new ArticleException("请选择定时发布时间");
        }
        if (articleDTO.getTimedPublishTime().isBefore(LocalDateTime.now())) {
            throw new ArticleException("定时发布时间必须晚于当前时间");
        }

        Article article = Article.builder()
                .id(articleDTO.getId())
                .timedPublishTime(articleDTO.getTimedPublishTime())
                .status(ArticleStatusConstant.SCHEDULED)
                .updateTime(LocalDateTime.now()).build();
        // 4. 执行更新
        articleMapper.updateTimedPublish(article);
    }

    /**
     * 取消定时发布
     */
    public void cancelTimedPublish(Long id) {
        // 1. 校验文章
        Article oldArticle = articleMapper.getArticleById(id);
        if (oldArticle == null) {
            throw new ArticleException("文章不存在");
        }

        // 2. 校验状态：只有定时发布状态才能取消
        if (!ArticleStatusConstant.SCHEDULED.equals(oldArticle.getStatus())) {
            throw new ArticleException("当前文章不是定时发布状态，无法取消");
        }

        Article article = Article.builder()
                .id(id)
                .status(ArticleStatusConstant.DRAFT)
                .updateTime(LocalDateTime.now()).build();

        // 3. 取消后变回草稿状态，清空定时时间
        articleMapper.cancelTimedPublish(article);
    }
}

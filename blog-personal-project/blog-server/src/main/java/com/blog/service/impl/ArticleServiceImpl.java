package com.blog.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.blog.constant.ArticleStatusConstant;
import com.blog.constant.DelStatusConstant;
import com.blog.constant.LikeConstant;
import com.blog.constant.RedisConstant;
import com.blog.constant.StatusConstant;
import com.blog.context.BaseContext;
import com.blog.exception.ArticleException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.LikeMapper;
import com.blog.mapper.SysUserMapper;
import com.blog.pojo.dto.ArticleDTO;
import com.blog.pojo.dto.ArticlePageQueryDTO;
import com.blog.pojo.dto.ArticleTagDTO;
import com.blog.pojo.entity.Article;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.vo.ArticleDetailVO;
import com.blog.pojo.vo.ArticleVo;
import com.blog.pojo.vo.SimpleArticleVO;
import com.blog.result.PageResult;
import com.blog.service.AiService;
import com.blog.service.ArticleService;
import com.blog.service.RedisService;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private AiService aiService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private LikeMapper likeMapper;

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
        article.setViewNum(0L);
        article.setLikeNum(0L);
        article.setCommentNum(0L);
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

    public static String genQueryMd5(ArticlePageQueryDTO dto) {
        // 拼接所有条件，按固定顺序！
        StringBuilder sb = new StringBuilder();
        sb.append("title=").append(StrUtil.nullToEmpty(dto.getTitle())).append("&");
        sb.append("categoryId=").append(StrUtil.nullToEmpty(dto.getCategoryId())).append("&");
        sb.append("tag=").append(StrUtil.nullToEmpty(dto.getTag())).append("&");
        sb.append("isTop=").append(dto.getIsTop()).append("&");
        sb.append("status=").append(dto.getStatus()).append("&");
        sb.append("begin=").append(dto.getBegin()).append("&");
        sb.append("end=").append(dto.getEnd());
        // md5加密缩短字符串
        return DigestUtil.md5Hex(sb.toString());
    }

    /**
     * 分页查询文章列表
     *
     * @param articlePageQueryDTO
     */
    @Override
    public PageResult pageQurey(ArticlePageQueryDTO articlePageQueryDTO) {

        //查缓存
        String md5 = genQueryMd5(articlePageQueryDTO);
        String cacheKey = RedisConstant.ARTICLE_LIST_KEY + ":" + articlePageQueryDTO.getPage() + ":" + articlePageQueryDTO.getPageSize() + ":" + md5;
        Object articleCache = redisService.get(cacheKey,PageResult.class);
        if (articleCache != null) {
            return (PageResult) articleCache;
        }

        PageHelper.startPage(articlePageQueryDTO.getPage(), articlePageQueryDTO.getPageSize());
        List<ArticleVo> articleList = articleMapper.pageQurey(articlePageQueryDTO);
        PageInfo<ArticleVo> pageInfo = new PageInfo<>(articleList);
        PageResult pageResult = new PageResult(pageInfo.getTotal(), pageInfo.getList());

        //存入缓存 10分钟过期
        redisService.set(cacheKey, pageResult, 600);
        return pageResult;
    }

    /**
     * 分页查询逻辑删除的文章（回收站）
     *
     * @param articlePageQueryDTO
     */
    @Override
    public PageResult recyclePageQuery(ArticlePageQueryDTO articlePageQueryDTO) {
        PageHelper.startPage(articlePageQueryDTO.getPage(), articlePageQueryDTO.getPageSize());
        List<ArticleVo> articleList = articleMapper.recyclePageQuery(articlePageQueryDTO);
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
    public ArticleDetailVO getArticleById(Long id) {
        ArticleVo articleVo = articleMapper.getArticleVoById(id);
        if(articleVo == null){
            return null;
        }

        Article prev = articleMapper.selectPrevArticle(articleVo.getId());
        Article next = articleMapper.selectNextArticle(articleVo.getId());

        ArticleDetailVO articleDetailVO = new ArticleDetailVO();
        articleDetailVO.setArticleVo(articleVo);
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId != null) {
            List<Long> likedIds = likeMapper.selectLikedIds(
                    currentUserId, LikeConstant.TARGET_ARTICLE, Collections.singletonList(articleVo.getId()));
            articleDetailVO.setLiked(likedIds != null && !likedIds.isEmpty());
        }
        if(prev != null){
            SimpleArticleVO prevArticleVO =SimpleArticleVO.builder()
                    .id(prev.getId()).title(prev.getTitle()).createTime(prev.getCreateTime()).build();
            articleDetailVO.setPrevArticle(prevArticleVO);
        }
        if(next != null){
            SimpleArticleVO nextArticleVO =SimpleArticleVO.builder()
                    .id(next.getId()).title(next.getTitle()).createTime(next.getCreateTime()).build();
            articleDetailVO.setNextArticle(nextArticleVO);
        }
        return articleDetailVO;
    }

    /**
     * 修改文章
     *
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
        if (ArticleStatusConstant.ARCHIVED.equals(article.getStatus())) {
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
     *
     * @param oldStatus  原文章状态
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
        if (status != 0 && status != 2 && status != 4) {
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

        // 2. 只有定时发布状态才能取消
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

    /**
     * 批量逻辑删除
     *
     * @param ids
     */
    @Override
    public void logicDelete(List<Long> ids) {
        articleMapper.deleteBatchLogic(ids, DelStatusConstant.DISABLE);
    }

    /**
     * 彻底删除
     *
     * @param ids
     */
    @Override
    public void delete(List<Long> ids) {
        articleMapper.deleteBatch(ids);
    }

    /**
     * 批量恢复（回收站 -> 正常列表）
     *
     * @param ids
     */
    @Override
    public void recover(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ArticleException("请选择要恢复的文章");
        }
        articleMapper.recoverBatch(ids);
    }

    /**
     * 修改文章状态
     *
     * @param articleDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(ArticleDTO articleDTO) {

        if (articleDTO.getId() == null || articleDTO.getStatus() == null) {
            throw new ArticleException("文章ID、设置的目标状态不能为空");
        }

        Article oldArticle = articleMapper.getArticleById(articleDTO.getId());
        if (oldArticle == null) {
            throw new ArticleException("文章不存在");
        }

        Article article = Article.builder()
                .id(articleDTO.getId())
                .updateTime(LocalDateTime.now()).build();

        //设置发布，文章为草稿或定时发布或归档或私密时设置
        if (ArticleStatusConstant.PUBLISHED.equals(articleDTO.getStatus()) && List.of(
                ArticleStatusConstant.DRAFT,
                ArticleStatusConstant.REMOVED,
                ArticleStatusConstant.ARCHIVED,
                ArticleStatusConstant.SCHEDULED,
                ArticleStatusConstant.PRIVATE).contains(oldArticle.getStatus())) {

            article.setStatus(ArticleStatusConstant.PUBLISHED);
            article.setPublishTime(LocalDateTime.now());
        }
        //设置下架，文章为发布时设置
        else if (ArticleStatusConstant.REMOVED.equals(articleDTO.getStatus())
                && ArticleStatusConstant.PUBLISHED.equals(oldArticle.getStatus())) {
            article.setStatus(ArticleStatusConstant.REMOVED);
        }
        //设置归档，文章为发布时设置
        else if (ArticleStatusConstant.ARCHIVED.equals(articleDTO.getStatus())
                && ArticleStatusConstant.PUBLISHED.equals(oldArticle.getStatus())) {
            article.setStatus(ArticleStatusConstant.ARCHIVED);
        }
        //设置私密，文章为发布时设置
        else if (ArticleStatusConstant.PRIVATE.equals(articleDTO.getStatus())
                && (ArticleStatusConstant.PUBLISHED.equals(oldArticle.getStatus())
                || ArticleStatusConstant.ARCHIVED.equals(oldArticle.getStatus()))) {
            article.setStatus(ArticleStatusConstant.PRIVATE);
        }
        // 定时改成私密、归档
        else if (ArticleStatusConstant.SCHEDULED.equals(oldArticle.getStatus())
                && (ArticleStatusConstant.PRIVATE.equals(articleDTO.getStatus())
                || ArticleStatusConstant.ARCHIVED.equals(articleDTO.getStatus()))) {
            article.setStatus(articleDTO.getStatus());
        } else {
            throw new ArticleException("当前状态不允许变更为目标状态");
        }

        //目标不是定时，清空定时时间
        if (!ArticleStatusConstant.SCHEDULED.equals(articleDTO.getStatus())) {
            article.setTimedPublishTime(null);
        }

        articleMapper.update(article);
    }

    /**
     * 置顶设置
     *
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTop(Long id) {
        Article oldArticle = articleMapper.getArticleById(id);
        if (oldArticle == null) {
            throw new ArticleException("文章不存在");
        }

        if (!(ArticleStatusConstant.PUBLISHED.equals(oldArticle.getStatus())
                || ArticleStatusConstant.ARCHIVED.equals(oldArticle.getStatus()))) {
            throw new ArticleException("当前文章状态不允许执行置顶操作");
        }

        Integer newIsTop = StatusConstant.ENABLE.equals(oldArticle.getIsTop()) ? StatusConstant.DISABLE : StatusConstant.ENABLE;
        Article article = Article.builder()
                .id(id)
                .isTop(newIsTop)
                .updateTime(LocalDateTime.now()).build();
        articleMapper.update(article);
    }
}

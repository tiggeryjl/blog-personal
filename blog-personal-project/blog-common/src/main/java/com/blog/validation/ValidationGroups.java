package com.blog.validation;

import lombok.Data;

/**
 * 参数校验分组
 */
@Data
public final class ValidationGroups {

    /**
     * 新增操作
     */
    public interface Create {
    }

    /**
     * 完整编辑操作
     */
    public interface Update {
    }

    /**
     * 状态修改操作
     */
    public interface Status {
    }

    /**
     * 定时发布操作
     */
    public interface Timed {
    }

    /**
     * AI 写文章
     */
    public interface WriteArticle {
    }

    /**
     * AI 润色文章
     */
    public interface PolishArticle {
    }
}

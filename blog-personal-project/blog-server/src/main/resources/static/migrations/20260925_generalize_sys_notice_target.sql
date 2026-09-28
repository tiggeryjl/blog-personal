-- Existing database migration for generalized notification targets.
-- Back up the database before running this script. Run it exactly once.

USE `blog`;

ALTER TABLE `sys_notice`
    CHANGE COLUMN `article_title` `target_title` VARCHAR(255) NOT NULL COMMENT '跳转目标标题或摘要',
    CHANGE COLUMN `article_id` `target_id` BIGINT NOT NULL COMMENT '跳转目标ID';

ALTER TABLE `sys_notice`
    ADD COLUMN `target_type` VARCHAR(20) NULL COMMENT '跳转目标类型：article / daily / link'
        AFTER `action_text`;

UPDATE `sys_notice`
SET `target_type` = CASE
    WHEN `type` = 'link' THEN 'link'
    ELSE 'article'
END
WHERE `target_type` IS NULL OR `target_type` = '';

ALTER TABLE `sys_notice`
    MODIFY COLUMN `target_type` VARCHAR(20) NOT NULL COMMENT '跳转目标类型：article / daily / link'
        AFTER `action_text`,
    MODIFY COLUMN `target_title` VARCHAR(255) NOT NULL COMMENT '跳转目标标题或摘要'
        AFTER `target_type`,
    MODIFY COLUMN `target_id` BIGINT NOT NULL COMMENT '跳转目标ID'
        AFTER `target_title`,
    ADD INDEX `idx_target_type_id` (`target_type`, `target_id`);

-- user_like already uses target_type=0/1/2 for article/daily/comment.
-- No column migration is required for daily likes.

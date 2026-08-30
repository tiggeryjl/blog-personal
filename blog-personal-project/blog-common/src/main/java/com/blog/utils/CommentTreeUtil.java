package com.blog.utils;

import com.blog.pojo.vo.CommentVo;
import java.util.*;
import java.util.stream.Collectors;

public class CommentTreeUtil {

    /**
     * 构建平铺分组结构
     * @param flatList
     * @return
     */
    public static List<CommentVo> buildFlatReplyTree(List<CommentVo> flatList) {
        if (flatList == null || flatList.isEmpty()) {
            return new ArrayList<>();
        }

        // 查找父节点
        Map<Long, CommentVo> commentMap = flatList.stream()
                .collect(Collectors.toMap(CommentVo::getId, vo -> vo));

        List<CommentVo> rootList = new ArrayList<>();

        for (CommentVo vo : flatList) {

            if (vo.getReplies() == null) {
                vo.setReplies(new ArrayList<>());
            }

            if (vo.getParentId() == 0) {
                rootList.add(vo);
            } else {
                Long rootId = findTopRootId(vo.getParentId(), commentMap);
                if (rootId != null && commentMap.containsKey(rootId)) {
                    CommentVo rootComment = commentMap.get(rootId);
                    rootComment.getReplies().add(vo);
                }
            }
        }
        return rootList;
    }


    /**
     * 循环向上递归查找最顶层根评论(parentId=0)
     * @param startParentId
     * @param commentMap
     * @return
     */
    private static Long findTopRootId(Long startParentId, Map<Long, CommentVo> commentMap) {
        Long pid = startParentId;

        while (true) {
            CommentVo parent = commentMap.get(pid);
            // 父评论已不存在，终止
            if (parent == null) {
                return null;
            }
            // 找到主楼节点
            if (parent.getParentId() == 0) {
                return parent.getId();
            }
            // 继续向上追溯
            pid = parent.getParentId();
        }
    }
}


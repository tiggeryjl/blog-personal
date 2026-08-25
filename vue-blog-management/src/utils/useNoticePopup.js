import { ElNotification, ElButton } from 'element-plus';
import { createVNode } from 'vue';
import { useRouter } from 'vue-router';

export function useNoticePopup() {
  const router = useRouter();
  const queue = [];
  let processing = false;

  // 不同类型通知的标题颜色：评论=蓝、点赞=橙、友链=紫
  const TYPE_COLOR = {
    comment: '#409EFF',
    like: '#E6A23C',
    link: '#7C4DFF',
  };

  function buildVNode(item, close) {
    const isComment = item.type === 'comment';
    const isLink = item.type === 'link';
    const isLinkUrge = isLink && (item.actionText || '').includes('催促');
    const color = TYPE_COLOR[item.type] || TYPE_COLOR.like;
    // 正文动作文案：友链分为“申请友链”和“催促审核”两种
    const middleText = isLink ? (isLinkUrge ? ' 催促审核了友链：' : ' 申请了友链：') : ` ${item.actionText}了文章：`;
    return createVNode('div', { class: 'notice-body' }, [
      createVNode(
        'div',
        {
          style: `font-size:16px;font-weight:bold;color:${color};margin-bottom:8px;border-bottom:1px solid #eee;padding-bottom:6px;`,
        },
        item.title
      ),
      createVNode('div', { style: 'font-size:14px;color:#606266;line-height:1.6;word-break:break-all;' }, [
        createVNode('span', { style: 'font-weight:bold;color:#303133;' }, item.operatorName),
        createVNode('span', null, middleText),
        createVNode('span', { style: 'color:#409EFF;' }, item.articleTitle),
      ]),
      (isComment || isLink) && item.content
        ? createVNode(
            'div',
            {
              style:
                'margin-top:8px;padding:8px 10px;background:#f5f7fa;border-radius:4px;font-size:13px;color:#606266;line-height:1.5;word-break:break-all;',
            },
            `${isComment ? '评论内容' : '申请信息'}: ${item.content}`
          )
        : null,
      createVNode(
        'div',
        {
          style:
            'display:flex;justify-content:space-between;align-items:center;margin-top:10px;padding-top:6px;border-top:1px solid #ebeef5;',
        },
        [
          createVNode('span', { style: 'font-size:12px;color:#909399;' }, item.actionText + '时间:' + item.createTime),
          item.articleId
            ? createVNode(
                ElButton,
                {
                  size: 'small',
                  type: 'primary',
                  plain: true,
                  onClick: () => {
                    router.push(isLink ? '/linkInfo' : `/articleDetail?id=${item.articleId}`);
                    close();
                  },
                },
                () => (isLink && isLinkUrge ? '去审核' : '查看详情')
              )
            : null,
        ]
      ),
    ]);
  }

  const consume = () => {
    if (processing || queue.length === 0) return;
    processing = true;
    const item = queue.shift();
    let instance = null;
    instance = ElNotification({
      message: buildVNode(item, () => instance?.close()),
      duration: 7000,
      customClass: 'notice-popup',
      progress: {
        color: [
          { color: '#ff3e3e', percentage: 20 },
          { color: '#e6a23c', percentage: 40 },
          { color: '#5cb873', percentage: 60 },
          { color: '#02da99', percentage: 80 },
          { color: '#0279da', percentage: 100 },
        ],
      },
    });
    setTimeout(() => {
      processing = false;
      consume();
    }, 750);
  };

  const push = (noticeItem) => {
    queue.push(noticeItem);
    consume();
  };
  return { push };
}

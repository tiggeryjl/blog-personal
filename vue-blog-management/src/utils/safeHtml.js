import { FilterXSS, whiteList } from 'xss';

const options = {
  whiteList: {
    ...whiteList,
    figure: [],
    figcaption: [],
    video: ['src', 'poster', 'controls', 'width', 'height'],
    source: ['src', 'type'],
    span: ['class'],
    div: ['class'],
    p: [],
    img: ['src', 'alt', 'title', 'width', 'height'],
    a: ['href', 'target', 'rel'],
    h1: [],
    h2: [],
    h3: [],
    h4: [],
    h5: [],
    h6: [],
    ul: [],
    ol: [],
    li: [],
    strong: [],
    em: [],
    u: [],
    s: [],
    br: [],
    hr: [],
    blockquote: [],
    code: [],
    pre: [],
    table: [],
    thead: [],
    tbody: [],
    tr: [],
    td: [],
    th: [],
  },
  stripIgnoreTag: true, // 不在白名单的标签直接删掉（保留文字）
  stripIgnoreTagBody: ['script', 'style'], // script/style 连内容一起删
  allowCommentTag: false,
  onTagAttr(tag, name, value) {
    // 二次兜底：任何 href/src 出现 javascript:/vbscript:/data:text 一律拒绝
    if ((name === 'href' || name === 'src') && /^\s*(javascript|vbscript|data:text)/i.test(value)) {
      return '';
    }
    return undefined;
  },
};

const myXSS = new FilterXSS(options);

export const safeHtml = (html) => (html == null ? '' : myXSS.process(String(html)));

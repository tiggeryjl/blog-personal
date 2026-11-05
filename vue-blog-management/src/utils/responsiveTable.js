const RESPONSIVE_TABLE_CLASS = 'responsive-data-table';
const MOBILE_BREAKPOINT = 768;

const normalizeText = (value) =>
  String(value || '')
    .replace(/\s+/g, ' ')
    .trim();

const MEDIA_FIELD_PATTERN = /^(头像|封面|图片)$/;
const PRIMARY_FIELD_PATTERN = /(标题|内容|账号信息|网站名称|歌曲|角色名称|菜单名称|评论人|名称|文章|日常)$/;
const DETAIL_FIELD_PATTERN = /(简介|描述|来源|链接|权限标识|路由地址|组件路径|设备信息|举报原因|备注|附件)$/;

const getFieldKind = (label) => {
  if (!label) return 'empty';
  if (label === '选择') return 'selection';
  if (label === '操作') return 'action';
  if (MEDIA_FIELD_PATTERN.test(label)) return 'media';
  if (DETAIL_FIELD_PATTERN.test(label)) return 'detail';
  if (PRIMARY_FIELD_PATTERN.test(label)) return 'primary';
  return 'meta';
};

const getHeaderLabels = (table) => {
  const headers = table.querySelectorAll('.el-table__header-wrapper thead th');
  return Array.from(headers).map((header, index) => {
    const isSelectionHeader =
      header.classList.contains('el-table-column--selection') || Boolean(header.querySelector('.el-checkbox'));
    header.classList.toggle('responsive-selection-header', isSelectionHeader);
    if (isSelectionHeader) {
      return '选择';
    }
    if (header.classList.contains('el-table-column--expand') || header.querySelector('.el-table__expand-icon')) {
      header.classList.add('responsive-expand-header');
      return '展开';
    }
    const label = normalizeText(header.querySelector('.cell')?.textContent);
    return label || `字段${index + 1}`;
  });
};

const annotateTable = (table) => {
  const labels = getHeaderLabels(table);
  if (!labels.length) return;

  const rows = table.querySelectorAll('.el-table__body-wrapper tbody tr');
  rows.forEach((row) => {
    const cells = row.querySelectorAll(':scope > td');
    let hasMedia = false;
    cells.forEach((cell, index) => {
      const isSelectionCell =
        cell.classList.contains('el-table-column--selection') || Boolean(cell.querySelector('.el-checkbox'));
      const label = isSelectionCell ? '选择' : labels[index];
      if (label) {
        cell.dataset.label = label;
        let kind = getFieldKind(label);
        if (
          kind === 'media' &&
          !cell.querySelector(
            'img, .el-image, .el-carousel, .daily-video-card, .daily-cover-area'
          )
        ) {
          kind = 'meta';
        }
        cell.dataset.fieldKind = kind;
        if (kind === 'media') hasMedia = true;
      } else {
        delete cell.dataset.label;
        delete cell.dataset.fieldKind;
      }
    });
    row.classList.toggle('responsive-table-row-has-media', hasMedia);
  });

  table.classList.add(RESPONSIVE_TABLE_CLASS);
};

/**
 * 管理端表格在移动端自动转为卡片布局。
 */
export const initResponsiveTables = () => {
  let frameId = 0;

  const scan = () => {
    frameId = 0;
    document.querySelectorAll('.el-table').forEach(annotateTable);
  };

  const scheduleScan = () => {
    if (frameId) return;
    frameId = window.requestAnimationFrame(scan);
  };

  scan();

  const observer = new MutationObserver((mutations) => {
    const hasRelevantMutation = mutations.some(
      (mutation) =>
        mutation.type === 'childList' &&
        (mutation.target.closest?.('.el-table') ||
          Array.from(mutation.addedNodes).some(
            (node) =>
              node.nodeType === Node.ELEMENT_NODE && (node.matches?.('.el-table') || node.querySelector?.('.el-table'))
          ))
    );
    if (hasRelevantMutation) scheduleScan();
  });

  observer.observe(document.body, {
    childList: true,
    subtree: true,
  });

  const handleResize = () => {
    if (window.innerWidth <= MOBILE_BREAKPOINT) {
      scheduleScan();
    }
  };
  window.addEventListener('resize', handleResize, { passive: true });

  return () => {
    observer.disconnect();
    window.removeEventListener('resize', handleResize);
    if (frameId) {
      window.cancelAnimationFrame(frameId);
    }
  };
};

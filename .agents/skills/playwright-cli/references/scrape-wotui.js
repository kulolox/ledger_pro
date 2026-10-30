// Scrape all Wot UI component APIs
// Usage: playwright-cli run-code --filename=scrape-wotui.js
// Output: saves JSON to .playwright-cli/wotui-components.json

const fs = require('fs');
const BASE = 'https://wot-ui.cn/component/';

const PAGES = [
  'button', 'icon', 'text', 'layout', 'cell', 'fab', 'transition', 'resize',
  'config-provider', 'root-portal',
  'navbar', 'tabbar', 'tabs', 'segmented', 'sidebar', 'pagination', 'index-bar',
  'backtop', 'tour',
  'form', 'input', 'textarea', 'password-input', 'keyboard', 'input-number',
  'search', 'checkbox', 'radio', 'switch', 'rate', 'slider',
  'picker', 'picker-view', 'select-picker', 'cascader', 'calendar', 'calendar-view',
  'datetime-picker', 'datetime-picker-view',
  'upload', 'signature', 'slide-verify',
  'popup', 'overlay', 'dialog', 'action-sheet', 'drop-menu', 'popover', 'tooltip',
  'floating-panel',
  'loading', 'progress', 'circle', 'toast', 'notify', 'notice-bar',
  'swipe-action', 'sort-button', 'empty', 'count-down', 'count-to',
  'avatar', 'badge', 'tag', 'card', 'divider', 'gap', 'grid', 'collapse',
  'steps', 'sticky', 'skeleton', 'loadmore',
  'img', 'image-preview', 'video-preview', 'img-cropper', 'swiper',
  'table', 'watermark', 'curtain',
  'use-upload', 'use-count-down', 'use-toast', 'use-dialog',
  'use-image-preview', 'use-video-preview', 'use-config-provider'
];

module.exports = async (page) => {
  const results = {};
  const errors = [];

  for (const name of PAGES) {
    try {
      await page.goto(`${BASE}${name}.html`, { waitUntil: 'networkidle', timeout: 20000 });
      await page.waitForTimeout(300);
      const tables = await page.evaluate(() => {
        const tables = document.querySelectorAll('table');
        return Array.from(tables).map(t => {
          let caption = '';
          let el = t.previousElementSibling;
          while (el) {
            if (el.tagName === 'H2' || el.tagName === 'H3' || el.tagName === 'H4') {
              caption = el.textContent.trim();
              break;
            }
            el = el.previousElementSibling;
          }
          const headers = Array.from(t.querySelectorAll('thead th')).map(h => h.textContent.trim());
          const rows = Array.from(t.querySelectorAll('tbody tr')).map(r =>
            Array.from(r.querySelectorAll('td')).map(d => d.textContent.trim().replace(/\s+/g, ' '))
          );
          return {caption, headers, rows};
        });
      });
      results[name] = tables;
      console.log(`OK [${PAGES.indexOf(name)+1}/${PAGES.length}]: ${name}`);
    } catch (e) {
      errors.push(name);
      results[name] = { error: e.message };
      console.log(`FAIL: ${name} - ${e.message.slice(0, 60)}`);
    }
  }

  const output = { results, errors };
  const outPath = require('path').join(__dirname, '..', 'wotui-components.json');
  fs.writeFileSync(outPath, JSON.stringify(output, null, 2));
  console.log(`\nDone! ${PAGES.length - errors.length}/${PAGES.length} OK, ${errors.length} errors`);
  console.log(`Saved to: ${outPath}`);
  if (errors.length) console.log('Errors:', errors.join(', '));
  return `Scraped ${PAGES.length - errors.length}/${PAGES.length} components`;
};

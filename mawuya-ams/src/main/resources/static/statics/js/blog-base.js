/* ============================================================
 *  blog-base.js —— AMS 前台基础脚本
 * ------------------------------------------------------------
 *  替代 NexT 旧版 (utils5174 / motion5174 / bootstrap5174 / scrollspy5174)
 *  + 第三方依赖 fastclick / jquery_lazyload / velocity / fancybox。
 *
 *  设计原则：
 *    - 零依赖：纯原生 ES5+，不再依赖 jQuery / velocity / fancybox 等
 *    - IIFE 隔离作用域，挂极少量全局符号（无）
 *    - DOM 操作通过 querySelector，事件用原生 addEventListener
 *    - 图片懒加载已改用 HTML5 loading="lazy"，无需 JS
 *    - 移动端汉堡菜单已在 header.html 内联（保留原样不动）
 *
 *  覆盖能力（与旧版功能对齐）：
 *    1) 菜单当前页高亮       (原 NexT utils.addActiveClassToMenuItem)
 *    2) 图片点击放大          (原 NexT utils.wrapImageWithFancyBox + fancybox)
 *    3) 返回顶部按钮          (原 bootstrap5174 .back-to-top click)
 *    4) 文章 use-motion 入场  (改为 CSS keyframes，本文件触发 .is-motion-ready 即可)
 *    5) 嵌入视频响应式包裹    (原 utils.embeddedVideoTransformer)
 *
 *  @author 钟启辉
 * ============================================================ */

(function () {
    'use strict';

    /* ---------- 工具 ---------- */
    function $$(sel, ctx) { return Array.prototype.slice.call((ctx || document).querySelectorAll(sel)); }
    function ready(fn) {
        if (document.readyState !== 'loading') { fn(); }
        else { document.addEventListener('DOMContentLoaded', fn); }
    }

    /* ============================================================
     * 1) 菜单当前页高亮
     *    原 NexT: NexT.utils.addActiveClassToMenuItem()
     *    实现：把 location.pathname 与每个 .menu-item a 的 href 对比
     *         严格相等 → 加 menu-item-active；首页 '/' 单独处理
     * ============================================================ */
    function highlightActiveMenu() {
        var path = window.location.pathname || '/';
        // 去掉末尾斜杠（除根路径本身）
        if (path !== '/' && path.endsWith('/')) {
            path = path.substring(0, path.length - 1);
        }
        $$('.menu-item a').forEach(function (a) {
            var href = a.getAttribute('href') || '';
            // 提取 href 的路径部分（忽略 host/query）
            var hrefPath;
            try {
                hrefPath = new URL(href, window.location.href).pathname || '/';
            } catch (e) { hrefPath = href; }
            if (hrefPath !== '/' && hrefPath.endsWith('/')) {
                hrefPath = hrefPath.substring(0, hrefPath.length - 1);
            }
            if (hrefPath === path) {
                var li = a.parentElement;
                if (li) { li.classList.add('menu-item-active'); }
            }
        });
    }

    /* ============================================================
     * 2) 图片点击放大（原生 lightbox）
     *    原 NexT 用 jQuery + fancybox 实现，体积庞大且老旧。
     *    这里用原生 <dialog> + 简单 fade/zoom 动画自实现，约 50 行：
     *      - 仅对 .posts-expand .post-body img 生效（文章正文）
     *      - 跳过已被 <a> 包裹的图片（避免覆盖正常超链接）
     *      - 跳过缩略图 .post-thumb-fixed
     *      - dialog 不可滚动，按 ESC / 点击空白处关闭
     * ============================================================ */
    function setupImageLightbox() {
        var imgs = $$('.posts-expand .post-body img, #articleContentDiv img').filter(function (img) {
            // 显式白名单：首页封面图 .post-thumb-zoom 一定要绑 lightbox（覆盖所有跳过规则）
            if (img.classList.contains('post-thumb-zoom')) { return true; }
            // 1) 已被 <a> 包裹的图片由浏览器原生处理（跳链接），不绑 lightbox
            if (img.closest('a')) { return false; }
            // 2) 其它 .post-thumb-fixed（未来可能有非 zoom 场景）默认跳过
            if (img.classList.contains('post-thumb-fixed')) { return false; }
            if (img.closest('.post-thumb-wrap')) { return false; }
            // 3) 小于 80px 的装饰图标
            if (img.naturalWidth && img.naturalWidth < 80) { return false; }
            return true;
        });
        if (imgs.length === 0) { return; }

        // 单例 overlay，惰性创建
        var overlay = null, overlayImg = null, overlayCaption = null;
        function ensureOverlay() {
            if (overlay) { return; }
            overlay = document.createElement('div');
            overlay.className = 'blog-lightbox';
            overlay.setAttribute('role', 'dialog');
            overlay.setAttribute('aria-modal', 'true');
            overlay.innerHTML =
                '<button type="button" class="blog-lightbox-close" aria-label="关闭">&times;</button>' +
                '<img class="blog-lightbox-img" alt=""/>' +
                '<div class="blog-lightbox-caption"></div>';
            document.body.appendChild(overlay);
            overlayImg = overlay.querySelector('.blog-lightbox-img');
            overlayCaption = overlay.querySelector('.blog-lightbox-caption');

            overlay.addEventListener('click', function (e) {
                // 点击空白（非图片本身）或关闭按钮 → 关闭
                if (e.target === overlay || e.target.classList.contains('blog-lightbox-close')) {
                    closeLightbox();
                }
            });
            document.addEventListener('keydown', function (e) {
                if (overlay && overlay.classList.contains('is-open') && e.key === 'Escape') {
                    closeLightbox();
                }
            });
        }
        function openLightbox(src, caption) {
            ensureOverlay();
            overlayImg.src = src;
            overlayCaption.textContent = caption || '';
            overlayCaption.style.display = caption ? 'block' : 'none';
            overlay.classList.add('is-open');
            document.documentElement.classList.add('blog-lightbox-lock');
        }
        function closeLightbox() {
            if (!overlay) { return; }
            overlay.classList.remove('is-open');
            document.documentElement.classList.remove('blog-lightbox-lock');
            // 清掉 src 释放内存
            setTimeout(function () { overlayImg.src = ''; }, 200);
        }

        imgs.forEach(function (img) {
            img.classList.add('blog-lightbox-trigger');
            img.style.cursor = 'zoom-in';
            img.addEventListener('click', function () {
                openLightbox(img.currentSrc || img.src, img.getAttribute('title') || img.getAttribute('alt') || '');
            });
        });
    }

    /* ============================================================
     * 3) 返回顶部按钮
     *    原 bootstrap5174: $('.back-to-top').on('click', velocity('scroll'))
     *    改用原生 scrollTo({behavior:'smooth'})，并按滚动距离决定按钮显示/隐藏
     * ============================================================ */
    function setupBackToTop() {
        var btn = document.querySelector('.back-to-top');
        if (!btn) { return; }

        function onScroll() {
            if (window.scrollY > 400) { btn.classList.add('is-visible'); }
            else { btn.classList.remove('is-visible'); }
        }
        window.addEventListener('scroll', onScroll, { passive: true });
        onScroll();

        btn.addEventListener('click', function () {
            // prefers-reduced-motion 友好
            var prefersReduced = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
            window.scrollTo({
                top: 0,
                behavior: prefersReduced ? 'auto' : 'smooth'
            });
        });
        // 键盘可达
        btn.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                btn.click();
            }
        });
    }

    /* ============================================================
     * 4) 入场动画就绪信号
     *    原 NexT 用 velocity 链式动画给 logo / menu / postList / sidebar 做淡入。
     *    现在改用纯 CSS keyframes（见 blog-extra.css 的 .use-motion 规则）。
     *    本函数只负责在 DOM ready 后给 <html> 加 .is-motion-ready，
     *    CSS 据此触发 fade-in-up 动画，避免 SSR 首屏闪动。
     * ============================================================ */
    function markMotionReady() {
        document.documentElement.classList.add('is-motion-ready');
    }

    /* ============================================================
     * 5) 嵌入视频响应式包裹
     *    原 NexT utils.embeddedVideoTransformer：
     *      给 youtube/vimeo/youku/网易云音乐/tudou 的 iframe 包一层 .fluid-vids
     *      padding-top: ratio% 实现等比宽高，自适应容器宽度
     * ============================================================ */
    function transformEmbeddedVideo() {
        var supported = /www\.youtube\.com|player\.vimeo\.com|player\.youku\.com|music\.163\.com|www\.tudou\.com/;
        $$('iframe').forEach(function (iframe) {
            if (!iframe.src || !supported.test(iframe.src)) { return; }
            if (iframe.closest('.fluid-vids')) { return; }
            var w = iframe.width  ? parseInt(iframe.width, 10)  : iframe.offsetWidth;
            var h = iframe.height ? parseInt(iframe.height, 10) : iframe.offsetHeight;
            if (!w || !h) { return; }
            var ratio = (h / w) * 100;
            if (iframe.src.indexOf('music.163.com') >= 0) { ratio += 10; }
            iframe.style.width = '100%';
            iframe.style.height = '100%';
            iframe.style.position = 'absolute';
            iframe.style.top = '0';
            iframe.style.left = '0';
            var wrap = document.createElement('div');
            wrap.className = 'fluid-vids';
            wrap.style.width = '100%';
            wrap.style.position = 'relative';
            wrap.style.paddingTop = ratio + '%';
            iframe.parentNode.insertBefore(wrap, iframe);
            wrap.appendChild(iframe);
        });
    }

    /* ============================================================
     * 6) 侧边栏切换（默认显示，悬浮按钮触发收起/展开）
     *    - 检测页面里是否存在 .sidebar 与 .main-inner，缺一即不渲染按钮
     *    - 按钮固定在视口右下，比 .back-to-top 高 50px
     *    - 用户偏好持久化：localStorage 'sidebar-hidden' = '1'|'0'（默认 '0' = 显示）
     *    - 移动端 (≤767px) 已在 CSS 用 !important 强制隐藏 sidebar/toggle，
     *      此处不必额外判断屏幕宽度
     * ============================================================ */
    function setupSidebarToggle() {
        var mainInner = document.querySelector('.main-inner');
        var sidebar   = document.querySelector('.sidebar');
        if (!mainInner || !sidebar) { return; }

        // 复用模板里的 .sidebar-toggle 占位若已存在，则不重复创建
        var btn = document.querySelector('.sidebar-toggle');
        if (!btn) {
            btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'sidebar-toggle';
            document.body.appendChild(btn);
        }
        // 标准化按钮内部 DOM（移除 NexT 旧的三条线 markup，让 CSS ::before/::after 接管）
        btn.innerHTML = '<i aria-hidden="true"></i>';
        btn.setAttribute('role', 'button');
        btn.setAttribute('aria-label', '切换侧边栏');
        btn.setAttribute('tabindex', '0');

        function apply(hidden) {
            if (hidden) {
                mainInner.classList.add('sidebar-hidden');
                btn.classList.add('is-open');         /* 收起态显示 "×"，提示"点击恢复" */
                btn.setAttribute('aria-expanded', 'false');
            } else {
                mainInner.classList.remove('sidebar-hidden');
                btn.classList.remove('is-open');
                btn.setAttribute('aria-expanded', 'true');
            }
        }

        // 初始态：用户上次的偏好；默认显示（hidden=false）
        var saved = null;
        try { saved = localStorage.getItem('sidebar-hidden'); } catch (e) {}
        apply(saved === '1');

        function toggle() {
            var next = !mainInner.classList.contains('sidebar-hidden');
            apply(next);
            try { localStorage.setItem('sidebar-hidden', next ? '1' : '0'); } catch (e) {}
        }
        btn.addEventListener('click', toggle);
        btn.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                toggle();
            }
        });
    }

    /* ---------- 启动 ---------- */
    ready(function () {
        markMotionReady();
        highlightActiveMenu();
        setupBackToTop();
        setupSidebarToggle();
        setupImageLightbox();
        transformEmbeddedVideo();
    });
})();

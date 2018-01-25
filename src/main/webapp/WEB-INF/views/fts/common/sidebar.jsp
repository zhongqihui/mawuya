<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>

<div class="sidebar-toggle">
    <div class="sidebar-toggle-line-wrap">
        <span class="sidebar-toggle-line sidebar-toggle-line-first"></span>
        <span class="sidebar-toggle-line sidebar-toggle-line-middle"></span>
        <span class="sidebar-toggle-line sidebar-toggle-line-last"></span>
    </div>
</div>

<aside id="sidebar" class="sidebar">
    <div class="sidebar-inner">
        <section class="site-overview sidebar-panel  sidebar-panel-active ">
            <div class="site-author motion-element">
                <img class="site-author-image" src="#" alt="Zhongqihui"/>
                <p class="site-author-name">Zhongqihui</p>
                <p class="site-description motion-element">一个疯子的世界，<br/>只有疯子才能生存下去的世界。</p>
            </div>
            <nav class="site-state motion-element">
                <div class="site-state-item site-state-posts">
                    <a href="${pageContext.request.contextPath}/index">
                        <span class="site-state-item-count">70</span>
                        <span class="site-state-item-name">日志</span>
                    </a>
                </div>

                <div class="site-state-item site-state-categories">
                    <a href="#">
                        <span class="site-state-item-count">11</span>
                        <span class="site-state-item-name">分类</span>
                    </a>
                </div>

                <div class="site-state-item site-state-tags">
                    <a href="../tags/index.html">
                        <span class="site-state-item-count">65</span>
                        <span class="site-state-item-name">标签</span>
                    </a>
                </div>

            </nav>

            <div class="links-of-author motion-element"></div>

        </section>

    </div>
</aside>

</body>
</html>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html class="theme-next mist use-motion">
<head>
    <title>${article.articleTitle} | Zqh's Blog </title>
    <%@include file="common/css-head.jsp"%>

</head>
<body lang="zh-Hans">
<div class="container one-collumn sidebar-position-left page-post-detail">
    <div class="headband"></div>
    <%@ include file="common/header.jsp" %>

    <main id="main" class="main">
        <div class="main-inner">
            <div class="content-wrap">
                <div id="content" class="content">
                    <div id="posts" class="posts-expand">
                        <article class="post post-type-normal">
                            <%--该文章信息--%>
                            <header class="post-header">
                                <h1 class="post-title">${article.articleTitle}</h1>
                                <div class="post-meta">
                                    <span class="post-time">
                                        <span class="post-meta-item-icon"><i class="fa fa-calendar-o"></i></span>
                                        <span class="post-meta-item-text">发表于 </span>
                                        <span>
                                            <c:if test="${fn:length(article.insertTime) > 10}">
                                                <c:out value="${fn:substring(article.insertTime, 0, 10)}"/>
                                            </c:if>
                                        </span>
                                    </span>

                                    <span class="post-category">&nbsp; | &nbsp;
                                        <span class="post-meta-item-icon"><i class="fa fa-folder-o"></i></span>
                                        <span class="post-meta-item-text">分类于</span>
                                        <span><a href="${pageContext.request.contextPath}/categories/${category.sn}"><span>${category.categoryName}</span></a></span>
                                    </span>

                                    <span class="post-category">&nbsp; | &nbsp;
                                        <span><a href="#"><span>${article.reviewNum}条评论</span></a></span>
                                    </span>

                                    <span>&nbsp; | &nbsp;
                                        <span class="post-meta-item-text">阅读次数 ${article.readNum}</span>
                                    </span>
                                </div>
                            </header>

                            <div class="post-body">
                                <div id="articleContentDiv">
                                    <textarea style="display: none">${article.articleContent}</textarea>
                                </div>
                            </div>

                            <div>
                                <div class="copyright-txt">
                                    <a href="#"><i class="fa fa-copyright"></i>著作权归作者所有</a>
                                </div>
                            </div>

                            <footer class="post-footer">
                                <div class="post-tags">
                                    <a href="${pageContext.request.contextPath}/categories/${category.sn}" rel="tag">#${category.categoryName}</a>
                                </div>

                                <div class="post-nav">
                                    <div class="post-nav-next post-nav-item">
                                        <c:if test="${prev != null}">
                                        <a href="${pageContext.request.contextPath}/${prev.sn}"><i class="fa fa-chevron-left"></i> ${prev.articleTitle}</a>
                                        </c:if>
                                    </div>

                                    <div class="post-nav-prev post-nav-item">
                                        <c:if test="${next != null}">
                                            <a href="${pageContext.request.contextPath}/${next.sn}"><i class="fa fa-chevron-right"></i> ${next.articleTitle}</a>
                                        </c:if>
                                    </div>
                                </div>
                            </footer>
                        </article>
                    </div>
                </div>
            </div>

            <%--todo 这里展示评论--%>
            <div class="comments" id="comments">

            </div>

            <%--可以从下面调到最上面的特效--%>
            <aside id="sidebar" class="sidebar">
                <div class="sidebar-inner">
                    <section class="post-toc-wrap motion-element sidebar-panel sidebar-panel-active">
                        <div class="post-toc-indicator-bottom post-toc-indicator">
                            <i class="fa fa-angle-double-down"></i>
                        </div>
                    </section>
                </div>
            </aside>

        </div>
    </main>
</div>


<%@ include file="common/footer.jsp" %>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/editor.md/lib/marked.min.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/editor.md/lib/prettify.min.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/editor.md/editormd.min.js"></script>

<script type="text/javascript">
    $(function () {
        editormd.markdownToHTML("articleContentDiv",{
            htmlDecode: "style,script,iframe",
            emoji: true,
            taskList: true,
            tex: true, // 默认不解析
            //flowChart: true, // 默认不解析
            //sequenceDiagram: true, // 默认不解析
            codeFold: true
        });
    });
</script>

</body>
</html>

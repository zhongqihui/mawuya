<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>

<html class="theme-next mist use-motion">


<head>

</head>

<body lang="zh-Hans">


<div class="container one-collumn sidebar-position-left page-home">
    <div class="headband"></div>
    <%@ include file="header.jsp" %>

    <main id="main" class="main">
        <div class="main-inner">
            <div class="content-wrap">
                <div id="content" class="content">
                    <section id="posts" class="posts-expand">

                        <c:forEach items="${articleList}" var="article" varStatus="status">
                            <article class="post post-type-normal">
                                <header class="post-header">
                                    <h1 class="post-title">
                                        <a class="post-title-link" href="${pageContext.request.contextPath}/${article.sn}">${article.ATitle}</a>
                                    </h1>

                                    <div class="post-meta">
                                        <span class="post-time">
                                            <span class="post-meta-item-icon"><i class="fa fa-calendar-o"></i></span>
                                            <span class="post-meta-item-text">发表于</span>
                                            <span>${article.insertTime}</span>
                                        </span>

                                        <span class="post-category">&nbsp; | &nbsp;
                                            <span class="post-meta-item-icon"><i class="fa fa-folder-o"></i></span>
                                            <span class="post-meta-item-text">分类于</span>
                                            <span>
                                              <a href="#"><span>职场生涯</span></a>
                                            </span>
                                        </span>

                                        <span>&nbsp; | &nbsp;</span>
                                        <span>&nbsp; | &nbsp;
                                           <span class="post-meta-item-icon"><i class="fa fa-eye"></i></span>
                                           <span class="post-meta-item-text">阅读次数 ${article.readNum}</span>
                                        </span>
                                    </div>
                                </header>

                                <div class="post-body">
                                    <p>${article.ASummary}</p>
                                        <p><img src="${pageContext.request.contextPath}/statics/images/gallery/img6.jpg" alt=""></p>
                                    <div class="post-more-link text-center">
                                        <a class="btn" href="${pageContext.request.contextPath}/${article.sn}" rel="contents">
                                            阅读全文 &raquo;
                                        </a>
                                    </div>
                                </div>
                                <div></div>
                                <footer class="post-footer">
                                    <div class="post-eof"></div>
                                </footer>
                            </article>
                        </c:forEach>
                    </section>

                    <nav class="pagination">
                        <span class="page-number current">1</span>
                        <a class="page-number" href="#">2</a>
                        <span class="space">&hellip;</span><a class="page-number" href="#">7</a>
                        <a class="extend next" rel="next" href="#">
                            <i class="fa fa-angle-right"></i>
                        </a>
                    </nav>
                </div>
            </div>

        </div>
    </main>

    <%@ include file="footer.jsp" %>
</div>
</body>

</html>

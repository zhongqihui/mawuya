<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>

<html class="theme-next mist use-motion">

<meta http-equiv="content-type" content="text/html;charset=utf-8"/><!-- /Added by HTTrack -->
<head>
    <meta charset="UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1"/>
    <meta http-equiv="Cache-Control" content="no-transform"/>
    <meta http-equiv="Cache-Control" content="no-siteapp"/>
    <meta name="author" content="Zqh"/>
    <meta name="keywords" content="Java"/>
    <meta name="description" content="这是一个疯子的世界，只有疯子才能生存下去。">

    <link rel="shortcut icon" href="${pageContext.request.contextPath}/statics/images/favicon.jpg"/>
    <link href="${pageContext.request.contextPath}/statics/plugins/vendors/fancybox/source/jquery.fancybox8cbb.css?v=2.1.5"
          rel="stylesheet" type="text/css"/>
    <link href="${pageContext.request.contextPath}/statics/plugins/vendors/font-awesome/css/font-awesome.min93e3.css?v=4.4.0"
          rel="stylesheet" type="text/css"/>
    <link href="${pageContext.request.contextPath}/statics/css/main5174.css?v=0.5.0" rel="stylesheet" type="text/css"/>

    <script type="text/javascript" id="hexo.configuration">
        var NexT = window.NexT || {};
        var CONFIG = {
            scheme: 'Mist',
            sidebar: {"position": "left", "display": "post"},
            fancybox: true,
            motion: true
        };
    </script>
    <title> Zqh's Blog </title>
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
                                        <a class="post-title-link" href="#">${article.ATitle}</a>
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
                                        <a class="btn" href="#" rel="contents">
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


<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/vendors/jquery/index62ba.js?v=2.1.3"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/vendors/fastclick/lib/fastclick.minc924.js?v=1.0.6"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/vendors/jquery_lazyload/jquery.lazyloada045.js?v=1.9.7"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/vendors/velocity/velocity.min.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/vendors/velocity/velocity.ui.min.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/vendors/fancybox/source/jquery.fancybox.pack.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/js/utils5174.js?v=0.5.0"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/js/motion5174.js?v=0.5.0"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/js/bootstrap5174.js?v=0.5.0"></script>

</body>

</html>

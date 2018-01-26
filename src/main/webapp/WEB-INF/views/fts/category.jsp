<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!doctype html>

<html class="theme-next mist use-motion">
<head>
    <title> Zqh's Blog </title>
</head>
<body lang="zh-Hans">
<div class="container one-collumn sidebar-position-left page-home">
    <div class="headband"></div>
    <%@ include file="common/header.jsp" %>

    <main id="main" class="main">
        <div class="main-inner">
            <div class="content-wrap">
                <div id="content" class="content">


                    <section id="posts" class="posts-collapse">
                        <div class="collection-title">
                            <h2>${category.CName}<small>分类</small></h2>
                        </div>

                        <c:forEach items="${category.arts}" var="item">
                            <article class="post post-type-normal">
                                <header class="post-header">
                                    <h1 class="post-title">
                                        <a class="post-title-link" href="">
                                            <span>${item.ATitle}</span>
                                        </a>
                                    </h1>

                                    <div class="post-meta">
                                        <time class="post-time" itemprop="dateCreated" datetime="2017-05-17T20:53:38+08:00" content="2017-05-17">
                                            2017-05-17
                                        </time>
                                    </div>

                                </header>
                            </article>
                        </c:forEach>

                    </section>
                </div>

            </div>

            <%@ include file="common/sidebar.jsp" %>

        </div>
    </main>


    <%@ include file="common/footer.jsp" %>
</div>
</body>

</html>
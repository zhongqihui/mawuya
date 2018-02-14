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

                    <section id="posts" class="posts-collapse" style="border: 4px #f5f5f5;border-left-style: solid;">
                        <span class="archive-move-on"></span>
                        <span class="archive-page-counter">好! 目前共计 ${count} 篇日志。 继续努力。</span>

                        <c:forEach items="${map}" var="map">
                            <div class="collection-title">
                                <h2>${map.key}</h2>
                            </div>

                            <c:forEach items="${map.value}" var="v">
                                <article class="post post-type-normal">
                                    <header class="post-header">
                                        <h1 class="post-title">
                                            <a class="post-title-link"
                                               href="${pageContext.request.contextPath}/${v.sn}"><span>${v.articleTitle}</span></a>
                                        </h1>

                                        <div class="post-meta">
                                            <c:if test="${fn:length(v.insertTime) > 10}">
                                                <c:out value="${fn:substring(v.insertTime, 5, 10)}"/>
                                            </c:if>
                                        </div>

                                    </header>
                                </article>
                            </c:forEach>
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
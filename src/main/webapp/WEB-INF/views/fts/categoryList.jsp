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
                    <div id="posts" class="posts-expand">
                        <div class="category-all-page">
                            <div class="category-all-title">
                                目前共计 ${fn:length(categoryList)} 个分类
                            </div>

                            <div class="category-all">
                                <ul class="category-list">
                                    <c:forEach items="${categoryList}" var="item">
                                        <li class="category-list-item">
                                            <a class="category-list-link" href="${pageContext.request.contextPath}/categories/${item.sn}">${item.categoryName}</a>
                                            <span class="category-list-count">${item.artSize}</span>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </div>
                        </div>

                    </div>

                </div>

            </div>
            <%@ include file="common/sidebar.jsp" %>

        </div>
    </main>

    <%@ include file="common/footer.jsp" %>
</div>
</body>

</html>

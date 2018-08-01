<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!doctype html>

<html class="theme-next mist use-motion">
<head>
    <%@include file="common/css-head.jsp" %>
</head>
<body lang="zh-Hans">
<div class="container one-collumn sidebar-position-left page-home">
    <div class="headband"></div>
    <%@ include file="common/header.jsp" %>

    <main id="main" class="main">
        <div class="main-inner">
            <div class="content-wrap">
                <div id="content" class="content">
                    <section id="posts" class="posts-expand">

                        <c:forEach items="${page.lists}" var="article" varStatus="status">
                            <article class="post post-type-normal">
                                <header class="post-header">
                                    <h1 class="post-title">
                                        <a class="post-title-link"
                                           href="${pageContext.request.contextPath}/${article.sn}">${article.articleTitle}</a>
                                    </h1>

                                    <div class="post-meta">
                                        <span class="post-time">
                                            <span class="post-meta-item-icon"><i class="fa fa-calendar-o"></i></span>
                                            <span class="post-meta-item-text">发表于</span>
                                            <span>
                                                <c:if test="${fn:length(article.insertTime) > 10}">
                                                    <c:out value="${fn:substring(article.insertTime, 0, 10)}"/>
                                                </c:if>
                                            </span>
                                        </span>

                                        <span class="post-category">&nbsp; | &nbsp;
                                            <span class="post-meta-item-icon"><i class="fa fa-folder-o"></i></span>
                                            <span class="post-meta-item-text">分类于</span>
                                            <span>
                                              <a href="${pageContext.request.contextPath}/categories/${article.categorySn}">
                                                  <span>
                                                      <c:forEach items="${categoryList}" var="c">
                                                          <c:if test="${article.categorySn == c.sn}">
                                                              ${c.categoryName}
                                                          </c:if>
                                                      </c:forEach>
                                                  </span>
                                              </a>
                                            </span>
                                        </span>

                                        <span class="post-category">&nbsp; | &nbsp;
                                            <span class="post-meta-item-icon"><i class="fa fa-eye"></i></span>
                                            <span class="post-meta-item-text">评论次数 ${article.reviewNum}</span>
                                        </span>

                                        <span>&nbsp; | &nbsp;
                                           <span class="post-meta-item-icon"><i class="fa fa-eye"></i></span>
                                           <span class="post-meta-item-text">阅读次数 ${article.readNum}</span>
                                        </span>
                                    </div>
                                </header>

                                <div class="post-body">
                                    <p>${article.articleSummary}</p>
                                    <c:if test="${article.pictureUrl != null}">
                                        <p>
                                            <img src="${article.pictureUrl}" alt=""/>
                                        </p>
                                    </c:if>
                                    <div class="post-more-link text-center">
                                        <a class="btn" href="${pageContext.request.contextPath}/${article.sn}"
                                           rel="contents">
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

                    <%@ include file="common/page.jsp" %>
                </div>
            </div>

        </div>
    </main>

    <%@ include file="common/footer.jsp" %>
</div>
</body>

</html>

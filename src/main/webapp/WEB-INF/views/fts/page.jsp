<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<c:if test="${page.pageSize > 1}">
    <nav class="pagination">
        <c:if test="${page.pageSize <=5 }">
            <a class="extend prev" rel="prev" href="#"><i class="fa fa-angle-left"></i></a>
            <c:if test="${page.prev >= 1}">
                <a class="page-number" href="javascript:goURL('1')">1</a>
                <span class="space">&hellip;</span>
                <a class="page-number" href="javascript:goURL('${page.prev}')">${page.prev}</a>
            </c:if>

            <a class="page-number current" href="javascript:goURL('${page.curr}')">${page.curr}</a>
            <a class="page-number" href="javascript:goURL('${page.next}')">${page.next}</a>
            <span class="space">&hellip;</span>
            <a class="page-number" href="javascript:goURL('${page.pageSize}')">${page.pageSize}</a>
            <a class="extend next" rel="next" href="#"><i class="fa fa-angle-right"></i></a>
        </c:if>

        <c:if test="${page.pageSize > 5}">
            <a class="extend prev" rel="prev" href="#"><i class="fa fa-angle-left"></i></a>
            <a class="page-number current">1</a>
            <c:if test="${page.curr > 2}">
                <a class="page-number" href="javascript:goURL('${page.prev}')">${page.prev}</a>
            </c:if>
            <c:if test="${page.curr > 1}">
                <a class="page-number" href="javascript:goURL('${page.curr}')">${page.curr}</a>
            </c:if>

            <c:if test="${page.curr < page.pageSize -1 }">
                <a class="page-number" href="javascript:goURL('${page.next}')">${page.next}</a>
            </c:if>

            <a class="page-number">${page.pageSize}</a>
            <a class="extend next" rel="next" href="#"><i class="fa fa-angle-right"></i></a>
        </c:if>


            <%--<span class="page-number current">1</span>
            <a class="page-number" href="#">2</a>
            <span class="space">&hellip;</span>
            <a class="page-number" href="#">7</a>
            <a class="extend next" rel="next" href="#">
                <i class="fa fa-angle-right"></i>
            </a>--%>
    </nav>
</c:if>

</body>

<script type="text/javascript" src="${pageContext.request.contextPath}/statics/plugins/jquery.js"></script>
<script type="text/javascript">
    $(function () {
        $('a').click(function () {
            $('a').removeClass("current");
            $(this).addClass("current");
        });
    });

    function goURL(curr) {
        $('<form action="${pageContext.request.contextPath}/${page.url}" method="post" style="display:none" >' +
            '<input type="hidden"  name="currNum"  value="' + curr + '"/></form>')
            .appendTo('body')
            .submit();
    }
</script>
</html>

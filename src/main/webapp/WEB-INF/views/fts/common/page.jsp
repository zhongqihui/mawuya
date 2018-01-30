<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<c:if test="${page.pageSize > 1}">
    <nav class="pagination">
        <c:if test="${page.curr != 1}">
            <a class="extend prev" title="上一页" href="javascript:goURL('${page.curr -1}')"><i class="fa fa-angle-left"></i></a>
        </c:if>

        <script type="text/javascript" >

        </script>

        <c:forEach items="${page.pageLine}" var="line">
            <c:if test="${line != fn:trim(page.curr)}">
                <a class="page-number" href="javascript:goURL('${line}')">${line}</a>
            </c:if>
            <c:if test="${line == fn:trim(page.curr)}">
                <span class="page-number current" href="javascript:goURL('${line}')">${line}</span>
            </c:if>
            <c:if test="${line == '...'}">
                <span class="space">&hellip;</span>
            </c:if>
        </c:forEach>

        <c:if test="${page.curr != page.pageSize}">
            <a class="extend next" title="下一页" href="javascript:goURL('${page.curr + 1}')"><i class="fa fa-angle-right"></i></a>
        </c:if>


    </nav>
</c:if>

</body>

<script type="text/javascript" src="${pageContext.request.contextPath}/statics/plugins/jquery.js"></script>
<script type="text/javascript">
    function goURL(curr) {
        $('<form action="${pageContext.request.contextPath}/${page.url}" method="post" style="display:none" >' +
            '<input type="hidden"  name="currNum"  value="' + curr + '"/></form>')
            .appendTo('body')
            .submit();
    }

    $(function () {

    });
</script>
</html>

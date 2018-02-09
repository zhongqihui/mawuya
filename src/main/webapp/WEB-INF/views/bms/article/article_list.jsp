<%@ page language="java" contentType="text/html; utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
<%@ include file="../common/css-head.jsp"%>
</head>
<body>
<div class="page-head">
    <ol class="breadcrumb">
        <li>首页</li>
        <li>博客管理</li>
        <li class="active">博客列表</li>
    </ol>
</div>
    <div class="row">
        <div class="col-md-12">
            <div class="block-flat">
                <div class="content">
                    <div class="table-responsive">
                        <table class="table table-bordered" id="datatable-icons">
                            <thead>
                            <tr>
                                <th>序号</th>
                                <th>博客标题</th>
                                <th>博客分类</th>
                                <th>阅读次数</th>
                                <th>评论次数</th>
                                <th>图片路径</th>
                                <th>发布时间</th>
                                <th>操作</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${articleList}" var="item" varStatus="status">
                                <tr class="odd gradeX">
                                    <td>${status.count}</td>
                                    <td>${item.aTitle}</td>
                                    <td>
                                        <c:forEach items="${categoryList}" var="c">
                                            <c:if test="${c.sn eq item.categorySn}">
                                                c.CName
                                            </c:if>
                                        </c:forEach>
                                    </td>
                                    <td>${item.readNum}</td>
                                    <td>${item.reviewNum}</td>
                                    <td>${item.pictureUrl}</td>
                                    <td>${item.insertTime}</td>
                                    <td>${item.updateTime}</td>
                                    <td class="center">
                                        <i class="fa fa-pencil"></i>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
<%@ include file="../common/js-footer.jsp" %>

<script type="text/javascript">
    $(document).ready(function () {
        App.init();
        App.dataTables();
        $('#datatable-icons').dataTable();
    });
</script>
</body>
</html>

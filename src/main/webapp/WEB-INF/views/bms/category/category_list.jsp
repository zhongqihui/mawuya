<%@ page language="java" contentType="text/html; utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>

</head>
<body>
<div class="page-head">
    <h2>General Elements</h2>
    <ol class="breadcrumb">
        <li><a href="#">Home</a></li>
        <li><a href="#">博客管理</a></li>
        <li class="active">博客分类管理</li>
    </ol>
</div>

    <div class="row">
        <div class="col-md-12">
            <div class="block-flat">
                <div class="header">
                    <h3>博客分类列表</h3>
                </div>
                <div class="content">
                    <div class="table-responsive">
                        <table class="table table-bordered" id="datatable-icons">
                            <thead>
                            <tr>
                                <th>序号</th>
                                <th>分类名称</th>
                                <th>操作</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${categoryList}" var="item" varStatus="status">
                                <tr class="odd gradeX">
                                    <td>${status.count}</td>
                                    <td>${item.CName}</td>
                                    <td class="center"><i class="fa fa-pencil"></i></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>


<script type="text/javascript">
    $(document).ready(function () {
        App.init();
        App.dataTables();
        $('#datatable-icons').dataTable();
    });

</script>
</body>
</html>

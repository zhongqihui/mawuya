<%@ page language="java" contentType="text/html; utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="page-head">
    <ol class="breadcrumb">
        这是一个疯子的世界，只有疯子才能生存下去
        <button type="button" title="添加博客" id="article-add" class="btn btn-default btn-xs"><i class="fa fa-file"></i>
        </button>
    </ol>
</div>

    <div class="row">
        <div class="col-md-12">
            <div class="block-flat">
                <div class="content">
                    <div class="table-responsive">
                        <table class="table table-bordered" id="datatable-article">
                            <thead>
                            <tr>
                                <th>序号</th>
                                <th>博客标题</th>
                                <th>博客分类</th>
                                <th>阅读次数</th>
                                <th>评论次数</th>
                                <th>图片路径</th>
                                <th>发布时间</th>
                                <th>修改时间</th>
                                <th>操作</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${articleList}" var="item" varStatus="status">
                                <tr class="odd gradeX">
                                    <td>${status.count}</td>
                                    <td>${item.ATitle}</td>
                                    <td>
                                        <c:if test="${item.categorySn == 0}">
                                            暂无分类
                                        </c:if>
                                        <c:if test="${item.categorySn != 0}">
                                            <c:forEach items="${categoryList}" var="c">
                                                <c:if test="${c.sn eq item.categorySn}">
                                                    ${c.CName}
                                                </c:if>
                                            </c:forEach>
                                        </c:if>
                                    </td>
                                    <td>${item.readNum}</td>
                                    <td>${item.reviewNum}</td>
                                    <td>${item.pictureUrl}</td>
                                    <td>${item.insertTime}</td>
                                    <td>${item.updateTime}</td>
                                    <td class="center">
                                        <i class="fa fa-pencil" title="修改" update-id="${item.sn}"></i>&nbsp;&nbsp;
                                        <i class="fa fa-trash-o" title="删除" del-id="${item.sn}"></i>
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

<script>
    $(function () {
        $('#datatable-article').dataTable();

        //修改博客
        $('.fa-pencil').click(function () {
            var sn = $(this).attr('update-id');
            var url = '${pageContext.request.contextPath}/bms/article/toUpdate.do?sn=' + sn;
            $.get(url, function (data) {//get请求跳转修改博客分类页面
                layer.open({
                    type: 1,
                    skin: 'layui-layer-lan',
                    title: '修改博客分类',
                    content: data,
                    area: ['350px', '160px'],
                    btn: ['确定', '取消'],
                    yes: function (index, layero) {//确认修改操作
                        layer.close(index);//手动关闭layer层
                        var load = layer.load(0, {shade: [0.1, '#fff']});//加载load特效
                        $.ajax({
                            type: "post",
                            url: '${pageContext.request.contextPath}/bms/article/updateSubmit.do',
                            data: {
                                'sn': sn,
                                'CName': layero.find('input[name="CName"]').val()
                            },
                            success: function (result) {
                                layer.close(load); //关闭load特效
                                if (result == 'success') {
                                    layer.alert("修改成功", {
                                        skin: 'layui-layer-lan',
                                        icon: 1,
                                        end: function () {//修改成功则div重新加载
                                            $('#content3').load('${pageContext.request.contextPath}/bms/article/list.do');
                                        }
                                    });
                                } else {
                                    layer.alert('修改失败', {
                                        skin: 'layui-layer-lan',
                                        closeBtn: 0,
                                        icon: 2
                                    });
                                }
                            }
                        });
                    }
                });
            });
        });

        //删除博客
        $('.fa-trash-o').click(function () {
            var delId = $(this).attr('del-id');
            layer.confirm('确认删除？', {
                btn: ['是', '我后悔了'],
                skin: 'layui-layer-lan'
            }, function () {
                var load = layer.load(0, {shade: [0.1, '#fff']});
                $.ajax({
                    type: "get",
                    url: "${pageContext.request.contextPath}/bms/article/delSubmit.do?sn=" + delId,
                    cache: false,
                    success: function (data) {
                        layer.close(load);
                        if (data == 'success') {
                            layer.alert("删除成功", {
                                skin: 'layui-layer-lan',
                                icon: 1,
                                end: function () {
                                    $('#content3').load('${pageContext.request.contextPath}/bms/article/list.do');
                                }
                            });
                        } else {
                            layer.alert('删除失败', {
                                skin: 'layui-layer-lan',
                                closeBtn: 0,
                                icon: 2
                            });
                        }
                    }
                });
            }, function () {
            });
        });
    });
</script>


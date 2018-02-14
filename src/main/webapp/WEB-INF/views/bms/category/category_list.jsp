<%@ page language="java" contentType="text/html; utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="page-head">
    <ol class="breadcrumb">
        这是一个疯子的世界，只有疯子才能生存下去
        <button type="button" title="添加分类" onclick="addCategory()" class="btn btn-default btn-xs"><i
                class="fa fa-file"></i>
        </button>
    </ol>
</div>
<div class="row">
    <div class="col-md-12">
        <div class="block-flat">
            <div class="content">
                <div class="table-responsive">
                    <table class="table table-bordered" id="datatable-category">
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
                                <td>${item.categoryName}</td>
                                <td class="center">
                                    <i class="fa fa-pencil" title="修改" onclick="modCategory('${item.sn}')"></i>&nbsp;&nbsp;
                                    <i class="fa fa-trash-o" title="删除" onclick="delCategory('${item.sn}')"></i>
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


<script type="text/javascript">
    $(function () {
        $('#datatable-category').dataTable(); //表格框架初始化
    });

    //新增博客分类
    function addCategory() {
        var url = '${pageContext.request.contextPath}/bms/category/toAdd.do';
        $.get(url, function (data) {//get请求跳转修改博客分类页面
            layer.open({
                type: 1,
                skin: 'layui-layer-lan',
                title: '新增博客分类',
                content: data,
                area: ['350px', '160px'],
                btn: ['确定', '取消'],
                yes: function (index, layero) {//确认修改操作
                    layer.close(index);//手动关闭layer层
                    var load = layer.load(0, {shade: [0.1, '#fff']});//加载load特效
                    $.ajax({
                        type: "post",
                        url: '${pageContext.request.contextPath}/bms/category/addSubmit.do',
                        data: {
                            'categoryName': layero.find('input[name="categoryName"]').val()
                        },
                        success: function (result) {
                            layer.close(load); //关闭load特效
                            if (result == 'success') {
                                layer.alert("添加成功", {
                                    skin: 'layui-layer-lan',
                                    icon: 1,
                                    end: function () {//修改成功则div重新加载
                                        $('#content2').load('${pageContext.request.contextPath}/bms/category/categoryList.do');
                                    }
                                });
                            } else {
                                layer.alert('添加失败', {
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
    }


    // 删除博客分类
    function delCategory(sn) {
        layer.confirm('删除后，该分类下的博客将暂无分类，仍然删除？', {
            btn: ['是', '我后悔了'],
            skin: 'layui-layer-lan'
        }, function () {
            var load = layer.load(0, {shade: [0.1, '#fff']});
            $.ajax({
                type: "get",
                url: "${pageContext.request.contextPath}/bms/category/delSubmit.do?sn=" + sn,
                cache: false,
                success: function (data) {
                    layer.close(load);
                    if (data == 'success') {
                        layer.alert("删除成功", {
                            skin: 'layui-layer-lan',
                            icon: 1,
                            end: function () {
                                $('#content2').load('${pageContext.request.contextPath}/bms/category/categoryList.do');
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
        }, function () {});
    }


    // 修改博客分类
    function modCategory(sn) {
        var url = '${pageContext.request.contextPath}/bms/category/toUpdate.do?sn=' + sn;
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
                        url: '${pageContext.request.contextPath}/bms/category/updateSubmit.do',
                        data: {
                            'sn': sn,
                            'categoryName': layero.find('input[name="categoryName"]').val()
                        },
                        success: function (result) {
                            layer.close(load); //关闭load特效
                            if (result == 'success') {
                                layer.alert("修改成功", {
                                    skin: 'layui-layer-lan',
                                    icon: 1,
                                    end: function () {//修改成功则div重新加载
                                        $('#content2').load('${pageContext.request.contextPath}/bms/category/categoryList.do');
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
    }
</script>


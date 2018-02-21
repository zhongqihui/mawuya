<%@ page language="java" contentType="text/html; utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html style="background: #ffffff !important;">

<head>
    <meta charset="utf-8">
    <title>layui</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/statics/plugins/editor.md/css/editormd.css"/>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/statics/plugins/jquery.validationEngine/validationEngine.jquery.css"/>

</head>

<body>

<div class="page-head">
    <ol class="breadcrumb">
        <li>博客管理</li>
        <li>博客列表</li>
        <li class="active">博客修改</li>
        <span style="float: right;"><a onclick="retArticleList()">&lt;返回</a></span>
    </ol>
</div>

<div class="row">
    <div class="col-md-12">
        <div class="block-flat">
            <div class="content">
                <form class="form-horizontal group-border-dashed" id="mod-article-form" style="border-radius: 0px;">
                    <input type="hidden"  name="sn" value="${article.sn}"/>
                    <div class="form-group">
                        <label class="col-sm-1 control-label">博客标题</label>
                        <div class="col-sm-6">
                            <input type="text" class="form-control validate[required]" name="articleTitle" value="${article.articleTitle}" >
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-1 control-label">分类于</label>
                        <div class="col-sm-6">
                            <select class="select2" name="categorySn">
                                <option value="0">暂不分类</option>
                                <c:forEach items="${categoryList}" var="list">
                                    <option value="${list.sn}" <c:if test="${list.sn eq article.categorySn}">selected="selected"</c:if> >${list.categoryName}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-1 control-label">概要</label>
                        <div class="col-sm-8">
                            <textarea class="form-control validate[required]" name="articleSummary">${article.articleSummary}</textarea>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-1 control-label">博客内容</label>
                        <div class="col-sm-11" id="mod-article-editormd">
                            <textarea class="editormd-markdown-textarea" name="test-editormd-markdown-doc"
                                      id="editormd">${article.articleContent}</textarea>
                           <%-- <!-- 第二个隐藏文本域，用来构造生成的HTML代码，方便表单POST提交，这里的name可以任意取，后台接受时以这个name键为准 -->
                            <!-- html textarea 需要开启配置项 saveHTMLToTextarea == true -->
                            <textarea class="editormd-html-textarea" name="aContent"></textarea>--%>
                        </div>
                    </div>

                </form>

                <div class="form-group">
                    <label class="col-sm-1 control-label"></label>
                    <button type="button" class="btn btn-primary" id="mod_blog_btn">立即修改</button>
                    <button type="reset" class="btn btn-default" onclick="resetBlog()">重置</button>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/editor.md/editormd.min.js"></script>
<%--校验框架--%>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.validationEngine/jquery.validationEngine.min.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.validationEngine/jquery.validationEngine-zh_CN.js"></script>

<script>
    //jquery.validate框架初始化
    $(function () {
        $("#mod-article-form").validationEngine("attach", {scroll: false});
        App.init();

        var contentEditor = editormd("mod-article-editormd", {
            width: false,
            height: 640,
            //markdown : md,
            codeFold: true,
            syncScrolling: "single",
            //你的lib目录的路径
            path: "${pageContext.request.contextPath}/statics/plugins/editor.md/lib/",
            imageUpload: true,//关闭图片上传功能
            imageFormats : ["jpg", "jpeg", "gif", "png", "bmp", "webp"],
            imageUploadURL : "${pageContext.request.contextPath}/bms/article/imgUpload.do",//后端的上传图片服务地址
            //theme: "dark",//工具栏主题
            //previewTheme: "dark",//预览主题
            //editorTheme: "pastel-on-dark",//编辑主题
            emoji: true,
            taskList: true,
            tocm: true,         // Using [TOCM]
            tex: true,                   // 开启科学公式TeX语言支持，默认关闭
            flowChart: true,             // 开启流程图支持，默认关闭
            sequenceDiagram: true,       // 开启时序/序列图支持，默认关闭,
            //这个配置在simple.html中并没有，但是为了能够提交表单，使用这个配置可以让构造出来的HTML代码直接在第二个隐藏的textarea域中，方便post提交表单。
            //saveHTMLToTextarea: true
        });


        //发布博客
        $("#mod_blog_btn").click(function () {
            var flag = $("#mod-article-form").validationEngine("validate");
            if (flag == false) {
                return;
            } else {
                var content = contentEditor.getMarkdown();//editor.md的源代码
                var sn = $('#mod-article-form').find('input[name="sn"]').val();
                var title = $('#mod-article-form').find('input[name="articleTitle"]').val();
                var categorySn = $('#mod-article-form').find('select[name="categorySn"]').val();
                var summary = $('#mod-article-form').find('textarea[name="articleSummary"]').val();


                $.ajax({
                    type: "post",
                    url: "${pageContext.request.contextPath}/bms/article/updateSubmit.do",
                    data: {
                        "sn":sn,
                        "articleTitle":title,
                        "categorySn":categorySn,
                        "articleSummary": summary,
                        "articleContent": content
                    },
                    cache: false,
                    success: function (data) {
                        if (data == "success") {
                            layer.confirm('修改成功！赶快去看看吧', {
                                btn: ['去看看', '懒，不去'],
                                skin: 'layui-layer-lan',
                                anim: 4,
                                icon: 1
                            }, function (index) {
                                layer.close(index);
                                retArticleList();
                                window.open('${pageContext.request.contextPath}/'+ sn);
                            }, function (index) {
                                layer.close(index);
                                retArticleList();
                            });
                        } else {
                            layer.alert('修改失败', {
                                skin: 'layui-layer-lan',
                                closeBtn: 0,
                                nim: 4,
                                icon: 2
                            });
                        }
                    }
                });
            }
        });

    });

    // 重置博客内容信息
    function resetBlog() {
        /*$('#article-form')[0].reset();
        $('.fa-eraser').trigger('click');//触发editor.md的内置清除*/
    }

    // 返回上一页
    function retArticleList() {
        $('#content3').load('${pageContext.request.contextPath}/bms/article/list.do');
    }

</script>

<script type="text/javascript">

</script>

</body>

</html>
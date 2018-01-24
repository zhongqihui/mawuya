<%@ page language="java" contentType="text/html; utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <script type="text/javascript"
            src="${pageContext.request.contextPath}/statics/plugins/jquery.js"></script>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/statics/plugins/editor.md/css/editormd.css"/>
    <script type="text/javascript"
            src="${pageContext.request.contextPath}/statics/plugins/editor.md/editormd.min.js"></script>

</head>
<body>
<div class="page-head">
    <h2>General Elements</h2>
    <ol class="breadcrumb">
        <li><a href="#">Home</a></li>
        <li><a href="#">博客管理</a></li>
        <li class="active">写博客</li>
    </ol>
</div>

<div class="row">
    <div class="col-md-12">
        <div class="block-flat">
            <div class="content">
                <form class="form-horizontal group-border-dashed" action="#" style="border-radius: 0px;">
                    <div class="form-group">
                        <label class="col-sm-1 control-label">博客标题</label>
                        <div class="col-sm-6">
                            <input type="text" class="form-control" name="aTitle" id="aTitle"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-1 control-label">分类于</label>
                        <div class="col-sm-3">
                            <select class="form-control" name="categorySn" id="categorySn">
                                <option value="0">暂不分类</option>
                                <option value="2">2</option>
                                <option value="3">3</option>
                                <option value="4">4</option>
                                <option value="5">5</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-1 control-label">概要</label>
                        <div class="col-sm-6">
                            <textarea class="form-control" name="aSummary" id="aSummary"></textarea>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-1 control-label">博客内容</label>
                        <div class="editormd" id="test-editormd">
                            <textarea class="editormd-markdown-textarea" name="test-editormd-markdown-doc"
                                      id="editormd"></textarea>
                            <!-- 第二个隐藏文本域，用来构造生成的HTML代码，方便表单POST提交，这里的name可以任意取，后台接受时以这个name键为准 -->
                            <!-- html textarea 需要开启配置项 saveHTMLToTextarea == true -->
                            <textarea class="editormd-html-textarea" name="aContent" id="aContent"></textarea>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-sm-1 control-label"></div>
                        <div class="col-sm-6">
                            <button type="button" class="btn btn-success btn-rad" id="publish_blog">发布博客</button>
                            <button type="button" class="btn btn-warning btn-rad" id="save_blog">存草稿</button>
                        </div>
                    </div>

                </form>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    var testEditor;
    testEditor = $(function () {
        editormd("test-editormd", {
            width: "90%",
            height: 640,
            //markdown : md,
            codeFold: true,
            syncScrolling: "single",
            //你的lib目录的路径
            path: "${pageContext.request.contextPath}/statics/plugins/editor.md/lib/",
            imageUpload: false,//关闭图片上传功能
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
            saveHTMLToTextarea: true
        });
    });

    $("#publish_blog").click(function () {
        $.ajax({
            type:"post",
            url:"${pageContext.request.contextPath}/bms/writeArticle.do",
            data: {
                "aTitle": $("#aTitle").val(),
                "categorySn": $("#categorySn").val(),
                "aSummary": $("#aSummary").val(),
                "aContent": $("#aContent").val()
            },
            cache:false,
            success:function (data) {
                if(data == "success") {
                    alert("success");
                }
                else {
                    alert("fail");
                }
            }
        });
    });

</script>
</body>
</html>

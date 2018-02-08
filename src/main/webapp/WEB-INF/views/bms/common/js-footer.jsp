<%@ page language="java" contentType="text/html; utf-8" pageEncoding="utf-8" %>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.nestable/jquery.nestable.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/bootstrap/dist/js/bootstrap.min.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/bootstrap.switch/bootstrap-switch.min.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/bootstrap.datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/bootstrap.slider/js/bootstrap-slider.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.nanoscroller/jquery.nanoscroller.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.ui/jquery-ui.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.datatables/jquery.datatables.min.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.select2/select2.min.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/behaviour/general.js"></script>

<script type="text/javascript">
    $(document).ready(function () {
        App.init();
    });

    /*链接到后台url*/
    function linkTo(url) {
        $("#pcont").load(url);
    }
</script>

<script src="${pageContext.request.contextPath}/statics/plugins/layui/layui.js"></script>
<script>
    var message;
    layui.config({
        base: '${pageContext.request.contextPath}/statics/js/'
    }).use(['app', 'message'], function () {
        var app = layui.app,
            $ = layui.jquery,
            layer = layui.layer;
        //将message设置为全局以便子页面调用
        message = layui.message;
        //主入口
        app.set({
            type: 'iframe'
        }).init();

    });

    function getRootPath(){
        var curWwwPath=window.document.location.href;
        var pathName=window.document.location.pathname;
        var pos=curWwwPath.indexOf(pathName);
        var localhostPaht=curWwwPath.substring(0,pos);
        var projectName=pathName.substring(0,pathName.substr(1).indexOf('/')+1);
        var realPath=localhostPaht+projectName;
        return realPath;
    }
</script>
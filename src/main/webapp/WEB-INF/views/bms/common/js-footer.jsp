<%@ page language="java" contentType="text/html; utf-8" pageEncoding="utf-8" %>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.nanoscroller/jquery.nanoscroller.js"></script>

<%--<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.sparkline/jquery.sparkline.min.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.easypiechart/jquery.easy-pie-chart.js"></script>
<script src='http://maps.google.cn/maps/api/js?key=AIzaSyDY0kkJiTPVd2U7aTOAwhc9ySH6oHxOIYM' type="text/javascript"></script>--%>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/behaviour/general.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.ui/jquery-ui.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.nestable/jquery.nestable.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/bootstrap.switch/bootstrap-switch.min.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/bootstrap.datetimepicker/js/bootstrap-datetimepicker.min.js"></script>

<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/bootstrap.slider/js/bootstrap-slider.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.gritter/js/jquery.gritter.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.datatables/jquery.datatables.min.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.datatables/bootstrap-adapter/js/datatables.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/bootstrap/dist/js/bootstrap.min.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/behaviour/voice-commands.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.select2/select2.min.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/layer/layer.js"></script>
<%--<script type="text/javascript" src="${pageContext.request.contextPath}/statics/plugins/jquery.flot/jquery.flot.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/statics/plugins/jquery.flot/jquery.flot.pie.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/statics/plugins/jquery.flot/jquery.flot.resize.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/statics/plugins/jquery.flot/jquery.flot.labels.js"></script>--%>

<script type="text/javascript">
    $(document).ready(function () {
        App.init();
        App.dataTables();

        //左侧菜单栏点击事件
        $('.link-li').click(function () {
            var id = $(this).attr('id');
            var url = $(this).attr('link-url');
            var title = $(this).html();

            //如果该tab已经存在，则不会向后台请求，并将该tab设成active状态
            var flag = true;
            $('#tab-title').find('li').each(function () {
                var h = $(this).attr('id');
                if (h == 'title' + id) {
                    flag = false;
                }
            });

            cssActive(id);//class的active变化

            // 如果tab不存在，append内容div和tab
            if (flag) {
                $('#tab-title').append('<li class="title-li active" id="title' + id + '">' + title + '&nbsp;&nbsp;<button type="button" class="close" aria-label="Close"><span aria-hidden="true">&times;</span></button></li>');
                $('#tab-content').append('<div class="tab-pane active" id="content' + id + '"></div>');
                $('#content' + id).load(url);
            }

            //tab选项卡点击事件,css active事件
            $('.title-li').click(function () {
                var titleId = $(this).attr('id');
                var id = titleId.replace('title', '');
                cssActive(id);
            });

            //tab选项卡删除事件,并跳转到首页
            $('.close').click(function () {
                var titleId = $(this).parent('li').attr('id');
                var id = titleId.replace('title', '');
                $('#title' + id).remove();
                $('#content' + id).remove();
                cssActive(0);
            });

        });


        /*css的样式添加active*/
        function cssActive(id) {
            $('#tab-title').find('li').each(function () {
                $(this).attr("class", "title-li");
            });

            $('#title' + id).attr("class", "title-li active");

            $('#tab-content .tab-pane').each(function () {
                $(this).attr("class", "tab-pane");
            });

            $('#content' + id).attr("class", "tab-pane active");
        }
    });
</script>
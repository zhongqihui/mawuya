<%@ page language="java" contentType="text/html; utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/statics/images/favicon.jpg">
    <title>Zqh's Blog</title>
    <link href="${pageContext.request.contextPath}/statics/plugins/bootstrap/dist/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/statics/css/style.css" rel="stylesheet"/>
    <link rel="stylesheet" type="text/css" href="http://cdn.bootcss.com/font-awesome/4.6.0/css/font-awesome.min.css">
</head>

<body>
<div id="head-nav" class="navbar navbar-default navbar-fixed-top">
    <div class="container-fluid">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                <span class="fa fa-gear"></span>
            </button>
            <a class="navbar-brand" href="#"><span>Zqh's Blog</span></a>
        </div>
        <div class="navbar-collapse collapse">
            <ul class="nav navbar-nav">
                <li class="active"><a href="#">Home</a></li>
            </ul>
            <ul class="nav navbar-nav navbar-right user-nav">
                <li class="dropdown profile_menu">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <img alt="Avatar" src="${pageContext.request.contextPath}/statics/images/avatar2.jpg"/>钟启辉<b class="caret"></b>
                    </a>
                    <ul class="dropdown-menu">
                        <li><a href="#">我的账户</a></li>
                        <li><a href="#">消息</a></li>
                        <li class="divider"></li>
                        <li><a href="#">退出</a></li>
                    </ul>
                </li>
            </ul>
            <ul class="nav navbar-nav navbar-right not-nav">
                <li class="button dropdown">
                    <a href="javascript:;" class="dropdown-toggle" data-toggle="dropdown"><i
                            class=" fa fa-comments"></i></a>
                    <ul class="dropdown-menu messages">
                        <li>
                            <div class="nano nscroller">
                                <div class="content">
                                    <ul>
                                        <li>
                                            <a href="#">
                                                <img src="${pageContext.request.contextPath}/statics/images/avatar2.jpg" alt="avatar"/><span
                                                    class="date pull-right">13 Sept.</span> <span
                                                    class="name">Daniel</span> I'm following you, and I want your money!
                                            </a>
                                        </li>
                                        <li>
                                            <a href="#">
                                                <img src="${pageContext.request.contextPath}/statics/images/avatar_50.jpg" alt="avatar"/><span
                                                    class="date pull-right">20 Oct.</span><span class="name">Adam</span>
                                                is now following you
                                            </a>
                                        </li>
                                        <li>
                                            <a href="#">
                                                <img src="${pageContext.request.contextPath}/statics/images/avatar4_50.jpg" alt="avatar"/><span
                                                    class="date pull-right">2 Nov.</span><span
                                                    class="name">Michael</span> is now following you
                                            </a>
                                        </li>
                                        <li>
                                            <a href="#">
                                                <img src="${pageContext.request.contextPath}/statics/images/avatar3_50.jpg" alt="avatar"/><span
                                                    class="date pull-right">2 Nov.</span><span class="name">Lucy</span>
                                                is now following you
                                            </a>
                                        </li>
                                    </ul>
                                </div>
                            </div>
                            <ul class="foot">
                                <li><a href="#">View all messages </a></li>
                            </ul>
                        </li>
                    </ul>
                </li>
                <li class="button dropdown">
                    <a href="javascript:;" class="dropdown-toggle" data-toggle="dropdown"><i
                            class="fa fa-globe"></i><span class="bubble">2</span></a>
                    <ul class="dropdown-menu">
                        <li>
                            <div class="nano nscroller">
                                <div class="content">
                                    <ul>
                                        <li><a href="#"><i class="fa fa-cloud-upload info"></i><b>Daniel</b> is now
                                            following you <span class="date">2 minutes ago.</span></a></li>
                                        <li><a href="#"><i class="fa fa-male success"></i> <b>Michael</b> is now
                                            following you <span class="date">15 minutes ago.</span></a></li>
                                        <li><a href="#"><i class="fa fa-bug warning"></i> <b>Mia</b> commented on post
                                            <span class="date">30 minutes ago.</span></a></li>
                                        <li><a href="#"><i class="fa fa-credit-card danger"></i> <b>Andrew</b> killed
                                            someone <span class="date">1 hour ago.</span></a></li>
                                    </ul>
                                </div>
                            </div>
                            <ul class="foot">
                                <li><a href="#">View all activity </a></li>
                            </ul>
                        </li>
                    </ul>
                </li>
                <li class="button"><a href="javascript:;" class="speech-button"><i class="fa fa-microphone"></i></a>
                </li>
            </ul>

        </div>
    </div>
</div>


<div id="cl-wrapper">
    <div class="cl-sidebar">
        <div class="cl-toggle"><i class="fa fa-bars"></i></div>
        <div class="cl-navblock">
            <div class="menu-space">
                <div class="content">
                    <div class="side-user">
                        <div class="avatar"><img src="${pageContext.request.contextPath}/statics/images/avatar1_50.jpg" alt="Avatar"/></div>
                        <div class="info">
                            <a href="#">钟启辉</a>
                            <img src="${pageContext.request.contextPath}/statics/images/state_online.png" alt="Status"/> <span>Online</span>
                        </div>
                    </div>
                    <ul class="cl-vnavigation">
                        <li><a href="#"><i class="fa fa-home"></i><span>我的博客</span></a>
                            <ul class="sub-menu">
                                <li><a href="${pageContext.request.contextPath}/index" target="_blank">博客首页</a></li>
                                <li><a href="dashboard2.html"><span class="label label-primary pull-right">New</span>
                                    Version 2</a></li>
                            </ul>
                        </li>
                        <li><a href="#"><i class="fa fa-smile-o"></i><span>博客管理</span></a>
                            <ul class="sub-menu">
                                <li><a href="javascript:linkTo('${pageContext.request.contextPath}/bms/writeArticle.do')">写博客</a></li>
                                <li><a href="ui-buttons.html">Buttons</a></li>
                                <li><a href="ui-modals.html"><span class="label label-primary pull-right">New</span>
                                    Modals</a></li>
                                <li><a href="ui-notifications.html"><span
                                        class="label label-primary pull-right">New</span> Notifications</a></li>
                                <li><a href="ui-icons.html">Icons</a></li>
                                <li><a href="ui-grid.html">Grid</a></li>
                                <li><a href="ui-tabs-accordions.html">Tabs & Acordions</a></li>
                                <li><a href="ui-nestable-lists.html">Nestable Lists</a></li>
                                <li><a href="ui-treeview.html">Tree View</a></li>
                            </ul>
                        </li>
                        <li><a href="#"><i class="fa fa-list-alt"></i><span>留言管理</span></a>
                            <ul class="sub-menu">
                                <li><a href="form-elements.html">Components</a></li>
                                <li><a href="form-validation.html">Validation</a></li>
                                <li><a href="form-wizard.html">Wizard</a></li>
                                <li><a href="form-masks.html">Input Masks</a></li>
                                <li><a href="form-multiselect.html"><span
                                        class="label label-primary pull-right">New</span>Multi Select</a></li>
                                <li><a href="form-wysiwyg.html"><span class="label label-primary pull-right">New</span>WYSIWYG
                                    Editor</a></li>
                                <li><a href="form-upload.html"><span class="label label-primary pull-right">New</span>Multi
                                    Upload</a></li>
                            </ul>
                        </li>
                        <li><a href="#"><i class="fa fa-table"></i><span>日志管理</span></a>
                            <ul class="sub-menu">
                                <li><a href="tables-general.html">General</a></li>
                                <li><a href="tables-datatables.html"><span
                                        class="label label-primary pull-right">New</span>Data Tables</a></li>
                            </ul>
                        </li>
                        <li><a href="#"><i class="fa fa-map-marker nav-icon"></i><span>Maps</span></a>
                            <ul class="sub-menu">
                                <li><a href="maps.html">Google Maps</a></li>
                                <li><a href="vector-maps.html"><span class="label label-primary pull-right">New</span>Vector
                                    Maps</a></li>
                            </ul>
                        </li>
                        <li><a href="#"><i class="fa fa-envelope nav-icon"></i><span>Email</span></a>
                            <ul class="sub-menu">
                                <li><a href="email-inbox.html">Inbox</a></li>
                                <li><a href="email-read.html">Email Detail</a></li>
                                <li><a href="email-compose.html"><span class="label label-primary pull-right">New</span>Email
                                    Compose</a></li>
                            </ul>
                        </li>
                        <li><a href="typography.html"><i class="fa fa-text-height"></i><span>Typography</span></a></li>
                        <li><a href="charts.html"><i class="fa fa-bar-chart-o"></i><span>Charts</span></a></li>
                        <li><a href="#"><i class="fa fa-file"></i><span>Pages</span></a>
                            <ul class="sub-menu">
                                <li><a href="pages-blank.html">Blank Page</a></li>
                                <li class="active"><a href="pages-blank-header.html">Blank Page Header</a></li>
                                <li><a href="pages-blank-aside.html">Blank Page Aside</a></li>
                                <li><a href="pages-login.html">Login</a></li>
                                <li><a href="pages-404.html">404 Page</a></li>
                                <li><a href="pages-500.html">500 Page</a></li>
                                <li><a href="pages-sign-up.html"><span class="label label-primary pull-right">New</span>Sign
                                    Up</a></li>
                                <li><a href="pages-forgot-password.html"><span class="label label-primary pull-right">New</span>Forgot
                                    Password</a></li>
                                <li><a href="pages-profile.html"><span class="label label-primary pull-right">New</span>Profile</a>
                                </li>
                                <li><a href="pages-search.html"><span class="label label-primary pull-right">New</span>Search</a>
                                </li>
                                <li><a href="pages-calendar.html"><span
                                        class="label label-primary pull-right">New</span>Calendar</a></li>
                                <li><a href="pages-code-editor.html"><span
                                        class="label label-primary pull-right">New</span>Code Editor</a></li>
                                <li><a href="pages-gallery.html">Gallery</a></li>
                                <li><a href="pages-timeline.html">Timeline</a></li>
                            </ul>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="text-right collapse-button" style="padding:7px 9px;">
                <input type="text" class="form-control search" placeholder="Search..."/>
                <button id="sidebar-collapse" class="btn btn-default" style="">
                    <i style="color:#fff;" class="fa fa-angle-left"></i>
                </button>
            </div>
        </div>
    </div>

    <div class="container-fluid" id="pcont"></div>

</div>

<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/behaviour/general.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/bootstrap/dist/js/bootstrap.min.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/statics/plugins/jquery.nanoscroller/jquery.nanoscroller.js"></script>
<script type="text/javascript">
    $(document).ready(function () {
        App.init();
    });

    /*链接到后台url*/
    function linkTo(url) {
        $("#pcont").load(url);
    }
</script>
<script type="text/javascript">

</script>
</body>
</html>

<%@ page language="java" contentType="text/html; utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div id="cl-wrapper">
    <div class="cl-sidebar">
        <div class="cl-toggle"><i class="fa fa-bars"></i></div>
        <div class="cl-navblock">
            <div class="menu-space">
                <div class="content">
                    <div class="side-user">
                        <div class="avatar">
                            <img src="${pageContext.request.contextPath}/statics/images/avatar1_50.jpg" alt="Avatar"/>
                        </div>
                        <div class="info">
                            <a href="#">钟启辉</a>
                            <img src="${pageContext.request.contextPath}/statics/images/state_online.png" alt="Status"/>
                            <span>Online</span>
                        </div>
                    </div>
                    <ul class="cl-vnavigation">

                        <li><a href="#"><i class="fa fa-home"></i><span>我的博客</span></a>
                            <ul class="sub-menu">
                                <li>
                                    <a href="${pageContext.request.contextPath}/index" target="_blank">博客首页</a>
                                </li>
                            </ul>
                        </li>

                        <li><a href="#"><i class="fa fa-smile-o"></i><span>博客管理</span></a>
                            <ul class="sub-menu" >
                                <%--<li>
                                    <a link-url="${pageContext.request.contextPath}/bms/article/toAdd.do" class="link-li" id="1">&nbsp;&nbsp;发布博客</a>
                                </li>--%>
                                <li>
                                    <a link-url="${pageContext.request.contextPath}/bms/category/categoryList.do" class="link-li" id="2">&nbsp;&nbsp;分类列表</a>
                                </li>
                                <li>
                                    <a link-url="${pageContext.request.contextPath}/bms/article/list.do" class="link-li" id="3">&nbsp;&nbsp;博客列表</a>
                                </li>
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

    <!-- 内容主体区域 -->
    <div class="container-fluid" id="pcont">
        <div class="row">
            <div class="tab-container">
                <ul class="nav nav-tabs" id="tab-title">
                    <li class="title-li active" id="title0"  style="margin-left: 50px;">
                        <i class="fa fa-home"></i>首页
                    </li>
                </ul>
                <div class="tab-content" id="tab-content">
                    <div class="tab-pane active cont" id="content0">
                        <p>首页</p>
                    </div>
                </div>
            </div>
        </div>
    </div>

</div>


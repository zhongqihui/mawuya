<%@ page language="java" contentType="text/html; utf-8" pageEncoding="utf-8" %>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="shortcut icon" href="${pageContext.request.contextPath}/statics/images/favicon.ico">
<title>Zqh's Blog</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/statics/plugins/bootstrap/dist/css/bootstrap.css">
<!-- Bootstrap core CSS -->
<link rel="stylesheet" type="text/css"
      href="${pageContext.request.contextPath}/statics/plugins/jquery.gritter/css/jquery.gritter.css"/>
<link rel="stylesheet" type="text/css" href="http://cdn.bootcss.com/font-awesome/4.6.0/css/font-awesome.min.css">
<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
<script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
<![endif]-->

<link rel="stylesheet" type="text/css"
      href="${pageContext.request.contextPath}/statics/plugins/jquery.nanoscroller/nanoscroller.css"/>
<link rel="stylesheet" type="text/css"
      href="${pageContext.request.contextPath}/statics/plugins/jquery.easypiechart/jquery.easy-pie-chart.css"/>
<link rel="stylesheet" type="text/css"
      href="${pageContext.request.contextPath}/statics/plugins/bootstrap.switch/bootstrap-switch.css"/>
<link rel="stylesheet" type="text/css"
      href="${pageContext.request.contextPath}/statics/plugins/bootstrap.datetimepicker/css/bootstrap-datetimepicker.min.css"/>
<link rel="stylesheet" type="text/css"
      href="${pageContext.request.contextPath}/statics/plugins/jquery.select2/select2.css"/>
<link rel="stylesheet" type="text/css"
      href="${pageContext.request.contextPath}/statics/plugins/bootstrap.slider/css/slider.css"/>
<link rel="stylesheet" type="text/css"
      href="${pageContext.request.contextPath}/statics/plugins/jquery.datatables/bootstrap-adapter/css/datatables.css"/>
<link href="${pageContext.request.contextPath}/statics/css/style.css" rel="stylesheet"/>


<style>
    #tab-title li {
        display: inline-block;
        vertical-align: middle;
        font-size: 13px;
        transition: all .2s;
        -webkit-transition: all .2s;
        position: relative;
        line-height: 40px;
        min-width: 65px;
        padding: 0 10px;
        text-align: center;
        cursor: pointer;
    }

    .active {
        background: #ffffff;
    }

    .fa-pencil,.fa-trash-o{
        width: 13px;
        height:13px;
    }

    .fa-pencil:hover,.fa-trash-o:hover {
        background: #000;
        text-decoration: none;
        cursor: pointer;
        filter: alpha(opacity=50);
        opacity: .5;
    }

    a {
        cursor:pointer;
    }
</style>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="en">

<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/statics/images/favicon.ico">
    <title>zqh's Blog</title>

    <%--<link rel="stylesheet" type="text/css"
          href="${pageContext.request.contextPath}/statics/plugins/bootstrap/dist/css/bootstrap.css">
    <link rel="stylesheet" type="text/css" href="http://cdn.bootcss.com/font-awesome/4.6.0/css/font-awesome.min.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/statics/css/style.css"/>--%>


</head>

<body class="texture">

<div id="cl-wrapper" class="login-container">

    <div class="middle-login">
        <div class="block-flat">
            <div class="header">
                <h3 class="text-center"><img class="logo-img" src="${pageContext.request.contextPath}/statics/images/favicon.ico" alt="logo"/>Zqh's Blog</h3>
            </div>
            <div>
                <form style="margin-bottom: 0px !important;" class="form-horizontal" method="post"
                      action="${pageContext.request.contextPath}/bms/login.do">
                    <div class="content">
                        <h4 class="title">用户登录</h4>
                        <div class="form-group">
                            <div class="col-sm-12">
                                <div class="input-group">
                                    <span class="input-group-addon"><i class="fa fa-user"></i></span>
                                    <input type="text" placeholder="用户名" id="username" name="userName" value="admin"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-sm-12">
                                <div class="input-group">
                                    <span class="input-group-addon"><i class="fa fa-lock"></i></span>
                                    <input type="password" placeholder="密码" id="password" name="password" class="form-control">
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="foot">
                        <button class="btn btn-primary" data-dismiss="modal" type="submit">登录</button>
                    </div>
                </form>
            </div>
        </div>
       <%-- <div class="text-center out-links"><a href="#">&copy; 2013 Your Company</a></div>--%>
    </div>

</div>

</body>
</html>


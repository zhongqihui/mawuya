<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta http-equiv="content-type" content="text/html;charset=utf-8"/>
    <meta charset="UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1"/>
    <meta http-equiv="Cache-Control" content="no-transform"/>
    <meta http-equiv="Cache-Control" content="no-siteapp"/>
    <meta name="author" content="Zqh"/>
    <meta name="keywords" content="Java"/>
    <meta name="description" content="这是一个疯子的世界，只有疯子才能生存下去。">

    <link rel="shortcut icon" href="${pageContext.request.contextPath}/statics/images/favicon.jpg"/>
    <link href="${pageContext.request.contextPath}/statics/plugins/vendors/fancybox/source/jquery.fancybox8cbb.css?v=2.1.5"
          rel="stylesheet" type="text/css"/>
    <link href="${pageContext.request.contextPath}/statics/plugins/vendors/font-awesome/css/font-awesome.min93e3.css?v=4.4.0"
          rel="stylesheet" type="text/css"/>
    <link href="${pageContext.request.contextPath}/statics/css/main5174.css?v=0.5.0" rel="stylesheet" type="text/css"/>

    <script type="text/javascript" id="hexo.configuration">
        var NexT = window.NexT || {};
        var CONFIG = {
            scheme: 'Mist',
            sidebar: {"position": "left", "display": "post"},
            fancybox: true,
            motion: true
        };
    </script>
</head>
<body>
<header id="header" class="header">
    <div class="header-inner">
        <div class="site-meta ">
            <div class="custom-logo-site-title">
                <a href="index.html" class="brand" rel="start">
                    <span class="logo-line-before"><i></i></span>
                    <span class="site-title">Zqh's Blog</span>
                    <span class="logo-line-after"><i></i></span>
                </a>
            </div>
            <p class="site-subtitle">这是一个疯子的世界，只有疯子才能生存下去。</p>
        </div>

        <div class="site-nav-toggle">
            <button>
                <span class="btn-bar"></span>
                <span class="btn-bar"></span>
                <span class="btn-bar"></span>
            </button>
        </div>

        <nav class="site-nav">
            <ul id="menu" class="menu menu-left">
                <li class="menu-item menu-item-home">
                    <a href="${pageContext.request.contextPath}/index" rel="section">
                        <i class="menu-item-icon fa fa-home fa-fw"></i> <br/>首页
                    </a>
                </li>

                <li class="menu-item menu-item-archives">
                    <a href="${pageContext.request.contextPath}/archive" rel="section">
                        <i class="menu-item-icon fa fa-archive fa-fw"></i> <br/>归档
                    </a>
                </li>

                <li class="menu-item menu-item-categories">
                    <a href="${pageContext.request.contextPath}/categories" rel="section">
                        <i class="menu-item-icon fa fa-th fa-fw"></i> <br/>分类
                    </a>
                </li>

                <li class="menu-item menu-item-tags">
                    <a href="tags/index.html" rel="section">
                        <i class="menu-item-icon fa fa-tags fa-fw"></i> <br/>标签
                    </a>
                </li>

                <li class="menu-item menu-item-about">
                    <a href="about/index.html" rel="section">
                        <i class="menu-item-icon fa fa-user fa-fw"></i> <br/>关于
                    </a>
                </li>
            </ul>


            <div class="site-search">
                <form class="site-search-form">
                    <input type="text" id="st-search-input" class="st-search-input st-default-search-input"/>
                </form>

            </div>
        </nav>

    </div>
</header>

</body>
</html>

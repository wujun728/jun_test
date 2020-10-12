<%--
  Created by IntelliJ IDEA.
  User: dyl
  Date: 2018/12/29
  Time: 14:50
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>激活邮箱</title>
    <jsp:include page="${pageContext.request.contextPath}/WEB-INF/bbs_jsp/common/commonjs.jsp"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resource/bbs_resourse/css/global.css">
</head>
<body>
<jsp:include page="${pageContext.request.contextPath}/WEB-INF/bbs_jsp/common/header.jsp"/>

<div class="layui-container fly-marginTop  fly-user-main">
    <ul class="layui-nav layui-nav-tree layui-inline" lay-filter="user">
        <li class="layui-nav-item">
            <a href="home.html">
                <i class="layui-icon">&#xe609;</i>
                我的主页
            </a>
        </li>
        <li class="layui-nav-item layui-this">
            <a href="index.html">
                <i class="layui-icon">&#xe612;</i>
                用户中心
            </a>
        </li>
        <li class="layui-nav-item">
            <a href="set.html">
                <i class="layui-icon">&#xe620;</i>
                基本设置
            </a>
        </li>
        <li class="layui-nav-item">
            <a href="message.html">
                <i class="layui-icon">&#xe611;</i>
                我的消息
            </a>
        </li>
    </ul>

    <div class="site-tree-mobile layui-hide">
        <i class="layui-icon">&#xe602;</i>
    </div>
    <div class="site-mobile-shade"></div>

    <div class="site-tree-mobile layui-hide">
        <i class="layui-icon">&#xe602;</i>
    </div>
    <div class="site-mobile-shade"></div>


    <div class="fly-panel layui-panel-user" pad20="">
        <div class="layui-tab layui-tab-brief" lay-filter="user">
            <ul class="layui-tab-title">
                <li class="layui-this">激活邮箱</li>
            </ul>
            <div class="layui-tab-content" id="LAY_ucm" style="padding: 20px 0">
                <ul class="layui-form-item">
                    <li class="layui-form-li">
                        <input type="tst" id="brand1" value="">
                        <label for="brand1">您的邮箱:</label>
                        <span class="layui-form-text">xx@xx.com
                            <!-- <em style="color:#999;">（已成功激活）</em> -->
                        <em style="color:#c00;">（尚未激活）</em>
                        </span>
                    </li>
                    <li class="layui-form-li" style="margin-top: 20px; line-height: 26px;">
                        <div>
                            1. 如果您未收到邮件，或激活链接失效，您可以
                            <a class="layui-form-a" style="color:#4f99cf;" id="LAY-activate" href="javascript:;" email="{{user.email}}">重新发送邮件</a>，或者
                            <a class="layui-form-a" style="color:#4f99cf;" href="set.html">更换邮箱</a>；
                        </div>
                        <div>
                            2. 如果您始终没有收到 社区 发送的邮件，请注意查看您邮箱中的垃圾邮件；
                        </div>
                        <div>
                            3. 如果你实在无法激活邮件，您还可以联系：admin@xx.com
                        </div>
                    </li>
                </ul>
            </div>
        </div>
    </div>

</div>


<jsp:include page="${pageContext.request.contextPath}/WEB-INF/bbs_jsp/common/footer.jsp"/>
</body>
</html>
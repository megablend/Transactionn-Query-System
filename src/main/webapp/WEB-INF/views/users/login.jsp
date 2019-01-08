<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>

<c:url value="/resources/" var="resourceUrl" />



<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>NIBSS | TQS</title>
    <!-- Tell the browser to be responsive to screen width -->
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="${resourceUrl}css/bootstrap.min.css">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="${resourceUrl}css/font-awesome.min.css">
    <!-- Ionicons -->
    <link rel="stylesheet" href="${resourceUrl}css/ionicons.min.css">
    <!-- Theme style -->
    <link rel="stylesheet" href="${resourceUrl}css/AdminLTE.min.css">
    <!-- iCheck -->
    <link rel="stylesheet" href="${resourceUrl}css/iCheck/square/blue.css">

    <link href="${resourceUrl}css/custom.css" rel="stylesheet">
</head>


    <body class="hold-transition login-page">
    <div class="login-box">
        <div class="login-logo">
            <img src="${resourceUrl}images/logo.gif" alt="NIBSS Logo" class="img-responsive" />
        </div>
        <!-- /.login-logo -->
        <div class="login-box-body">
            <p class="login-box-msg">NIBSS Transaction Query System</p>

            <form action="<c:url value="/login" />" method="post" autocomplete="off">
                <div class="form-group has-feedback">
                    <input type="email" class="form-control" placeholder="Email" name="username" autocomplete="off">
                    <span class="glyphicon glyphicon-envelope form-control-feedback"></span>
                </div>
                <div class="form-group has-feedback">
                    <input type="password" class="form-control" placeholder="Password" name="password">
                    <span class="glyphicon glyphicon-lock form-control-feedback"></span>
                </div>
                <div class="row">
                    <div class="col-xs-8">
                        <a href="<c:url value="/forgotpassword" />"><i class="fa fa-info-circle"></i> I forgot my password</a><br>
                        <div class="checkbox icheck hide">
                            <label>
                                <input type="checkbox"> Remember Me
                            </label>
                        </div>
                    </div>
                    <!-- /.col -->
                    <div class="col-xs-4">
                        <button type="submit" class="btn btn-danger btn-block btn-flat">Log In</button>
                    </div>
                    <!-- /.col -->
                </div>
                <sec:csrfInput/>
            </form>
            <c:if test="${sessionScope.SPRING_SECURITY_LAST_EXCEPTION != null}">
                <div class="bg-warning" style="margin-top:15px;">
                        <i class="fa fa-exclamation-triangle"></i> ${sessionScope.SPRING_SECURITY_LAST_EXCEPTION.message}
                </div>
                <c:remove var="SPRING_SECURITY_LAST_EXCEPTION" scope="session" />
            </c:if>
           <%-- <c:if test="${param.logout != null}">
                <div class="bg-success" style="margin-top:15px;">
                    You have successfully logged out!
                </div>
            </c:if>--%>

            <div class="row">
                <span>
                    <a href="http://www.nibss-plc.com/privacy-policy" class="label label-default">Privacy Policy</a>&nbsp;
                    <a href="http://www.nibss-plc.com/terms-of-use" class="label label-default">Terms of Use</a>&nbsp;
                    <a href="http://www.nibss-plc.com/disclaimer" class="label label-default">Disclaimer</a>
                </span>
            </div>



        </div>
        <!-- /.login-box-body -->
    </div>
    <!-- /.login-box -->

    <!-- jQuery 2.2.0 -->
    <script src="${resourceUrl}js/jQuery-2.2.0.min.js"></script>
    <!-- Bootstrap 3.3.6 -->
    <script src="${resourceUr}js/bootstrap.min.js"></script>
    <!-- iCheck -->
    <script src="${resourceUrl}js/icheck.min.js"></script>
    <script>
        $(function () {
            $('input').iCheck({
                checkboxClass: 'icheckbox_square-blue',
                radioClass: 'iradio_square-blue',
                increaseArea: '20%' // optional
            });
        });
    </script>
</body>
</html>
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
    <div class="login-box" style="padding-bottom: 30px;">
        <div class="login-logo">
            <img src="${resourceUrl}images/logo.gif" alt="NIBSS Logo" class="img-responsive" />
        </div>
        <!-- /.login-logo -->
        <div class="login-box-body">
            <p class="login-box-msg">NIBSS Transaction Query System | <strong>Reset Password</strong></p>

          <c:choose>
              <c:when test="${success != null}">
                  <div class="row alert alert-success">
                      <p>Your password has been successfully reset. You should get an email with the newly generated password soon.
                      Kindly <a href="<c:url value="/login" />">Log</a> into the application with your newly generated password.</p>
                  </div>

              </c:when>
              <c:otherwise>
                  <form action="<c:url value="/forgotpassword" />" method="post">
                      <div class="form-group has-feedback">
                          <input type="email" class="form-control" placeholder="Email" name="username" autocomplete="off">
                          <span class="glyphicon glyphicon-envelope form-control-feedback"></span>
                      </div>
                      <div class="col-xs-6">
                          <a href="<c:url value="/login" />"><i class="fa fa-info-circle"></i> Back to Login Page</a><br>
                      </div>
                      <!-- /.col -->
                      <div class="col-xs-6">
                          <button type="submit" class="btn btn-danger btn-block btn-flat">Reset Password</button>
                      </div>
                      <!-- /.col -->
                      <sec:csrfInput/>
                  </form>
              </c:otherwise>
          </c:choose>

            <c:if test="${error != null}">
                <div class="row alert alert-danger">
                        ${error}
                </div>
            </c:if>


            <div class="row">
                <span>
                    <a href="http://www.nibss-plc.com/privacy-policy" class="label label-default">Privacy Policy</a>&nbsp;
                    <a href="http://www.nibss-plc.com/terms-of-use" class="label label-default">Terms of Use</a>&nbsp;
                    <a href="http://www.nibss-plc.com/disclaimer" class="label label-default">Disclaimer</a>
                </span>
            </div>


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
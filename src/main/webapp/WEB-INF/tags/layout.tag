<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- Page attributes and fragments defined here -->
<%@ attribute name="title" description="The Page Header" required="false" type="java.lang.String" %>
<%@ attribute name="contentHeader" description="The Content Header for the page" type="java.lang.String" %>

<%@ attribute name="scripts" description="Fragment for custom JavaScript code" fragment="true" %>

<!DOCTYPE html>
<html>
<head>
    <c:url value="/resources/" var="resourceUrl"/>
    <sec:csrfMetaTags/>

    <meta charset="utf-8">
    <title>NIBSS Transaction Query System</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <meta name="description" content="">
    <meta name="author" content="">

    <link rel="stylesheet" href="${resourceUrl}css/bootstrap.min.css"/>
    <!-- Font Awesome -->
    <link rel="stylesheet" href="${resourceUrl}css/font-awesome.min.css"/>
    <!-- Ionicons -->
    <link rel="stylesheet" href="${resourceUrl}css/ionicons.min.css"/>
    <!-- Theme style -->
    <link rel="stylesheet" href="${resourceUrl}css/AdminLTE.min.css"/>
    <link rel="stylesheet" href="${resourceUrl}css/skin-yellow-light.min.css"/>
    <link rel="stylesheet" href="${resourceUrl}css/custom.css"/>
    <!-- iCheck -->
    <link rel="stylesheet" href="${resourceUrl}css/iCheck/square/blue.css"/>
    <link rel="stylesheet" href="${resourceUrl}css/notifIt.css"/>
    <link rel="stylesheet" href="${resourceUrl}css/select2.min.css"/>
    <link rel="stylesheet" href="${resourceUrl}css/dataTables.bootstrap.css"/>
    <link rel="stylesheet" href="${resourceUrl}css/daterangepicker-bs3.css"/>

    <link rel="stylesheet" href="${resourceUrl}css/buttons.dataTables.min.css"/>
    <!-- jQuery 2.2.0 -->
    <script src="${resourceUrl}js/jQuery-2.2.0.min.js"></script>
    <!-- Bootstrap 3.3.6 -->
    <script src="${resourceUrl}js/bootstrap.min.js"></script>
    <script src="${resourceUrl}js/jquery.validate.min.js"></script>
    <!--notif it -->
    <script src="${resourceUrl}js/notifIt.js"></script>
    <!--blockUI -->
    <script src="${resourceUrl}js/jquery.blockUI.js"></script>
    <!-- select2 -->
    <script src="${resourceUrl}js/select2.full.min.js"></script>
    <script src="${resourceUrl}js/jquery.dataTables.min.js"></script>
    <script src="${resourceUrl}js/dataTables.bootstrap.min.js"></script>
    <script src="${resourceUrl}js/dataTables.buttons.min.js"></script>
    <script src="${resourceUrl}js/moment.min.js"></script>
    <script src="${resourceUrl}js/daterangepicker.js"></script>

    <!--colorbox -->
    <link href="${resourceUrl}css/colorbox.css" rel="stylesheet"/>
    <script src="${resourceUrl}js/jquery.colorbox-min.js"></script>

    <script src="${resourceUrl}js/icheck.min.js"></script>
    <!--custom JS file -->
    <script src="${resourceUrl}js/App.js"></script>
</head>


<body class="hold-transition skin-yellow-light sidebar-mini">
<!-- Site wrapper -->
<div class="wrapper">

    <header class="main-header">
        <!-- Logo -->
        <a href="#" class="logo">
            <!-- mini logo for sidebar mini 50x50 pixels -->
            <span class="logo-mini"><b>T</b>QS</span>
            <!-- logo for regular state and mobile devices -->
            <span class="logo-lg"><b>NIBSS</b>TQS</span>
        </a>
        <!-- Header Navbar: style can be found in header.less -->
        <nav class="navbar navbar-static-top">
            <!-- Sidebar toggle button-->
            <a href="#" class="sidebar-toggle" data-toggle="offcanvas" role="button">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </a>

            <div class="navbar-custom-menu">
                <ul class="nav navbar-nav">

                    <!-- User Account: style can be found in dropdown.less -->
                    <li class="dropdown user user-menu">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <%--<img src="../../dist/img/user2-160x160.jpg" class="user-image" alt="User Image">--%>
                            <span class="hidden-xs">
                                        <sec:authorize access="isAuthenticated()">
                                            <sec:authentication property="principal.fullName"/>
                                        </sec:authorize>
                                    </span>
                        </a>
                        <ul class="dropdown-menu">
                            <!-- User image -->
                            <li class="user-header">
                                <%--<img src="../../dist/img/user2-160x160.jpg" class="img-circle" alt="User Image">--%>

                                <p>
                                    <sec:authorize access="isAuthenticated()">
                                        <sec:authentication property="principal.fullName"/>
                                        <small>${sessionScope.user.organizationInterface.name}</small>
                                    </sec:authorize>

                                </p>
                            </li>
                            <!-- Menu Body -->
                            <%--<li class="user-body">
                                    <div class="row">
                                            <div class="col-xs-4 text-center">
                                                    <a href="#">Followers</a>
                                            </div>
                                            <div class="col-xs-4 text-center">
                                                    <a href="#">Sales</a>
                                            </div>
                                            <div class="col-xs-4 text-center">
                                                    <a href="#">Friends</a>
                                            </div>
                                    </div>
                                    <!-- /.row -->
                            </li>--%>
                            <!-- Menu Footer-->
                            <li class="user-footer">
                                <div class="pull-left">
                                    <a href="#" class="btn btn-info btn-flat" data-toggle="modal"
                                       data-target="#changePasswordDialog"><i class="fa fa-key"></i> Change Password</a>
                                </div>
                                <div class="pull-right">
                                    <form method="post" action="<c:url value="/logout" />" role="form"
                                          style="display: inline">
                                        <sec:csrfInput/>
                                        <button type="submit" class="btn btn-danger btn-flat"><i
                                                class="fa fa-power-off"></i> Sign Out
                                        </button>
                                    </form>
                                </div>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
        </nav>
    </header>

    <!-- =============================================== -->

    <!-- Left side column. contains the sidebar -->
    <aside class="main-sidebar">
        <!-- sidebar: style can be found in sidebar.less -->
        <section class="sidebar">
            <!-- Sidebar user panel -->
            <%--<div class="user-panel hide">
                    <div class="pull-left image">
                            <img src="../../dist/img/user2-160x160.jpg" class="img-circle" alt="User Image">
                    </div>
                    <div class="pull-left info">
                            <p>Alexander Pierce</p>
                            <a href="#"><i class="fa fa-circle text-success"></i> Online</a>
                    </div>
            </div>--%>

            <!-- /.search form -->
            <!-- sidebar menu: : style can be found in sidebar.less -->
            <ul class="sidebar-menu">
                <li class="header">MAIN NAVIGATION</li>
                <sec:authorize access="hasRole('ROLE_NIBSS_ADMIN')">
                    <li>
                        <a href="<c:url value="/organizations" /> ">
                            <i class="fa fa-th"></i> <span>Organizations</span>
                        </a>
                    </li>

                </sec:authorize>
                <sec:authorize access="hasRole('ROLE_ADMIN')">
                    <li>
                        <a href="<c:url value="/users" /> ">
                            <i class="fa fa-users"></i> <span>Users</span>
                        </a>
                    </li>
                </sec:authorize>

                <sec:authorize access="hasRole('ROLE_NIBSS_ADMIN')">
                    <li class="treeview">
                        <a href="#">
                            <i class="fa fa-pie-chart"></i>
                            <span>Billing Settings</span>
                            <i class="fa fa-angle-left pull-right"></i>
                        </a>
                        <ul class="treeview-menu">
                            <li><a href="<c:url value="/billers"/>"><i class="fa fa-circle-o"></i> e-BillPay</a></li>
                            <li><a href="<c:url value="/ussd" />"><i class="fa fa-circle-o"></i> USSD Settings</a></li>
                            <li><a href="<c:url value="/centralpay/billing" />"><i class="fa fa-circle-o"></i>
                                CentralPay Accounts</a></li>
                            <li><a href="<c:url value="/corporatelounge/setting" />"><i class="fa fa-circle-o"></i>
                                Corporate Lounge</a></li>
                            <li><a href="<c:url value="/billing" />"><i class="fa fa-circle-o"></i> Custom Billing
                                Report</a></li>
                        </ul>
                    </li>
                </sec:authorize>

                <sec:authorize access="hasAnyRole('ROLE_CL_ADMIN','ROLE_CL_USER')">
                    <li class="treeview">
                        <a href="#">
                            <i class="fa fa-pie-chart"></i>
                            <span>Corporate Lounge</span>
                            <i class="fa fa-angle-left pull-right"></i>
                        </a>
                        <ul class="treeview-menu">
                            <li><a href="<c:url value="/corporatelounge" />"><i class="fa fa-circle-o"></i> Clients</a>
                            </li>
                        </ul>
                    </li>
                </sec:authorize>

                <sec:authorize access="isAuthenticated()">
                    <li class="treeview">
                        <c:url value="/ebillspay" var="ebillsPayUrl"/>
                        <c:url value="/centralpay/card" var="centralpayCardUrl"/>
                        <c:url value="/centralpay/account" var="centralpayAccountsUrl"/>
                        <c:url value="/merchantpay" var="merchantPaymentUrl"/>
                        <c:url value="/billpayment" var="billPaymentUrl"/>

                        <c:set value="${sessionScope.user.organizationInterface}" var="theOrg"/>
                        <a href="#">
                            <i class="fa fa-laptop"></i>
                            <span>Transactions</span>
                            <i class="fa fa-angle-left pull-right"></i>
                        </a>
                        <ul class="treeview-menu">
                            <sec:authorize access="hasAnyRole('ROLE_NIBSS_USER','ROLE_NIBSS_ADMIN')">
                                <li><a href="${ebillsPayUrl}"><i class="fa fa-circle-o"></i> eBillsPay</a></li>
                                <li><a href="${centralpayCardUrl}"><i class="fa fa-circle-o"></i> CentralPay Cards</a>
                                </li>
                                <li><a href="${centralpayAccountsUrl}"><i class="fa fa-circle-o"></i> CentralPay
                                    Accounts</a></li>
                                <li><a href="${merchantPaymentUrl}"><i class="fa fa-circle-o"></i> mCASH</a></li>
                                <li><a href="${billPaymentUrl}"><i class="fa fa-circle-o"></i> USSD Bill Payment</a>
                                </li>
                            </sec:authorize>

                            <sec:authorize access="hasAnyRole('ROLE_BANK_USER','ROLE_BANK_ADMIN')">
                                <li><a href="${ebillsPayUrl}"><i class="fa fa-circle-o"></i> eBillsPay</a></li>
                                <li><a href="${centralpayAccountsUrl}"><i class="fa fa-circle-o"></i> CentralPay
                                    Accounts</a></li>
                                <li><a href="${merchantPaymentUrl}"><i class="fa fa-circle-o"></i> mCASH</a></li>
                                <li><a href="${billPaymentUrl}"><i class="fa fa-circle-o"></i> USSD Bill Payment</a>
                                </li>


                                <c:if test="${not empty theOrg.productCodes and fn:length(theOrg.productCodes) gt 0}">
                                    <c:forEach var="p" items="${theOrg.productCodes}">
                                        <c:choose>

                                            <c:when test="${fn:containsIgnoreCase('CPAY', p)}">
                                                <li><a href="${centralpayCardUrl}?bank=1"><i class="fa fa-circle-o"></i>
                                                    CentralPay Cards (My Merchants)</a></li>
                                                <li><a href="${centralpayAccountsUrl}?bank=1"><i
                                                        class="fa fa-circle-o"></i> CentralPay Accounts (My
                                                    Merchants)</a></li>
                                            </c:when>
                                        </c:choose>
                                    </c:forEach>
                                </c:if>


                            </sec:authorize>
                            <sec:authorize access="hasAnyRole('ROLE_MERCHANT','ROLE_AGGREGATOR')">
                                <c:if test="${not empty theOrg.productCodes and fn:length(theOrg.productCodes) gt 0}">
                                    <c:forEach var="p" items="${theOrg.productCodes}">
                                        <c:choose>
                                            <c:when test="${fn:containsIgnoreCase('EBILLS',p)}">
                                                <li><a href="${ebillsPayUrl}"><i class="fa fa-circle-o"></i>
                                                    eBillsPay</a></li>
                                            </c:when>
                                            <c:when test="${fn:containsIgnoreCase('CPAY', p)}">
                                                <li><a href="${centralpayCardUrl}"><i class="fa fa-circle-o"></i>
                                                    CentralPay Cards</a></li>
                                                <li><a href="${centralpayAccountsUrl}"><i class="fa fa-circle-o"></i>
                                                    CentralPay Accounts</a></li>
                                            </c:when>
                                            <c:when test="${fn:containsIgnoreCase('MPAY', p)}">
                                                <li><a href="${merchantPaymentUrl}"><i class="fa fa-circle-o"></i> mCASH</a>
                                                </li>
                                            </c:when>
                                            <c:when test="${fn:containsIgnoreCase('BPAY', p)}">
                                                <li><a href="${billPaymentUrl}"><i class="fa fa-circle-o"></i> USSD Bill
                                                    Payment </a></li>
                                            </c:when>
                                        </c:choose>
                                    </c:forEach>
                                </c:if>
                            </sec:authorize>
                        </ul>
                    </li>
                </sec:authorize>

                <sec:authorize access="hasAnyRole('ROLE_NIBSS','ROLE_BANK', 'ROLE_AGGREGATOR')">
                    <li class="treeview">
                        <c:url value="/billingreport/ebills" var="ebillsBillingUrl"/>
                        <c:url value="/billingreport/cpay" var="cpayBillingUrl"/>
                        <c:url value="/billingreport/bpay" var="billPayBillingUrl"/>
                        <c:url value="/billingreport/mpay" var="merchantPayBillingUrl"/>

                        <a href="#">
                            <i class="fa fa-laptop"></i>
                            <span>Generated Billing Reports</span>
                            <i class="fa fa-angle-left pull-right"></i>
                        </a>
                        <ul class="treeview-menu">
                            <sec:authorize access="hasAnyRole('ROLE_NIBSS','ROLE_BANK')">
                                <li><a href="${ebillsBillingUrl}"><i class="fa fa-circle-o"></i> eBillsPay</a></li>
                                <li><a href="${cpayBillingUrl}"><i class="fa fa-circle-o"></i> CentralPay Accounts</a>
                                </li>
                                <li><a href="${merchantPayBillingUrl}"><i class="fa fa-circle-o"></i> mCASH</a></li>
                                <li><a href="${billPayBillingUrl}"><i class="fa fa-circle-o"></i> USSD Bill Payment</a>
                                </li>
                            </sec:authorize>

                            <sec:authorize access="hasRole('ROLE_AGGREGATOR')">
                                <c:if test="${not empty theOrg.productCodes and fn:length(theOrg.productCodes) gt 0}">
                                    <c:forEach var="p" items="${theOrg.productCodes}">
                                        <c:choose>
                                            <c:when test="${fn:containsIgnoreCase('EBILLS',p)}">
                                                <li><a href="${ebillsBillingUrl}"><i class="fa fa-circle-o"></i>
                                                    eBillsPay</a></li>
                                            </c:when>
                                            <c:when test="${fn:containsIgnoreCase('CPAY', p)}">
                                                <li><a href="${cpayBillingUrl}"><i class="fa fa-circle-o"></i>
                                                    CentralPay Accounts</a></li>
                                            </c:when>
                                            <c:when test="${fn:containsIgnoreCase('MPAY', p)}">
                                                <li><a href="${merchantPayBillingUrl}"><i class="fa fa-circle-o"></i>
                                                    mCASH</a>
                                                </li>
                                            </c:when>
                                            <c:when test="${fn:containsIgnoreCase('BPAY', p)}">
                                                <li><a href="${billPayBillingUrl}"><i class="fa fa-circle-o"></i> USSD
                                                    Bill Payment </a></li>
                                            </c:when>
                                        </c:choose>
                                    </c:forEach>
                                </c:if>

                            </sec:authorize>


                        </ul>
                    </li>
                </sec:authorize>

                <sec:authorize
                        access="hasAnyRole('ROLE_NIBSS_USER','ROLE_NIBSS_ADMIN','ROLE_BANK_ADMIN','ROLE_BANK_USER')">
                    <li>
                        <a href="<c:url value="/merchantlist" /> ">
                            <i class="fa fa-bars"></i> <span>mCash Merchants</span>
                        </a>
                    </li>
                </sec:authorize>
                <%--	<li class="treeview">
                                <a href="#">
                                        <i class="fa fa-edit"></i> <span>Forms</span>
                                        <i class="fa fa-angle-left pull-right"></i>
                                </a>
                                <ul class="treeview-menu">
                                        <li><a href="../forms/general.html"><i class="fa fa-circle-o"></i> General Elements</a></li>
                                        <li><a href="../forms/advanced.html"><i class="fa fa-circle-o"></i> Advanced Elements</a></li>
                                        <li><a href="../forms/editors.html"><i class="fa fa-circle-o"></i> Editors</a></li>
                                </ul>
                        </li>--%>


            </ul>
        </section>
        <!-- /.sidebar -->
    </aside>

    <!-- =============================================== -->

    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">

        <!-- Content Header (Page header) -->
        <section class="content-header">
            <h1>
                ${contentHeader}
            </h1>
            <%--	<ol class="breadcrumb">
                            <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                            <li><a href="#">Examples</a></li>
                            <li class="active">Blank page</li>
                    </ol>--%>
        </section>

        <!-- Main content -->
        <section class="content">

            <jsp:doBody/>
            <!-- Default box -->
            <%--<div class="box">
                    <div class="box-header with-border">
                            <h3 class="box-title">Title</h3>

                                <div class="box-tools pull-right">
                                        <button type="button" class="btn btn-box-tool" data-widget="collapse" data-toggle="tooltip" title="Collapse">
                                                <i class="fa fa-minus"></i></button>
                                        <button type="button" class="btn btn-box-tool" data-widget="remove" data-toggle="tooltip" title="Remove">
                                                <i class="fa fa-times"></i></button>
                                </div>
                        </div>
                        <div class="box-body">
                                Start creating your amazing application!
                        </div>
                        <!-- /.box-body -->
                        <div class="box-footer">
                                Footer
                        </div>
                        <!-- /.box-footer-->
                </div>--%>
            <!-- /.box -->

        </section>
        <!-- /.content -->
    </div>
    <!-- /.content-wrapper -->

    <footer class="main-footer">
        <div class="pull-right hidden-xs">

        </div>

        <strong>Copyright &copy; 2017 <a href="http://www.nibss-plc.com" target="_blank">Nigeria Inter-Bank Settlement
            System Plc.</a></strong>
    </footer>


</div>
<!-- ./wrapper -->

<!--change password dialog -->
<div class="modal fade" role="dialog" aria-labelledby="myLargeModalLabel" id="changePasswordDialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="gridSystemModalLabel">Change Password</h4>
            </div>
            <form class="form" name="changePasswordForm" id="changePasswordForm" method="post"
                  action="<c:url value="/users/changepassword" /> " autocomplete="off">

                <div class="modal-body">
                    <c:if test="${not sessionScope.user.passwordChanged}">
                        <input type="hidden" id="userPasswordChange"/>
                    </c:if>
                    <div class="alert alert-info">
                        <i class="fa fa-info-circle"></i>
                        <small>
                            Please note that your password should be a minimum of 8 characters and contain each of the
                            following:
                            <ul>
                                <li>An uppercase character</li>
                                <li>A lowercase character</li>
                                <li>A digit</li>
                                <li>A special character</li>
                            </ul>
                        </small>
                    </div>
                    <div class="form-group">
                        <label for="newPassword">New Password*</label>
                        <input type="password" name="newPassword" id="newPassword" class="form-control" required/>
                    </div>

                    <div class="form-group">
                        <label for="confirmPassword">Confirm Password*</label>
                        <input type="password" name="confirmPassword" id="confirmPassword" class="form-control"
                               required/>
                    </div>

                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default btn-flat" data-dismiss="modal"
                            id="btnClosePasswordBox">
                        <i class="fa fa-times" aria-hidden="true"></i> Close
                    </button>
                    <button type="submit" class="btn btn-primary btn-flat" id="btnSaveOrganization">
                        <i class="fa fa-bolt" aria-hidden="true"></i> Update Password
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
<!-- /change password dialog -->


<!-- SlimScroll -->
<script src="${resourceUrl}js/jquery.slimscroll.min.js"></script>
<!-- FastClick -->
<script src="${resourceUrl}js/fastclick.min.js"></script>

<!-- AdminLTE App -->
<script src="${resourceUrl}js/app.min.js"></script>


<!-- custom JavaScript invoked -->
<c:if test="${not empty scripts }">
    <jsp:invoke fragment="scripts"/>
</c:if>
</body>

</html>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<tags:layout contentHeader="${orgLabel}">

    <jsp:attribute name="scripts">
        <c:url value="/resources/" var="resourceUrl"/>
        <c:url value="/search/" var="baseSearchUrl" />
        <c:url value="/organizations/merchants" var="merchantProductUrl" />
        <script type="text/javascript">
            var eBillsSearchUrl = "${baseSearchUrl}ebillspay";
            var cpaySearchUrl = "${baseSearchUrl}centralpay";
            var mpaySearchUrl = "${baseSearchUrl}merchantpay";
            var ussdSearchUrl = "${baseSearchUrl}ussd";

            var productTableUrl = "${merchantProductUrl}";
            var ORGANIZATION_ID = "${organization.id}";
        </script>
        <script type="text/javascript" src="${resourceUrl}js/OrganizationDetail.js"></script>
    </jsp:attribute>

    <jsp:body>

        <c:choose>
            <c:when test="${organization.organizationType == 9}">
                <div class="row">
                    <div class="col-md-12">
                        <div class="box box-primary">
                            <div class="box-header with-border">
                                <h3 class="box-title">Organization Details</h3>
                            </div>
                            <div class="box-body table-responsive">
                                <tags:organizationDetails organization="${organization}" code="${code}" />
                            </div>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-12">
                        <div class="box box-success">
                            <div class="box-header with-border">
                                <h3 class="box-title">Users</h3>
                            </div>
                            <div class="box-body">
                                <tags:users users="${users}" showButtons="true" organizationId="${organization.id}"  isNibss="true"/>
                            </div>
                        </div>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="row">
                    <div class="col-md-12">
                        <div class="nav-tabs-custom">
                            <ul class="nav nav-tabs">
                                <li class="active"><a href="#settings" data-toggle="tab"><i class="fa fa-cogs"></i> Settings</a> </li>
                                <li><a href="#bankAccounts" data-toggle="tab">Bank Accounts</a> </li>
                            </ul>
                            <div class="tab-content">
                                <div class="tab-pane active" id="settings">
                                    <div class="box box-default">
                                        <c:url value="/organizations/" var="baseFormUrl" />
                                        <c:if test="${organizationSetting != null}"> <%-- should never be null--%>
                                            <form role="form" name="settingsForm" id="settingsForm" method="POST" action="${baseFormUrl}${organization.id}/settings">
                                                <div class="box-body">
                                                    <div class="form-group">
                                                        <input type="hidden" name="id" id="id" value="${organizationSetting.id}" />
                                                        <label for="noOfOperators">Number of Operators</label>
                                                        <input type="number" name="noOfOperators" id="noOfOperators" class="form-control"
                                                               value="${organizationSetting.noOfOperators}" required  />
                                                    </div>
                                                    <div class="form-group">
                                                        <label for="noOfAdmins">Number of Admins</label>
                                                        <input type="number" name="noOfAdmins" class="form-control"
                                                               value="${organizationSetting.noOfAdmins}" required id="noOfAdmins" min="2" />
                                                    </div>
                                                    <div class="form-group">
                                                        <label for="ebillspayTransactionDateAllowed">View e-BillsPay Incomplete/Unapproved Transactions<br/>
                                                            <small><i class="fa fa-info-circle"></i> Allow organization see incomplete/unapproved e-BillsPay Transactions</small></label>
                                                        <div class="checkbox">
                                                            <label>
                                                                <input type="checkbox" name="ebillspayTransactionDateAllowed" id="ebillspayTransactionDateAllowed" class="flat-red"
                                                                        <c:if test="${organizationSetting.ebillspayTransactionDateAllowed}">
                                                                            checked="checked"
                                                                        </c:if>
                                                                /> </label>
                                                        </div>

                                                    </div>

                                                </div>
                                                <div class="box-footer">
                                                    <button class="btn btn-primary btn-flat pull-right" id="btnSaveSettings"><i class="fa fa-bookmark"></i>  Save Changes</button>
                                                </div>
                                            </form>
                                        </c:if>
                                    </div>

                                </div> <!-- //settings tab -->
                                <div class="tab-pane" id="bankAccounts">
                                    <div class="box box-default">
                                        <div class="box-body">
                                            <table class="table table-striped table-condensed table-bordered myDataTable">
                                                <thead>
                                                <tr>
                                                    <th>Product</th>
                                                    <th>Account Number</th>
                                                    <th>Account Name</th>
                                                    <th>CBN Bank Code</th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <c:forEach items="${bankAccounts}" var="ba">
                                                    <tr>
                                                        <td>${ba.productName}</td>
                                                        <td>${ba.accountNumber}</td>
                                                        <td>${ba.accountName}</td>
                                                        <td>${ba.bankCode}</td>
                                                    </tr>
                                                </c:forEach>
                                                </tbody>
                                            </table>
                                        </div>
                                        <div class="box-footer">
                                            <button class="btn btn-default btn-flat pull-right" data-toggle="modal" data-target="#newBankAccountDialog">
                                                <i class="fa fa-plus"></i> Add New Bank Account
                                            </button>
                                        </div>
                                    </div>

                                </div> <!-- //bank account tab ends here -->
                            </div>
                        </div>

                    </div>

                </div>
                <div class="row">
                    <div class="col-md-6">
                        <div class="nav-tabs-custom">
                            <ul class="nav nav-tabs">
                                <li class="active"><a href="#tab_product" data-toggle="tab">Products</a> </li>
                                <c:forEach items="${organizationProducts}" var="p">
                                    <c:choose>
                                        <c:when test="${p.code eq 'EBILLS'}">
                                            <li><a href="#tab_ebills" data-toggle="tab">E-BillsPay</a> </li>
                                        </c:when>
                                        <c:when test="${p.code eq 'CPAY'}">
                                            <li><a href="#tab_cpay" data-toggle="tab">CentralPay</a> </li>
                                        </c:when>
                                        <c:when test="${p.code eq 'MPAY'}">
                                            <li><a href="#tab_mpay" data-toggle="tab">mCASH</a> </li>
                                        </c:when>
                                        <c:when test="${p.code eq 'BPAY'}">
                                            <li><a href="#tab_bpay" data-toggle="tab">USSD Bill Payment</a> </li>
                                        </c:when>
                                    </c:choose>
                                </c:forEach>
                            </ul>
                            <div class="tab-content">
                                <div class="tab-pane active" id="tab_product">
                                    <div>
                                        <form role="form" name="productForm" id="productForm" method="post" action="${baseFormUrl}${organization.id}/products">
                                            <c:choose>
                                                <c:when test="${empty organizationProducts}">
                                                    <c:forEach items="${products}" var="p">
                                                        <div class="checkbox">
                                                            <label>
                                                                <input type="checkbox" value="${p.id}"  name="product" class="flat-red" />
                                                                    ${p.name}
                                                            </label>
                                                        </div>
                                                    </c:forEach>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:forEach items="${products}" var="op">
                                                        <div class="checkbox">
                                                            <label>
                                                                <input type="checkbox" value="${op.id}"  name="product" class="flat-red"
                                                                        <c:forEach items="${organizationProducts}" var="p">
                                                                            <c:if test="${op.code eq p.code}">
                                                                                checked
                                                                            </c:if>
                                                                        </c:forEach>

                                                                /> ${op.name}
                                                            </label>
                                                        </div>
                                                    </c:forEach>
                                                </c:otherwise>
                                            </c:choose>
                                            <div style="text-align: right;">
                                                <button class="btn btn-success btn-flat" type="submit"><i class="fa fa-bookmark"></i> Save Changes</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                                <c:forEach items="${organizationProducts}" var="p">
                                    <c:choose>
                                        <c:when test="${p.code eq 'EBILLS'}">
                                            <div class="tab-pane" id="tab_ebills">
                                                <div class="box box-default">
                                                    <div class="box-header with-border">
                                                        <h3 class="box-title">Billers</h3>
                                                    </div>
                                                    <form role="form" name="ebillsPayBillerForm" id="ebillsPayBillerForm" method="POST" action="${baseFormUrl}${organization.id}/ebillspay">
                                                        <div class="box-body">
                                                            <div>
                                                                <table class="table table-condensed table-bordered" id="ebillsTable">
                                                                    <thead>
                                                                    <tr>
                                                                        <th>Biller</th>
                                                                    </tr>
                                                                    </thead>
                                                                </table>
                                                            </div>
                                                            <div>
                                                                <c:choose>
                                                                    <c:when test="${organization.organizationType == 1}">
                                                                        <div class="form-group">
                                                                            <label for="billers">Add New/Change Biller</label>
                                                                        </div>
                                                                        <select class="form-control" name="billers" style="width: 100%;" id="ebillspayBillers">
                                                                        </select>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <div class="form-group">
                                                                            <label for="billers">Add New Biller(s)</label>
                                                                        </div>
                                                                        <select class="form-control" name="billers" multiple="multiple" style="width: 100%;" id="ebillspayBillers">
                                                                        </select>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </div>
                                                        </div>
                                                        <div class="box-footer">
                                                            <sec:csrfInput/>
                                                            <button class="btn btn-info btn-flat pull-right" type="submit">
                                                                <i class="fa fa-bookmark"></i> Save Changes
                                                            </button>
                                                        </div>
                                                    </form>
                                                </div>

                                            </div>
                                        </c:when>
                                        <c:when test="${p.code eq 'CPAY'}">
                                            <div class="tab-pane" id="tab_cpay">
                                                <div class="box box-primary">
                                                    <div class="box-header with-border">
                                                        <h3 class="box-title">Merchants</h3>
                                                    </div>
                                                    <form role="form" name="centralPayMerchantForm" id="centralPayMerchantForm" method="POST" action="${baseFormUrl}${organization.id}/cpay">
                                                        <div class="box-body">
                                                            <table class="table table-condensed table-bordered" id="cpayTable">
                                                                <thead>
                                                                <tr>
                                                                    <th style="width:30%">Merchant Code</th>
                                                                    <th>Merchant Name</th>
                                                                </tr>
                                                                </thead>
                                                            </table>

                                                            <div>
                                                                <c:choose>
                                                                    <c:when test="${organization.organizationType == 1}">
                                                                        <div class="form-group">
                                                                            <label for="merchants">Add New/Change Merchant</label>
                                                                        </div>
                                                                        <select class="form-control" name="merchants" style="width: 100%;" id="cpayMerchants">
                                                                        </select>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <div class="form-group">
                                                                            <label for="merchants">Add New Merchant(s)</label>
                                                                        </div>
                                                                        <select class="form-control mySelect2" name="merchants" multiple="multiple" style="width: 100%;" id="cpayMerchants">
                                                                        </select>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </div>
                                                        </div>
                                                        <div class="box-footer">
                                                            <sec:csrfInput/>
                                                            <button class="btn btn-warning btn-flat pull-right" type="submit">
                                                                <i class="fa fa-bookmark"></i> Save Changes
                                                            </button>
                                                        </div>
                                                    </form>
                                                </div>
                                            </div>
                                        </c:when>

                                        <c:when test="${p.code eq 'MPAY'}">
                                            <div class="tab-pane" id="tab_mpay">
                                                <div class="box box-default">
                                                    <div class="box-header with-border">
                                                        <h3 class="box-title">mCASH Merchants</h3>
                                                    </div>
                                                    <form role="form" name="merchantPayForm" id="merchantPayForm" method="POST" action="${baseFormUrl}${organization.id}/merchantpay">
                                                        <div class="box-body">
                                                            <div>
                                                                <table class="table table-condensed table-bordered" id="mcashTable">
                                                                    <thead>
                                                                    <tr>
                                                                        <th>Code</th>
                                                                        <th>Name</th>
                                                                    </tr>
                                                                    </thead>
                                                                </table>
                                                            </div>

                                                            <div>
                                                                <c:choose>
                                                                    <c:when test="${organization.organizationType == 1}">
                                                                        <div class="form-group">
                                                                            <label for="merchants">Add New Merchant(s)</label>
                                                                        </div>
                                                                        <select class="form-control" name="merchants" style="width: 100%;" id="merchantPayBillers" multiple="multiple">
                                                                        </select>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <div class="form-group">
                                                                            <label for="merchants">Add New Merchant(s)</label>
                                                                        </div>
                                                                        <select class="form-control" name="merchants" multiple="multiple" style="width: 100%;" id="merchantPayBillers">
                                                                        </select>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </div>
                                                        </div>
                                                        <div class="box-footer">
                                                            <sec:csrfInput/>
                                                            <button class="btn btn-info btn-flat pull-right" type="submit">
                                                                <i class="fa fa-bookmark"></i> Save Changes
                                                            </button>
                                                        </div>
                                                    </form>
                                                </div>
                                            </div>
                                        </c:when>

                                        <c:when test="${p.code eq 'BPAY'}">
                                            <div class="tab-pane" id="tab_bpay">
                                                <div class="box box-default">
                                                    <div class="box-header with-border">
                                                        <h3 class="box-title">USSD Bill Payment Merchants</h3>
                                                    </div>
                                                    <form role="form" name="ussdBillerForm" id="ussdBillerForm" method="POST" action="${baseFormUrl}${organization.id}/ussd">
                                                        <div class="box-body">
                                                            <div>
                                                                <table class="table table-condensed table-bordered" id="ussdTable">
                                                                    <thead>
                                                                    <tr>
                                                                        <th>Code</th>
                                                                        <th>Name</th>
                                                                    </tr>
                                                                    </thead>
                                                                </table>
                                                            </div>
                                                            <div>
                                                                <c:choose>
                                                                    <c:when test="${organization.organizationType == 1}">
                                                                        <div class="form-group">
                                                                            <label for="ussdBillers">Add New/Change Biller</label>
                                                                        </div>
                                                                        <select class="form-control" name="ussd" style="width: 100%;" id="ussdBillers">
                                                                        </select>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <div class="form-group">
                                                                            <label for="ussdBillers">Add New Biller(s)</label>
                                                                        </div>
                                                                        <select class="form-control" name="ussd" multiple="multiple" style="width: 100%;" id="ussdBillers">
                                                                        </select>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </div>
                                                        </div>
                                                        <div class="box-footer">
                                                            <sec:csrfInput/>
                                                            <button class="btn btn-info btn-flat pull-right" type="submit">
                                                                <i class="fa fa-bookmark"></i> Save Changes
                                                            </button>
                                                        </div>
                                                    </form>
                                                </div>
                                            </div>
                                        </c:when>
                                    </c:choose>
                                </c:forEach>
                            </div>
                        </div>

                    </div>
                    <div class="col-md-6">
                        <div class="box box-success">
                            <div class="box-header with-border">
                                <h3 class="box-title">Users</h3>
                            </div>
                            <div class="box-body">
                                <tags:users users="${users}" showButtons="true" organizationId="${organization.id}"/>
                            </div>
                        </div>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

        <!-- dialog for maintaining bank account -->
        <div class="modal fade" role="dialog" aria-labelledby="myLargeModalLabel" id="newBankAccountDialog">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="gridSystemModalLabel">Add New Bank Account</h4>
                    </div>
                    <c:url value="/organizations/" var="orgUrl" />
                    <form class="form" name="bankAccountForm" id="bankAccountForm" method="post" action="${orgUrl}/${organization.id}/bankaccount" autocomplete="off">

                        <div class="modal-body">
                            <div class="form-group">
                                <label for="accountName">Account Name*</label>
                                <input type="text" name="accountName" id="accountName"  class="form-control"required />
                            </div>

                            <div class="form-group">
                                <label for="accountNumber">Account Number*</label>
                                <input type="text" name="accountNumber" id="accountNumber"  class="form-control" maxlength="10" />
                            </div>

                            <div class="form-group">
                                <label for="bankCode">CBN Bank Code*</label>
                                <input type="text" name="bankCode" id="bankCode" class="form-control" maxlength="3" />
                            </div>

                            <div class="form-group">
                                <label for="product">Product*</label>
                                <c:choose>
                                    <c:when test="${organization.organizationType eq 3}">
                                        <select name="product" required class="form-control" id="product">
                                            <c:forEach items="${products}" var="p">
                                                <option value="${p.id}">${p.name}</option>
                                            </c:forEach>
                                        </select>
                                    </c:when>
                                    <c:otherwise>
                                        <select name="product" required class="form-control" id="product">
                                            <c:forEach items="${organizationProducts}" var="p">
                                                <option value="${p.id}">${p.name}</option>
                                            </c:forEach>
                                        </select>
                                    </c:otherwise>
                                </c:choose>

                            </div>

                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-info btn-flat" data-dismiss="modal">
                                <i class="fa fa-times" aria-hidden="true"></i> Close</button>
                            <button type="submit" class="btn btn-primary btn-flat" id="btnSaveOrganization">
                                <i class="fa fa-bookmark" aria-hidden="true"></i> Save</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </jsp:body>
</tags:layout>
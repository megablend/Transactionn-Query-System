<%@tag language="java" pageEncoding="ISO-8859-1"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@attribute name="headers" required="true" type="java.util.Collection" description="values for table headers" %>
<%@attribute name="productCode" required="true" description="The product being rendered: ebills, cpay,tsa..." %>
<%@attribute name="showDateInitiated" type="java.lang.Boolean" required="false" description="indicates if user can search by trxn date" %>

<%@attribute name="searchable" type="java.lang.Boolean" required="false" description="indicates if a select box should be shown"  %>


<div class="row"> <!-- transactions div -->
    <div class="col-md-12">
    <div class="box box-primary">
        <div class="box-header with-border">
            <h3 class="box-title">
            <c:choose>
                <c:when test="${fn:toUpperCase(productCode) eq 'MERCHANTS'}">
                    Merchants
                </c:when>
                <c:otherwise> Transactions</c:otherwise>
            </c:choose>
            </h3>
        </div>
        <div class="box-body">
            <div class="table-responsive">
                <!--row for billers here -->
                <!--row for date search here. also includes if the user is allowed to search by date initiated-->
                <script type="text/javascript">
                    var DATA_URL = null;
                    var SEARCH_URL = null;
                    var MERCHANTS = false;
                    <c:choose>
                    <c:when test="${fn:toUpperCase(productCode) eq 'EBILLS'}">
                     DATA_URL = "<c:url  value="/ebillspay/transactions" />";
                    SEARCH_URL = "<c:url value="/search/ebillspay" />";
                    <c:url value="/ebillspay/download" var="downloadUrl" />
                    </c:when>
                    <c:when test="${fn:toUpperCase(productCode) eq 'CPAYCARD'}">
                    DATA_URL = "<c:url value="/centralpay/card/transactions"  />";
                    SEARCH_URL = "<c:url value="/search/centralpay" />";
                    <c:url value="/centralpay/card/download" var="downloadUrl" />
                    </c:when>
                    <c:when test="${fn:toUpperCase(productCode) eq 'CPAYACCOUNT'}">
                    DATA_URL = "<c:url value="/centralpay/account/transactions"  />";
                    SEARCH_URL = "<c:url value="/search/centralpay" />";
                    <c:url value="/centralpay/account/download" var="downloadUrl" />
                    </c:when>
                    <c:when test="${fn:toUpperCase(productCode) eq 'MPAY'}">
                    DATA_URL = "<c:url  value="/merchantpay/transactions" />";
                    SEARCH_URL = "<c:url value="/search/merchantpay" />";
                    <c:url value="/merchantpay/download" var="downloadUrl" />
                    </c:when>
                    <c:when test="${fn:toUpperCase(productCode) eq 'BPAY'}">
                    DATA_URL = "<c:url  value="/billpayment/transactions" />";
                    SEARCH_URL = "<c:url value="/search/ussd" />";
                    <c:url value="/billpayment/download" var="downloadUrl" />;
                    </c:when>
                    <c:when test="${fn:toUpperCase(productCode) eq 'MERCHANTS'}">
                    DATA_URL = "<c:url  value="/merchantlist" />";
                    SEARCH_URL = "<c:url value="/merchantlist/search" />";
                    MERCHANTS = true;
                    <c:url value="/merchantlist/download" var="downloadUrl" />
                    </c:when>
                    </c:choose>

                    <c:choose>
                    <c:when test="${not empty requestScope.bank}">
                    var BANK_CALL = true;
                    </c:when>
                    <c:otherwise>
                    var BANK_CALL = false;
                    </c:otherwise>
                    </c:choose>

                </script>
                <form role="form">
                    <c:if test="${searchable}">
                        <div class="form-group">
                            <label for="searchable">Merchant<br/>
                            <small><i class="fa fa-info"></i> Type <strong>ALL</strong> to view transactions from all merchants</small>
                            </label>
                                <select id="searchable" class="form-control" style="width:100%"></select>
                        </div>
                    </c:if>

                    <div class="form-group">
                        <label>Date and Time Range:</label>

                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-clock-o"></i>
                            </div>
                            <input type="text" class="form-control pull-right" id="dateField" readonly>
                        </div>
                    </div>

                    <c:if test="${showDateInitiated}">
                        <div class="form-group">
                            <label for="searchDate">Filter By</label>
                            <select name="searchDate" id="searchDate" class="form-control">
                                <option value="dateInitiated">Date Initiated</option>
                                <option value="dateApproved">Date Approved</option>
                            </select>
                        </div>
                    </c:if>
                </form>
                <table class="table table-bordered table-striped table-responsive" id="transactionTable">
                    <thead>
                    <tr>
                        <c:forEach var="h" items="${headers}">
                            <th>${h}</th>
                        </c:forEach>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</div>
</div>

<div class="row"> <!--summary div -->
    <div class="col-md-12">
    <div class="box-info box"  style="display:none" id="summaryHolder">
        <div class="box-header with-border">
            <h3 class="box-title">Summary</h3>
        </div>
        <div class="box-body">
            <div class="summaryBox table-responsive" style="display: none" id="summaryBox">

            </div>

        </div>
    </div>
    </div>
    <c:choose>
        <c:when test="${not empty requestScope.bank}">
            <a href="${downloadUrl}?bank=1&type=" id="transactionDownloadLink" target="_blank" class="hidden">download</a>
        </c:when>
        <c:otherwise>
            <a href="${downloadUrl}?type=" id="transactionDownloadLink" target="_blank" class="hidden">download</a>
        </c:otherwise>
    </c:choose>

</div>

<script type="text/javascript" src="<c:url value="/resources/js/tableTransactions.js" />"></script>

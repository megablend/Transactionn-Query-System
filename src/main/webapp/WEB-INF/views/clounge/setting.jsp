<%@taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<layout:layout contentHeader="Coporate Lounge Billing Configuration"  title="CoporateLounge Billing Config">

    <jsp:attribute name="scripts">
        <c:url var="resourceUrl" value="/resources/" />
        <script type="text/javascript" src="${resourceUrl}js/clSetting.js"></script>
    </jsp:attribute>
    <jsp:body>
        <form role="form" id="settingForm" method="post" action="<c:url value="/corporatelounge/setting" /> ">
            <div class="box box-default">
                <div class="box-body">
                    <div class="form-group">
                        <input type="hidden" name="id" id="id" value="${setting.id}" />
                        <label for="annualFee">Annual Fee</label>
                        <input type="number" name="annualFee" id="annualFee" required class="form-control"  value="${setting.annualFee}"  min="0" max="10000000" />
                    </div>
                    <div class="form-group">
                        <label for="perTransactionFee">Per-Transaction Fee</label>
                        <input type="number" name="perTransactionFee" id="perTransactionFee" required class="form-control" value="${setting.perTransactionFee}"  min="0" max="10000000" />
                    </div>

                    <div class="form-group">
                        <fmt:formatNumber value="${setting.nibssShare}" minFractionDigits="2" maxFractionDigits="4" var="nibssShare" />
                        <label for="nibssShare">NIBSS Share</label>
                        <input type="number" name="nibssShare" id="nibssShare" required class="form-control" value="${nibssShare}"  min="0" max="10000000" />
                    </div>

                    <div class="form-group">
                        <fmt:formatNumber value="${setting.bankShare}" minFractionDigits="2" maxFractionDigits="4" var="bankShare" />
                        <label for="bankShare">Bank Share</label>
                        <input type="number" name="bankShare" id="bankShare" required class="form-control"  value="${bankShare}"  min="0" max="10000000" />
                    </div>

                </div>
            </div>
            <div class="box-footer">
                <button type="submit" class="btn btn-primary btn-flat pull-right">
                    <i class="fa fa-bookmark"></i> Save Corporate Lounge Billing Config
                </button>
            </div>
        </form>
    </jsp:body>
</layout:layout>
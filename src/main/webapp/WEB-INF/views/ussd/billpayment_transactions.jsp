<%@taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<tag:layout contentHeader="USSD Bill Payment Transactions" title="USSD Bill Payment Transactions">
    <jsp:attribute name="scripts">

    </jsp:attribute>

    <jsp:body>
        <tag:transactions headers="${tableHeader}" productCode="BPAY" searchable="${searchable}" />
    </jsp:body>
</tag:layout>
<%@taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<tag:layout contentHeader="CentralPay Transaction : CARDS" title="CentralPay Card Transactions">
    <jsp:attribute name="scripts">

    </jsp:attribute>

    <jsp:body>
        <tag:transactions headers="${tableHeader}" productCode="CPAYCARD" searchable="${searchable}" />
    </jsp:body>
</tag:layout>
<%@taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<tag:layout contentHeader="E-BillsPay Transactions" title="EbillsPay Transactions">
    <jsp:attribute name="scripts">

    </jsp:attribute>

    <jsp:body>
        <tag:transactions headers="${tableHeader}" productCode="EBILLS" showDateInitiated="${showDateInitiated}"  searchable="${searchable}" />
    </jsp:body>
</tag:layout>
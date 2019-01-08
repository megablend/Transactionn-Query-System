<%@taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<tag:layout contentHeader="mCASH Transactions" title="mCASH Transactions">
    <jsp:attribute name="scripts">

    </jsp:attribute>

    <jsp:body>
        <tag:transactions headers="${tableHeader}" productCode="MPAY" searchable="${searchable}" />
    </jsp:body>
</tag:layout>
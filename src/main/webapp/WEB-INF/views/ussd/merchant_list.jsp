<%@taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<tag:layout contentHeader="mCASH Merchants" title="mCASH Merchants">
    <jsp:attribute name="scripts">

    </jsp:attribute>

    <jsp:body>
        <tag:transactions headers="${tableHeader}" productCode="MERCHANTS" searchable="false" />
    </jsp:body>
</tag:layout>
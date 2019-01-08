<%@tag language="java" pageEncoding="ISO-8859-1"%>
<%@attribute name="organization" description="the organization for which we want to render details"  required="true"  type="com.nibss.tqs.core.entities.Organization" %>
<%@attribute name="code" required="false" description="The code, if it is a bank or an aggregator" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table class="table table-bordered">
    <tr>
        <td style="width:20%">Name</td>
        <td>${organization.name}</td>
    </tr>
    <c:if test="${not empty code}">
        <tr>
            <td>Code</td>
            <td>${code}</td>
        </tr>
    </c:if>
    <tr>
        <td>Type</td>
        <td>
            <c:choose>
                <c:when test="${organization.organizationType == 9}">
                    NIBSS
                </c:when>
                <c:when test="${organization.organizationType == 3}">
                    Bank
                </c:when>
                <c:when test="${organization.organizationType == 2}">
                    Aggregator
                </c:when>
                <c:when test="${organization.organizationType == 1}">
                    Merchant
                </c:when>
            </c:choose>

        </td>
    </tr>
</table>
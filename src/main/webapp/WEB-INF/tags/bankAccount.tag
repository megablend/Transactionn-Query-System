<%@tag language="java" pageEncoding="ISO-8859-1"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@attribute name="bankAccount" required="false" type="com.nibss.tqs.core.entities.BankAccount" %>

<c:choose>

    <c:when test="${not empty bankAccount}">
        <div class="form-group">
            <label for="accountNumber">Account Number</label>
            <input type="text" name="accountNumber" id="accountNumber" class="form-control" value="${bankAccount.accountNumber}"/>
        </div>
        <div class="form-group">
            <label for="accountName">Account Name</label>
            <input type="text" name="accountName" id="accountName" class="form-control"  value="${bankAccount.accountName}"/>
        </div>
        <div class="form-group">
            <label for="bankCode">CBN Bank Code</label>
            <input type="text" name="bankCode" id="bankCode" class="form-control" value="${bankAccount.bankCode}" />
        </div>
    </c:when>

    <c:otherwise>
        <div class="form-group">
            <label for="accountNumber">Account Number</label>
            <input type="text" name="accountNumber" id="accountNumber" class="form-control"/>
        </div>
        <div class="form-group">
            <label for="accountName">Account Name</label>
            <input type="text" name="accountName" id="accountName" class="form-control" />
        </div>
        <div class="form-group">
            <label for="bankCode">CBN Bank Code</label>
            <input type="text" name="bankCode" id="bankCode" class="form-control" />
        </div>
    </c:otherwise>

</c:choose>

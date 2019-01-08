<%--
  Created by IntelliJ IDEA.
  User: eoriarewo
  Date: 9/19/2016
  Time: 2:15 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>User Entered Params</title>
</head>
<body>
<table style="width:100%">
    <thead>
    <tr>
        <th>Name</th>
        <th>Value</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="u" items="${userParams}">
        <tr>
            <td>${u.name}</td>
            <td>${u.value}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>

<%@taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/fmt" %>

<tag:layout contentHeader="${product}"  title="Billing Reports">
    <jsp:attribute name="scripts"></jsp:attribute>

    <jsp:body>
        <div class="row">
            <div class="col-lg-12">
                <div class="box box-primary">
                    <div class="box-body table-responsive">
                        <c:url value="/billingreport/download" var="downloadUrl" />

                        <table class="myDataTable table-condensed table-striped table table-bordered">
                            <thead>
                            <tr>
                                <th>File Name</th>
                                <th>Date Created</th>
                                <th>Type</th>
                                <th>&nbsp;</th>
                            </tr>
                            </thead>
                            <tbody>

                            <c:forEach items="${reportFiles}" var="f">
                                <tr>
                                    <td>${f.fileName}</td>
                                    <td> <fn:formatDate value="${f.dateCreated}" pattern="dd-MMM-yyyy HH:mm:ss" /></td>
                                    <td>${f.fileType}</td>
                                    <td>
                                        <a href="${downloadUrl}?name=${f.fileName}&product=${productType}" target="_blank" class="btn btn-flat btn-sm btn-primary" title="Download">
                                        <i class="fa fa-download"></i>
                                    </a>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>

                    </div>
                </div>
            </div>
        </div>
    </jsp:body>
</tag:layout>

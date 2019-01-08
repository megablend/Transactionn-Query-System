<%@taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<c:url value="/resources/" var="resourceUrl"/>
<tag:layout title="Client Details" contentHeader="Corporate Lounge Client :: ${organization.name}">
    <jsp:attribute name="scripts">
        <script type="text/javascript">
            var ACCOUNT_LINK = "<c:url value="/corporatelounge/accounts/updatestatus" />";
            var EMAIL_LINK = "<c:url value="/corporatelounge/accounts/email" />";
            var DATE_LINK = "<c:url value="/corporatelounge/accounts/dates" />";
        </script>
        <script type="text/javascript" src="${resourceUrl}js/clClientDetail.js"></script>
    </jsp:attribute>

    <jsp:body>

        <div class="box box-primary">
            <div class="box-header with-border">
                <h3 class="box-title">Accounts</h3>
            </div>
            <div class="box-body table-responsive">
                <table class="table table-condensed table-striped myDataTable table-bordered">
                    <thead>
                    <tr>
                        <th>Account Name</th>
                        <th>Account Number</th>
                        <th>Bank</th>
                        <th>Account Status</th>
                        <th>Email</th>
                        <th>Payment Mode</th>
                        <th>Date Active</th>
                        <th>Expiry Date</th>
                        <th style="text-align: left;padding: 0 0">
                            <input type="checkbox" id="selAll" />
                                <sec:authorize access="hasRole('ROLE_CL_ADMIN')">
                                    <button class="btn btn-sm btn-flat btn-info" title="Update Account Statuses"
                                     data-toggle="modal" data-target="#statusChangeDialog">
                                        <i class="fa fa-arrow-circle-o-up"></i>
                                    </button>

                                    <button class="btn btn-sm btn-flat btn-success" title="Update Account Email"
                                    data-toggle="modal" data-target="#emailDialog">
                                        <i class="fa fa-empire"></i>
                                    </button>

                                    <%--<button class="btn btn-sm btn-flat btn-danger"--%>
                                    <%--title="Update Expiry date to a year from now. Also set Date Active to today and Account Status to APPROVED"--%>
                                    <%--id="btnDates"--%>
                                    <%-->--%>
                                    <%--<i class="fa fa-arrow-circle-o-up"></i>--%>
                                    <%--</button>--%>
                                </sec:authorize>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${accounts}" var="acc">
                        <tr>
                            <td>${acc.accountName}</td>
                            <td>${acc.accountNumber}</td>
                            <td>${acc.bankName}</td>
                            <td>${acc.accountStatus}</td>
                            <td>${acc.email}</td>
                            <td>${acc.paymentMode}</td>
                            <td>
                                <fmt:formatDate value="${acc.dateActive}" pattern="dd-MMM-yyyy" />
                            </td>
                            <td>
                                <fmt:formatDate value="${acc.expiryDate}" pattern="dd-MMM-yyyy" />
                            </td>
                            <td align="right">
                                <input type="checkbox" class="accts" id="${acc.id}" />
                            </td>
                        </tr>

                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="modal fade" role="dialog" aria-labelledby="myLargeModalLabel" id="statusChangeDialog">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="gridSystemModalLabel">Select New Status for Selected Account(s)</h4>
                    </div>
                    <div class="modal-body">
                        <div class="form-group">
                            <label for="accountStatus">Status</label>
                                <select name="accountStatus" id="accountStatus" class="form-control">
                                    <option value=""> --SELECT-- </option>
                                    <option value="APPROVED">APPROVED</option>
                                    <option value="DECLINED">DECLINED</option>
                                    <option value="PENDING">PENDING</option>
                                    <option value="EXPIRED">EXPIRED</option>
                                </select>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn btn-default btn-flat" data-dismiss="modal">
                            <i class="fa fa-times" aria-hidden="true"></i> Close
                        </button>
                        <button type="submit" class="btn btn-primary btn-flat" id="btnUpdateStatus">
                            <i class="fa fa-bookmark" aria-hidden="true"></i> Save
                        </button>
                    </div>


                </div>
            </div>
        </div>

        <div class="modal fade" role="dialog" aria-labelledby="myLargeModalLabel" id="emailDialog">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title">Update Email for Selected Account(s)</h4>
                    </div>
                    <div class="modal-body">
                        <div class="form-group">
                            <label for="email">Email</label>
                            <input type="email" name="email" id="email" class="form-control" />
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn btn-default btn-flat" data-dismiss="modal">
                            <i class="fa fa-times" aria-hidden="true"></i> Close
                        </button>
                        <button type="submit" class="btn btn-primary btn-flat" id="btnUpdateEmail">
                            <i class="fa fa-bookmark" aria-hidden="true"></i> Save
                        </button>
                    </div>

                </div>
            </div>
        </div>


    </jsp:body>
</tag:layout>
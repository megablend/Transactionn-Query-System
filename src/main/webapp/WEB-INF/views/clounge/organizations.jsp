<%@taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="sec"	uri="http://www.springframework.org/security/tags"%>

<c:url value="/resources/" var="resourceUrl"/>
<c:url var="detailUrl" value="/corporatelounge/"/>

<tag:layout contentHeader="CorporateLounge Clients" title="Corporate Lounge Clients">


    <jsp:attribute name="scripts">
 <script type="text/javascript" src="${resourceUrl}js/clClient.js"></script>
    </jsp:attribute>

    <jsp:body>
        <div class="box box-primary">
            <div class="box-header with-border">
                <h3 class="box-title">Corporate Lounge Clients</h3>
                <sec:authorize access="hasRole('ROLE_CL_ADMIN')">
                    <button class="btn btn-primary btn-flat pull-right" title="Add New Organization" type="button"
                            data-toggle="modal" data-target=".organizationCreationDialog">
                        <i class="fa fa-plus-square" aria-hidden="true"></i>
                    </button>
                </sec:authorize>

            </div>
            <div class="table-responsive box-body">
                <table class="table table-condensed table-bordered table-striped myDataTable">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Institution Code</th>
                        <th>Secret Key</th>
                        <th>IV Key</th>
                        <th>Email</th>
                        <th>Max. Accounts Per Request</th>
                        <th>Debit Account Name</th>
                        <th>Debit Account Number</th>
                        <th>Debit Bank Code</th>
                        <th>&nbsp;</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${organizations}" var="org">
                        <tr>

                            <td>${org.name}</td>
                            <td>${org.institutionCode}</td>
                            <td>${org.secretKey}</td>
                            <td>${org.ivKey}</td>
                            <td>${org.emails}</td>
                            <td>${org.maxRequestSize}</td>
                            <td>${org.debitAccountName}</td>
                            <td>${org.debitAccountNumber}</td>
                            <td>${org.debitBankCode}</td>
                            <td><a href="${detailUrl}${org.id}">Details</a></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>

            </div>
        </div>

        <%--New org dialog--%>
        <div class="modal fade organizationCreationDialog" role="dialog" aria-labelledby="myLargeModalLabel">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="gridSystemModalLabel">Add New Corporate Lounge Client</h4>
                    </div>
                    <form class="form" name="clientForm" id="clientForm" method="post"
                          action="<c:url value="/corporatelounge" /> " autocomplete="off">

                        <div class="modal-body">
                            <div class="form-group">
                                <label for="name">Name*</label>
                                <input type="text" name="name" id="name" class="form-control" required/>
                            </div>

                            <div class="form-group">
                                <label for="emails">Emails* <br/>
                                    <small><i class="fa fa-info-circle"></i>
                                        This can be a comma or semi-colon separated list of valid emails
                                    </small>
                                </label>
                                <input type="text" name="emails" class="form-control" id="emails"/>
                            </div>

                            <div class="form-group">
                                <label for="institutionCode">Institution Code*</label>
                                <input type="text" name="institutionCode" id="institutionCode" class="form-control"
                                       required/>
                            </div>

                            <div class="form-group">
                                <label for="maxRequestSize">Max. Request Size*<br/>
                                    <small><i class="fa fa-info-circle"></i>
                                        The maximum number of accounts for which client can view balances at a time
                                    </small>
                                </label>
                                <input type="number" name="maxRequestSize" id="maxRequestSize" class="form-control"
                                       min="1" max="10" step="1"/>
                            </div>

                            <div class="form-group">
                                <label for="debitAccountName">Debit Account Name
                                <br />
                                    <small><i class="fa fa-info-circle"></i>
                                    The account name of the bank account that debits will be passed into
                                    </small>
                                </label>
                                <input type="text" name="debitAccountName" id="debitAccountName" maxlength="100" class="form-control" />
                            </div>

                            <div class="form-group">
                                <label for="debitAccountNumber">Debit Account Number
                                    <br />
                                    <small><i class="fa fa-info-circle"></i>
                                        The account number of the bank account that debits will be passed into
                                    </small>
                                </label>
                                <input type="text" name="debitAccountNumber" id="debitAccountNumber" maxlength="10" class="form-control" />
                            </div>

                            <div class="form-group">
                                <label for="debitAccountNumber">Debit Bank Code
                                    <br />
                                    <small><i class="fa fa-info-circle"></i>
                                        The CBN Bank Code for the above debit account details
                                    </small>
                                </label>
                                <input type="text" name="debitBankCode" id="debitBankCode" maxlength="3" class="form-control" />
                            </div>


                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default btn-flat" data-dismiss="modal">
                                <i class="fa fa-times" aria-hidden="true"></i> Close
                            </button>
                            <button type="submit" class="btn btn-primary btn-flat" id="btnSaveOrganization">
                                <i class="fa fa-bookmark" aria-hidden="true"></i> Save
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </jsp:body>
</tag:layout>

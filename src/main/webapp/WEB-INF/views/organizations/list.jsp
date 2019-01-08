<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:url value="/resources/" var="resourceUrl" />
<tags:layout>
    <jsp:attribute name="contentHeader">Organizations</jsp:attribute>


    <jsp:attribute name="scripts">
        <script type="text/javascript">
            var TABLE_URL = "<c:url value="/organizations/list" /> ";
            var DETAIL_URL = "<c:url value="/organizations/" /> ";
        </script>
        <script type="text/javascript" src="${resourceUrl}js/Organization.js"></script>
    </jsp:attribute>

    <jsp:body>

        <div class="box box-primary">
            <div class="box-header with-border">
                <h3 class="box-title">Organizations</h3>
                <button class="btn btn-primary btn-flat pull-right" title="Add New Organization" type="button" data-toggle="modal" data-target=".organizationCreationDialog">
                    <i class="fa fa-plus-square" aria-hidden="true"></i>
                </button>
            </div>
            <div class="box-body table-responsive">
                <c:url var="orgDetails" value="/organizations/" />
                <table class="table table-condensed table-bordered table-striped" id="orgTable">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Type</th>
                        <th>&nbsp;</th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>

        <div class="modal fade organizationCreationDialog" role="dialog" aria-labelledby="myLargeModalLabel">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="gridSystemModalLabel">Add New Organization</h4>
                    </div>
                    <form class="form" name="organizationForm" id="organizationForm" method="post" action="<c:url value="/organizations" /> " autocomplete="off">

                        <div class="modal-body">
                            <div class="form-group">
                                <label for="name">Name*</label>
                                <input type="text" name="name" id="name"  class="form-control"required />
                            </div>
                            <div class="form-group">
                                <label for="organizationType">Type*</label>
                                <select name="organizationType" class="form-control" required id="organizationType">
                                    <option value="">--SELECT--</option>
                                    <option value="1">Merchant/Biller</option>
                                    <option value="2">Aggregator</option>
                                    <option value="3">Bank</option>
                                </select>
                            </div>

                            <div class="form-group bank aggregator" style="display: none;">
                                <label for="code">Code <br/><small><i class="fa fa-info-circle"></i>
                                    CBN Bank Code for banks  and RC Number for Aggregators</small></label>
                                <input type="text" name="code" class="form-control" id="code" />
                            </div>

                            <div class="form-group bank" style="display: none;">
                                <label for="nipCode">NIP Code <br/><small><i class="fa fa-info-circle"></i> Needed for banks only</small></label>
                                <input type="text" name="nipCode" class="form-control" id="nipCode" />
                            </div>


                            <div class="form-group">
                                <label>Products* <br/><small><i class="fa fa-info-circle"></i> Select where necessary</small>
                                <small class="prodBank" style="display: none"><br/>If bank is a merchant/biller introducer, select related product</small></label>

                                <c:forEach items="${products}" var="p">
                                    <div class="checkbox">
                                        <label>
                                            <input type="checkbox" value="${p.id}"  name="product" class="flat-red"/>
                                                ${p.name}
                                        </label>
                                    </div>

                                </c:forEach>
                            </div>

                            <div class="form-group">
                                <label for="noOfOperators">No. of Operator Users*</label>
                                <input type="number" name="noOfOperators" id="noOfOperators"  class="form-control" min="2"/>
                            </div>

                            <div class="form-group">
                                <label for="noOfAdmins">No. of Admin Users*</label>
                                <input type="number" name="noOfAdmins" id="noOfAdmins" class="form-control" min="1" />
                            </div>
                            <div class="form-group">
                                <div class="checkbox">
                                    <label for="ebillspayTransactionDateAllowed">

                                    <input type="checkbox" name="ebillspayTransactionDateAllowed" id="ebillspayTransactionDateAllowed" class="flat-red" />
                                        View e-BillsPay Incomplete/Unapproved Transactions<br/>
                                            <small><i class="fa fa-info-circle"></i> Allow organization see incomplete/unapproved e-BillsPay Transactions</small>
                                    </label>
                                </div>
                            </div>

                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default btn-flat" data-dismiss="modal">
                                <i class="fa fa-times" aria-hidden="true"></i> Close</button>
                            <button type="submit" class="btn btn-primary btn-flat" id="btnSaveOrganization">
                                <i class="fa fa-bookmark" aria-hidden="true"></i> Save</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </jsp:body>
</tags:layout>
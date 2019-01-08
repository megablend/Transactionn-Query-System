<%@taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<tag:layout contentHeader="e-BillsPay Billers">

    <jsp:attribute name="scripts">
        <c:url var="resourceUrl" value="/resources/" />
        <script type="text/javascript" src="${resourceUrl}js/Biller.js"></script>
    </jsp:attribute>

    <jsp:body>
        <div class="row">
            <div class="col-md-12">

                        <div class="nav-tabs-custom">
                            <ul class="nav nav-tabs">
                                <li class="active"><a href="#billers" data-toggle="tab">Billers</a> </li>
                                <li><a href="#ebillsConfiguration" data-toggle="tab">e-BillsPay Billing Configuration (Sharing Formula)</a></li>
                            </ul>

                            <div class="tab-content">
                                <div class="tab-pane active" id="billers">
                                    <table class="table table-bordered table-striped myDataTable table-condensed">
                                        <thead>
                                        <tr>
                                            <th>Name</th>
                                            <th>&nbsp;</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:if test="${not empty billers}">
                                            <c:url var="baseUrl" value="/billers/" />
                                            <c:forEach items="${billers}" var="b">
                                                <tr>
                                                    <td>${b.text}</td>
                                                    <td><a href="${baseUrl}${b.id}" title="view details" class="btn btn-sm btn-flat"><i class="fa fa-eye"></i> </a> </td>
                                                </tr>
                                            </c:forEach>
                                        </c:if>
                                        </tbody>
                                    </table>
                                </div>
                                <div class="tab-pane" id="ebillsConfiguration">
                                    <div class="box box-default">
                                        <div class="box-body">
                                            <table class="table table-condensed table-bordered table-striped myDataTable">
                                                <thead>
                                                <tr>
                                                    <th>Biller</th>
                                                    <th>Aggregator Share</th>
                                                    <th>Collecting Bank Share</th>
                                                    <th>NIBSS Share</th>
                                                    <th>Biller Bank Share</th>
                                                    <th>Biller Bank Code</th>
                                                    <th>Percentage ?</th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <c:if test="${not empty ebillspayConfig}">
                                                    <c:forEach var="config" items="${ebillspayConfig}">
                                                        <tr>
                                                            <td>${config.billerName}</td>
                                                            <td><fmt:formatNumber value="${config.aggregatorShare}" maxIntegerDigits="8" maxFractionDigits="4" /> </td>
                                                            <td><fmt:formatNumber value="${config.collectingBankShare}" maxIntegerDigits="8" maxFractionDigits="4" /> </td>
                                                            <td><fmt:formatNumber value="${config.nibssShare}" maxIntegerDigits="8" maxFractionDigits="4" /> </td>
                                                            <td><fmt:formatNumber value="${config.billerBankShare}" maxIntegerDigits="8" maxFractionDigits="4" /> </td>
                                                            <td>
                                                                <c:choose>
                                                                    <c:when test="${not empty config.billerBankCode}">
                                                                        ${config.billerBankCode}
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        N/A
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </td>
                                                            <td>
                                                                <c:choose>
                                                                    <c:when test="${config.percentage}">
                                                                        <i class="fa fa-check"></i>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <i class="fa fa-times"></i>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </c:if>
                                                </tbody>
                                            </table>
                                        </div>
                                        <div class="box-footer">
                                            <button class="btn btn-primary btn-flat pull-right" data-toggle="modal" data-target="#ebillsPayConfigDialog">
                                                <i class="fa fa-plus"></i> Add New Sharing Configuration
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
        </div>
        </div>

        <!-- ebillspay config dialog -->
        <div class="modal fade" role="dialog" aria-labelledby="myLargeModalLabel" id="ebillsPayConfigDialog">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="gridSystemModalLabel">Add New Transaction Fee Sharing Configuration</h4>
                    </div>
                    <form class="form" name="ebillsSharingForm" id="ebillsSharingForm" method="post" action="<c:url value="/billers/ebillssharingconfig" /> " autocomplete="off">

                        <div class="modal-body">
                            <div class="form-group">
                                <label for="biller">Biller </label>
                                <select name="biller" class="form-control mySelect2" required id="biller" style="width: 100%;">
                                    <option value="">--SELECT--</option>
                                    <c:forEach var="b" items="${billers}">
                                        <option value="${b.id}">${b.text}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="sharingConfiguration"> Sharing Configuration</label>
                                <select name="sharingConfiguation" id="sharingConfiguration" class="form-control">
                                    <option value="1">NIBSS-40%, COLLECTING BANK-60%</option>
                                    <option value="2">NIBSS-30%, COLLECTING BANK-60%, AGGREGATOR-10%</option>
                                    <option value="3">NIBSS-N15, COLLECTING BANK-N30, AGGREGATOR-N5</option>
                                    <option value="4">Custom</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <div class="checkbox">
                                    <label>
                                        <input type="checkbox" name="percentage" id="percentage" class="flat-red"/>
                                        Percentage ?
                                        <br/><small><i class="fa fa-info"></i> Indicates if this sharing configuration is based on percentages or actual figures.
                                        If percentage based, enter the pecentages as fractions. E.g, 0.1 not 10%</small>
                                    </label>
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="collectingBankShare">Collecting Bank Share
                                    <br/><small><i class="fa fa-info"></i> The bank where the transaction was initiated</small>
                                </label>
                                <input type="number" class="form-control" name="collectingBankShare" id="collectingBankShare"  min="0" max="10000000" />
                            </div>
                            <div class="form-group">
                                <label for="aggregatorShare">Aggregator Share
                                <br/><small><i class="fa fa-info"></i> The aggregator is the entity that introduced the biller to the platform.
                                    This can be a bank or a third party</small>
                                </label>
                                <input type="number" id="aggregatorShare" name="aggregatorShare" class="form-control"  min="0" max="10000000" />
                            </div>
                            <div class="form-group">
                                <label for="nibssShare">NIBSS Share</label>
                                <input type="number" name="nibssShare" id="nibssShare" class="form-control"  min="0" max="10000000" />
                            </div>

                            <div class="form-group">
                                <label for="billerBankShare">Biller Bank Share
                                <br/><small><i class="fa fa-info"></i> This is used to capture the monetary charge agreed between the biller and the introducing bank.
                                        This should be 0 if none exists</small>
                                </label>
                                <input type="number" name="billerBankShare" id="billerBankShare" class="form-control"  min="0" max="10000000" />
                            </div>

                            <div class="form-group">
                                <label for="billerBankCode"> Biller Bank Code
                                <br/><small><i class="fa fa-info"></i> The CBN bank code for the bank mentioned above. If Biller Bank Share is 0, leave empty</small>
                                </label>
                                <input type="text" name="billerBankCode" id="billerBankCode" class="form-control" />
                            </div>


                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default btn-flat" data-dismiss="modal">
                                <i class="fa fa-times" aria-hidden="true"></i> Close</button>
                            <button type="submit" class="btn btn-primary btn-flat" id="btnSaveEbillsConfig">
                                <i class="fa fa-bookmark" aria-hidden="true"></i> Save</button>
                        </div>
                    </form>
                </div>
            </div>
        </div> <!--//ebillspay config dialog -->
    </jsp:body>
</tag:layout>
<%@taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<tag:layout contentHeader="USSD Configuration">
    <jsp:attribute name="scripts">
        <c:url var="resourceUrl" value="/resources/" />
        <script type="text/javascript" src="${resourceUrl}js/Ussd.js"></script>
    </jsp:attribute>

    <jsp:body>

        <div class="row">
            <div class="col-md-12">
                <div class="nav-tabs-custom">
                    <ul class="nav nav-tabs">
                        <li class="active"><a href="#ussdBillers" data-toggle="tab">USSD Bill Payment Billers</a> </li>
                        <li><a href="#ussdSharingConfig" data-toggle="tab">USSD Bill Payment Sharing Config</a> </li>
                        <li><a href="#merchantPaymentConfig" data-toggle="tab">mCASH Configuration</a> </li>
                    </ul>
                    <div class="tab-content">
                        <div class="tab-pane active" id="ussdBillers">

                            <div class="box box-default">
                                <div class="box-body table-responsive">
                                    <table class="table table-striped myDataTable table-bordered">
                                        <thead>
                                        <tr>
                                            <th>Merchant</th>
                                            <th>Code</th>
                                            <th>Transaction Fee</th>
                                            <th>Fee Type</th>
                                            <th>Amount Floor</th>
                                            <th>Amount Cap</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach var="b" items="${ussdBillers}">
                                            <tr>
                                                <td>${b.name}</td>
                                                <td>${b.merchantCode}</td>
                                                <td><fmt:formatNumber value="${b.transactionFeeConfig.transactionFee}" maxFractionDigits="4" minFractionDigits="2" /> </td>
                                                <td>${b.transactionFeeConfig.feeType}</td>
                                                <td><fmt:formatNumber value="${b.transactionFeeConfig.amountFloor}" maxFractionDigits="4" minFractionDigits="2" /> </td>
                                                <td><fmt:formatNumber value="${b.transactionFeeConfig.amountCap}" maxFractionDigits="4" minFractionDigits="2" /> </td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </div>


                        </div> <!--//ussd biller tab -->
                        <div class="tab-pane" id="ussdSharingConfig">

                            <div class="box box-default">
                                <div class="box-body table-responsive">
                                    <table class="table table-striped myDataTable">
                                        <thead>
                                        <tr>
                                            <th>Merchant</th>
                                            <th>Aggregator Share</th>
                                            <th>Collecting Bank Share</th>
                                            <th>NIBSS Share</th>
                                            <th>Biller Bank Share</th>
                                            <th>USSD Aggregator Share</th>
                                            <th>Telco Share</th>
                                            <th>Biller Bank Code</th>
                                            <th>Percentage ?</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach items="${ussdBillingConfig}" var="config">
                                            <tr>
                                                <td>${config.ussdBiller.name}</td>
                                                <td><fmt:formatNumber value="${config.aggregatorShare}" maxFractionDigits="4" minFractionDigits="2" /> </td>
                                                <td><fmt:formatNumber value="${config.collectingBankShare}" maxFractionDigits="4" minFractionDigits="2" /> </td>
                                                <td><fmt:formatNumber value="${config.nibssShare}" maxFractionDigits="4" minFractionDigits="2" /> </td>
                                                <td><fmt:formatNumber value="${config.billerBankShare}" maxFractionDigits="4" minFractionDigits="2" /> </td>
                                                <td><fmt:formatNumber value="${config.ussdAggregatorShare}" maxFractionDigits="4" minFractionDigits="2" /> </td>
                                                <td><fmt:formatNumber value="${config.telcoShare}" maxFractionDigits="4" minFractionDigits="2" /> </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty config.billerBankCode}">
                                                            ${config.billerBankCode}
                                                        </c:when>
                                                        <c:otherwise>N/A</c:otherwise>
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
                                        </tbody>
                                    </table>
                                </div>
                                <div class="box-footer">
                                    <button class="btn btn-flat btn-primary pull-right" data-toggle="modal" data-target="#ussdSharingDialog">
                                        <i class="fa fa-plus"></i> Add New Configuration
                                    </button>
                                </div>
                            </div>

                        </div> <!--//ussd sharing config -->
                        <div class="tab-pane" id="merchantPaymentConfig">
                            <form role="form" id="merchantPaymentConfigForm" method="post" action="<c:url value="/ussd/merchantpaymentconfig" /> ">
                            <div class="box box-default">
                                <div class="box-body">
                                    <div class="form-group">
                                        <fmt:formatNumber value="${merchantPaymentConfig.telcoShare}" minFractionDigits="2" maxFractionDigits="4" var="telcoShare" />
                                        <label for="telcoShare">Telco Share</label>
                                        <input type="number" name="telcoShare" id="telcoShare" required class="form-control"  value="${telcoShare}"  min="0" max="10000000" />
                                    </div>
                                    <div class="form-group">
                                        <fmt:formatNumber value="${merchantPaymentConfig.ussdAggregatorShare}" minFractionDigits="2" maxFractionDigits="4" var="ussdAggShare" />
                                        <label for="ussdAggregatorShare">USSD Aggregator Share</label>
                                        <input type="number" name="ussdAggregatorShare" id="ussdAggregatorShare" required class="form-control" value="${ussdAggShare}"  min="0" max="10000000" />
                                    </div>
                                    <div class="form-group">
                                        <fmt:formatNumber value="${merchantPaymentConfig.payerBankShare}" minFractionDigits="2" maxFractionDigits="4" var="payerBankShare" />
                                        <label for="payerBankShare">Payer (Collecting) Bank Share</label>
                                        <input type="number" name="payerBankShare" id="payerBankShare" class="form-control" value="${payerBankShare}"  min="0" max="10000000" />
                                    </div>

                                    <div class="form-group">
                                        <fmt:formatNumber value="${merchantPaymentConfig.merchantBankShare}" minFractionDigits="2" maxFractionDigits="4" var="merchantBankShare" />
                                        <label for="merchantBankShare">Merchant (Destination) Bank Share</label>
                                        <input type="number" name="merchantBankShare" id="merchantBankShare" required class="form-control" value="${merchantBankShare}"  min="0" max="10000000" />
                                    </div>

                                    <div class="form-group">
                                        <fmt:formatNumber value="${merchantPaymentConfig.nibssShare}" minFractionDigits="2" maxFractionDigits="4" var="nibssShare" />
                                        <label for="nibssShare">NIBSS Share</label>
                                        <input type="number" name="nibssShare" id="nibssShare" required class="form-control" value="${nibssShare}"  min="0" max="10000000" />
                                    </div>

                                    <div class="form-group">
                                        <fmt:formatNumber value="${merchantPaymentConfig.merchantIntroducerShare}" minFractionDigits="2" maxFractionDigits="4" var="merchantIntroducerShare" />
                                        <label for="merchantIntroducerShare">Merchant Introducer Share</label>
                                        <input type="number" name="merchantIntroducerShare" id="merchantIntroducerShare" required class="form-control"  value="${merchantIntroducerShare}"  min="0" max="10000000" />
                                    </div>

                                    <div class="form-group">
                                        <fmt:formatNumber value="${merchantPaymentConfig.schemeRoyaltyShare}" minFractionDigits="2" maxFractionDigits="4" var="schemeRoyaltyShare" />
                                        <label for="schemeRoyaltyShare">Scheme Royalty Share</label>
                                        <input type="number" name="schemeRoyaltyShare" id="schemeRoyaltyShare" required class="form-control" value="${schemeRoyaltyShare}" min="0" max="10000000" />
                                    </div>

                                    <div class="form-group">
                                        <label for="schemeRoyaltyCode">Scheme Royalty Code
                                        <br/><small><i class="fa fa-info"></i>The Telco Code for the scheme royalty beneficiary</small></label>
                                        <input type="text" name="schemeRoyaltyCode" id="schemeRoyaltyCode" required class="form-control" value="${merchantPaymentConfig.schemeRoyaltyCode}" />
                                    </div>

                                </div>
                            </div>
                                <div class="box-footer">
                                    <button type="submit" class="btn btn-primary btn-flat pull-right">
                                        <i class="fa fa-bookmark"></i> Save Merchant Payment Config
                                    </button>
                                </div>
                            </form>

                        </div> <!--//merchant payment config -->
                    </div>
                </div>
            </div>
        </div>

        <!-- ebillspay config dialog -->
        <div class="modal fade" role="dialog" aria-labelledby="myLargeModalLabel" id="ussdSharingDialog">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="gridSystemModalLabel">Add New Transaction Fee Sharing Configuration</h4>
                    </div>
                    <form class="form" name="ussdSharingForm" id="ussdSharingForm" method="post" action="<c:url value="/ussd/ussdsharingconfig" /> " autocomplete="off">

                        <div class="modal-body">
                            <div class="form-group">
                                <label for="ussdBiller">Biller </label>
                                <select name="ussdBiller" class="form-control mySelect2" required id="ussdBiller" style="width: 100%;">
                                    <option value="">--SELECT--</option>
                                    <c:forEach var="b" items="${ussdBillers}">
                                        <option value="${b.id}">${b.name}</option>
                                    </c:forEach>
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
                                <input type="number" class="form-control txtFeeConfig" name="collectingBankShare" id="collectingBankShare"  min="0" max="10000000" />
                            </div>
                            <div class="form-group">
                                <label for="aggregatorShare">Aggregator Share
                                    <br/><small><i class="fa fa-info"></i> The aggregator is the entity that introduced the biller to the platform.
                                        This can be a bank or a third party. Set to 0 if not needed</small>
                                </label>
                                <input type="number" id="aggregatorShare" name="aggregatorShare" class="form-control txtFeeConfig"  min="0" max="10000000" />
                            </div>
                            <div class="form-group">
                                <label for="ussdAggregatorShare">USSD Aggregator Share
                                    <br/><small><i class="fa fa-info"></i> The USSD aggregator is the USSD platform provider</small>
                                </label>
                                <input type="number" id="ussdAggregatorShare" name="ussdAggregatorShare" class="form-control txtFeeConfig"  min="0" max="10000000" />
                            </div>
                            <div class="form-group">
                                <label for="telcoShare">Telco Share
                                    <br/><small><i class="fa fa-info"></i> The Telco is the GSM network provider through which this transaction came</small>
                                </label>
                                <input type="number" id="telcoShare" name="telcoShare" class="form-control txtFeeConfig" min="0" max="10000000" />
                            </div>
                            <div class="form-group">
                                <label for="nibssShare">NIBSS Share</label>
                                <input type="number" name="nibssShare" id="nibssShare" class="form-control txtFeeConfig"   min="0" max="10000000" />
                            </div>

                            <div class="form-group">
                                <label for="billerBankShare">Biller Bank Share
                                    <br/><small><i class="fa fa-info"></i> This is used to capture the monetary charge agreed between the biller and the introducing bank.
                                        This should be 0 if none exists</small>
                                </label>
                                <input type="number" name="billerBankShare" id="billerBankShare" class="form-control txtFeeConfig" min="0" max="10000000" />
                            </div>

                            <div class="form-group">
                                <label for="billerBankCode"> Biller Bank Code
                                    <br/><small><i class="fa fa-info"></i> The CBN bank code for the bank mentioned above. If Biller Bank Share is 0, leave empty</small>
                                </label>
                                <input type="text" name="billerBankCode" id="billerBankCode" class="form-control"  />
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
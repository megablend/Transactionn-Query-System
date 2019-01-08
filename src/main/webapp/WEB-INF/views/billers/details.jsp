<%--
  Created by IntelliJ IDEA.
  User: eoriarewo
  Date: 8/9/2016
  Time: 7:53 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<tag:layout contentHeader="e-BillsPay Biller: ${biller.name}">
    <jsp:attribute name="scripts">
        <c:url value="/resources/" var="resourceUrl" />
        <script type="text/javascript" src="${resourceUrl}js/BillerDetails.js"></script>
    </jsp:attribute>
    <jsp:body>
        <c:url value="/billers/" var="baseUrl" />
        <div class="row">
            <div class="col-md-12">
                <div class="nav-tabs-custom">
                    <ul class="nav nav-tabs">
                        <li class="active"><a href="#billerSettings" data-toggle="tab"><i class="fa fa-cogs"></i> Settings</a> </li>
                        <li><a href="#ebillspayTransactionFee" data-toggle="tab"><i class="fa fa-money"></i> e-BillsPay Transaction Fee</a> </li>
                    </ul>
                    <div class="tab-content">
                        <div class="tab-pane active" id="billerSettings">
                            <form role="form" method="post" action="${baseUrl}${biller.id}/settings" name="settingsForm" id="settingsForm">
                                <input type="hidden" name="biller" value="${biller.id}" />
                                <div class="box box-default">
                                    <div class="box-body">
                                        <div class="form-group">
                                            <label for="paramName">Param Name
                                                <br/>
                                                <small><i class="fa fa-info"></i>
                                                    The name of the parameter collected for this biller that can serve as a unique identifier for a transaction</small>
                                            </label>
                                            <input type="text" value="${biller.billerSetting.paramName}" required name="paramName" id="paramName" class="form-control"/>
                                        </div>
                                        <div class="form-group">
                                            <label for="billingCycle">e-BillsPay Billing Cycle</label>
                                            <select name="billingCycle" class="form-control" id="billingCycle" required>
                                                <option value=""> --SELECT-- </option>
                                                <option value="WEEKLY"
                                                    <c:if test="${biller.billerSetting.billingCycle eq 'WEEKLY'}">
                                                        selected
                                                          </c:if>
                                                    >
                                                    WEEKLY</option>
                                                <option value="MONTHLY"<c:if test="${biller.billerSetting.billingCycle eq 'MONTHLY'}">
                                                    selected
                                                </c:if>
                                                >MONTHLY</option>
                                            </select>
                                        </div>
                                    </div>
                                    <div class="box-footer">
                                        <button class="pull-right btn btn-flat btn-primary" type="submit">
                                            <i class="fa fa-bookmark"> </i> Save Biller Settings
                                        </button>
                                    </div>
                                </div>
                            </form>

                        </div> <!--//biller settings -->
                        <div class="tab-pane" id="ebillspayTransactionFee">
                            <form role="form" method="post" action="${baseUrl}${biller.id}/ebillspaytransactionfee" name="feeForm" id="feeForm">
                                <input type="hidden" name="biller" value="${biller.id}" />
                                <div class="box box-default">
                                    <div class="box-body">
                                        <div class="form-group">
                                            <label for="fee">Transaction Fee
                                            <br/>
                                                <small>
                                                    <i class="fa fa-info"></i>
                                                    The transaction fee as agreed for the biller. <br/> If it is a percentage of the transaction amount,
                                                    Please specify in fraction, not whole number. For example,2.5% is 0.025 instead of 2.5
                                                </small>
                                            </label>
                                            <fmt:formatNumber var="fee" maxFractionDigits="4" minFractionDigits="2" value="${biller.ebillsPayTransactionFee.fee}"/>
                                            <input type="number" name="fee" id="fee" required class="form-control" value="${fee}"  min="0" max="10000000"/>
                                        </div>
                                        <div class="form-group">
                                            <div class="checkbox">
                                                <label>
                                                    <input type="checkbox" name="percentage" class="flat-red" id="percentage"
                                                           <c:if test="${biller.ebillsPayTransactionFee.percentage}">
                                                               checked
                                                           </c:if>
                                                           />
                                                    Is Percentage ?
                                                    <br/>
                                                    <small><i class="fa fa-info"></i> Check if Transaction Fee is a percentage of the Transaction Amount</small>
                                                </label>
                                            </div>
                                        </div>
                                        <div class="percentageBounds" style="display: none">
                                        <div class="form-group">
                                            <label for="amountCap">Amount Cap
                                            <br/>
                                                <small><i class="fa fa-info"></i> If transaction fee above is percentage,
                                                    the computed transaction fee should not exceed the amount in naira specified below</small>
                                            </label>
                                            <fmt:formatNumber var="amountCap" maxFractionDigits="2" minFractionDigits="2" value="${biller.ebillsPayTransactionFee.amountCap}" />
                                            <input type="number" name="amountCap" id="amountCap" class="form-control" value="${amountCap}"  min="0" max="10000000" />
                                        </div>
                                        <div class="form-group">
                                            <label for="amountFloor">Amount Floor
                                                <br/>
                                                <small><i class="fa fa-info"></i> If transaction fee above is percentage,
                                                    the computed transaction fee should not be lower than the amount in naira specified below
                                                    </small>
                                            </label>
                                            <fmt:formatNumber var="amountFloor" maxFractionDigits="2" minFractionDigits="2" value="${biller.ebillsPayTransactionFee.amountFloor}" />
                                            <input type="number" name="amountFloor" id="amountFloor" class="form-control" value="${amountFloor}"  min="0" max="10000000" />
                                        </div>
                                        </div>
                                        <div class="form-group">
                                            <div class="checkbox">
                                                <label>
                                                    <input type="checkbox" name="customerPays" class="flat-red" id="customerPays"
                                                            <c:if test="${biller.ebillsPayTransactionFee.customerPays}">
                                                                checked
                                                            </c:if>
                                                    />
                                                    Customer Pays ?
                                                    <br/>
                                                    <small><i class="fa fa-info"></i> Check if Transaction Fee is taken from customer. Leave unchecked if from biller</small>
                                                </label>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <div class="checkbox">
                                                <label>
                                                    <input type="checkbox" name="transactionTimeTaken" class="flat-red" id="transactionTimeTaken"
                                                            <c:if test="${biller.ebillsPayTransactionFee.transactionTimeTaken}">
                                                                checked
                                                            </c:if>
                                                    />
                                                    Taken At Transaction Time ?
                                                    <br/>
                                                    <small><i class="fa fa-info"></i> Check if Transaction Fee is taken at transaction time</small>
                                                </label>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="box-footer">
                                        <button class="btn btn-flat btn-success pull-right" type="submit">
                                            <i class="fa fa-bookmark"></i>  Save e-BillsPay Transaction Fee
                                        </button>
                                    </div>
                                </div>
                            </form>

                        </div> <!--//ebills transaction fee -->
                    </div>
                </div>
            </div>
        </div>
    </jsp:body>
</tag:layout>
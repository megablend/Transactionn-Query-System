<%@taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<tag:layout contentHeader="Custom Billing Report" title="Billing Report">
    <jsp:attribute name="scripts">

        <script type="text/javascript">
            var startDate = null, endDate = null;
            $(function () {

                $('#dateRange').daterangepicker({
                    opens: 'left',
                    timePicker: true,
                    timePickerIncrement: 1,
                    startDate: moment(),
                    endDate: moment().endOf('day'),
                    minDate: moment().subtract(3,'year').startOf('day'),
                    maxDate: moment().endOf('day'),
                    ranges: {
                        'Today': [moment().startOf('days'), moment().endOf('day')],
                        'Yesterday': [moment().subtract(1, 'd').startOf('days'), moment().subtract(1, 'days').endOf('day')],
                        'Last 7 Days': [moment().subtract(6, 'days').startOf('days'), moment().endOf('day')],
                    },locale: {
                        format: 'YYYY-MM-DD HH:mm'
                    }
                }, function (locStartDate, locEndDate) {
                    startDate = locStartDate.format('YYYY-MM-DD HH:mm:ss');
                    endDate = locEndDate.seconds(59).format('YYYY-MM-DD HH:mm:ss');
                });

                $('#billingForm').validate({
                    submitHandler: function (form) {
                        var data = {
                            product: $('#product option:selected').val(),
                            generateReports: $('#generateReports')[0].checked,
                            startDate: startDate,
                            endDate: endDate
                        };
                        var postObj = {url: $(form).attr('action'), data: data};
                        $.blockUI();
                        App.post(postObj);
                        return false;
                    }
                });
            });
        </script>
    </jsp:attribute>
    <jsp:body>
        <div class="row">
            <div class="col-md-12">
                <div class="box box-primary">
                    <c:url value="/billing" var="billingUrl" />
                    <form role="form" method="post" action="${billingUrl}" name="billingForm" id="billingForm" autocomplete="off">
                        <div class="box-body">
                            <div class="form-group">
                                <label for="dateRange">Date and Time Period</label>
                                <input type="text" name="dateRange" id="dateRange" class="form-control"  required  readonly />
                            </div>
                            <div class="form-group">
                                <label for="product">Product</label>
                                <select class="form-control" style="width:100%;" name="product" id="product" required>
                                    <option value=""> --SELECT-- </option>
                                    <option value="ussd">USSD Bill Payment</option>
                                    <option value="mpay">mCASH</option>
                                    <option value="ebills">e-BillsPay Billing (Transaction Time Taken)</option>
                                    <option value="ebills_custom">e-BillsPay Billing (Not Transaction time taken)</option>
                                    <option value="cpay">CentralPay Account Billing</option>
                                    <sec:authorize access="hasRole('ROLE_CL_ADMIN')">
                                        <option value="cl_annual">Corporate Lounge - Annual Subscription</option>
                                        <option value="cl_transaction">Corporate Lounge - Per Transaction</option>
                                    </sec:authorize>
                                </select>
                            </div>

                            <div class="form-group">
                                <div class="checkbox">
                                    <label>
                                        <input type="checkbox" class="flat-red" name="generateReports" id="generateReports" />
                                        Generate Reports for concerned parties?
                                    </label>
                                </div>
                            </div>
                            <div class="box-footer">
                                <button type="submit" class="btn btn-flat btn-info pull-right"><i class="fa fa-cogs"></i> Generate Billing Report</button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </jsp:body>
</tag:layout>

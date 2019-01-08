var Transactions = {};
var trxnTable = null;

var gStartDate;
var gEndDate;
var selectedItem;
var searchDate;
$(function () {

    initDatePicker();
    initTransactionTable();
    setupRefresher();
    initSelectible();

    $('#searchDate').change(function () {
        searchDate = $(this, 'option:selected').val();
        drawTable();
    }).change();

});

function initDatePicker() {
    var config = {
        opens: 'left',
        timePicker: true,
        timePickerIncrement: 1,
        startDate: moment().startOf('days'),
        endDate: moment().endOf('day'),
        minDate: moment().subtract(2, 'months').startOf('month'),
        maxDate: moment().endOf('day'),
        locale: {
            format: 'YYYY-MM-DD HH:mm'
        },
        ranges: {
            'Today': [moment().startOf('day'), moment().endOf('day')],
            'Yesterday': [moment().subtract(1, 'days').startOf('days'), moment().subtract(1, 'days').endOf('day')],
            'Last 7 Days': [moment().subtract(6, 'days').startOf('days'), moment().endOf('day')],
            'Last 30 Days': [moment().subtract(29, 'days').startOf('days'), moment().endOf('day')],
            'This Month': [moment().startOf('month'), moment().endOf('month')],
            'Last Month': [moment().subtract(1, 'months').startOf('month'), moment().subtract(1, 'month').endOf('month'), ],
            'Last 2 Months': [moment().subtract(2, 'months').startOf('month'), moment().subtract(2, 'month').endOf('month')]
        }
    };

    if(MERCHANTS) {
        config.showDropdowns = true;
        config.minDate = moment().subtract(5, 'years').startOf('year');
    }


    $('#dateField').daterangepicker(config, function (startDate, endDate) {
        gStartDate = startDate;
        gEndDate = endDate.seconds(59);
        drawTable();
    });
}

function drawTable() {
    if (trxnTable) {
        abortCurrentRequest();
        trxnTable.draw();
    }
}
function initTransactionTable() {
    trxnTable = $('#transactionTable').DataTable({
        // autoWidth: true,
        ordering: false,
        serverSide: true,
        searchDelay: 1300, //delay by 1 second for search requests
        pagingType: 'full_numbers',
        dom: '<"H"<"pull-left tInfo"l><"pull-left dtSearchBox"f><"pull-right"B><"pull-right"T>>t<"F"<"pull-left"i>p>',
        ajax: {
            type: 'POST',
            url: DATA_URL,
            dataSrc: function (json) {
                if (json.extras) {

                    try {
                        buildSummaryTable(json.extras);
                    } catch (e) {
                        App.log('could not build summary part: ' + e);
                    }
                }
                return json.data;
            },
            error: function (xhr, strStatus, strError) {
                if (strError) {
                    if (strError.search(/internal server/i) !== -1)
                        App.error('Sorry, an error occurred while processing your request');
                    else if (strError.search(/unauthorized/i) !== -1 || strError.search(/forbidden/i) !== -1) //session expired
                        location.reload();
                    else if (strError.search(/not found/i) !== -1)
                        App.error('The server could not be contacted at the moment.Please try again later');
                    else if (strError.search(/abort/i) === -1) //aborted occurs when user aborts running request.
                        App.error('Your request failed. Reason: ' + strError);
                }
                $("#summaryBox").html('').hide();
                $('.tInfo label span.mydtProc').remove();
            },
            data: function (dataObj) {
                if (gStartDate && gEndDate) {
                    dataObj.startDate = gStartDate.format('YYYY-MM-DD HH:mm:ss');
                    dataObj.endDate = gEndDate.format('YYYY-MM-DD HH:mm:ss');
                }
                if (selectedItem) {
                    dataObj.searchId = selectedItem;
                }
                if (BANK_CALL)
                    dataObj.isBank = "yes";
                if (searchDate)
                    dataObj.dateType = searchDate;

            }
        },
        buttons: [
            {
                text: '<i class="fa fa-file-text" aria-hidden="true"></i> CSV ',
                action: function () {
                    triggerDownload('csv');
                }
            },
            {
                text: '<i class="fa fa-file-excel-o" aria-hidden="true"></i> MS. Excel',
                action: function () {
                    triggerDownload('excel');
                }
            },
            {
                text: '<i class="fa fa-file-pdf-o" aria-hidden="true"></i> PDF',
                action: function () {
                    triggerDownload('pdf');
                }
            },
        ]
    });

    trxnTable.on('processing.dt', function (e, settings, processing) {
        if (processing) {
            if ($('.tInfo label .mydtProc').size() == 0)
                $('.tInfo label').append("<span class='mydtProc'><i class='fa fa-circle-o-notch fa-spin fa-3x fa-fw'></i></span>");
        } else {
            $('span.mydtProc').remove();
        }
    });
    trxnTable.on('draw.dt', initColorBoxLinks);
}


function setupRefresher() {
    if (!trxnTable)
        return;
    var refreshRate = 5 * 60 * 1000;//5 mins
    if (window.requestAnimationFrame) {
        function refreshTable() {
            setTimeout(function () {
                requestAnimationFrame(refreshTable);
                if (shouldRefresh())
                    drawTable();
            }, refreshRate);
        }
        requestAnimationFrame(refreshTable);
    } else {
        setInterval(function () {
            if (shouldRefresh())
                drawTable();
        }, refreshRate);
    }
}

function buildSummaryTable(extra) {
    if (!extra.summary) {
        $("#summaryBox").html('').hide();
        $('#summaryHolder').hide();
        return;
    }
    var table = "<table class='table table-bordered table-striped table-condensed'><thead><tr><th>&nbsp;</th><th>Volume</th><th>Total Amount(N)</th>";
    if (extra.showFee) {
        table += "<th>Transaction Fee (N)</th>";
    }
    table += "</tr></thead><tbody>";
    for (var i = 0; i < extra.summary.length; i++) {
        table += "<tr><td>" + extra.summary[i].label + "</td>";
        table += "<td>" + extra.summary[i].volume + "</td>";
        table += "<td>" + extra.summary[i].totalAmount + "</td>";
        if (extra.showFee)
            table += "<td>" + extra.summary[i].transactionFee + "</td>";
        table += "</tr></tbody>";;
    }
    table += "</table>";
    $("#summaryBox").html(table).show();
    $('#summaryHolder').show();
}

function initSelectible() {
    if ($('select#searchable').size() == 0)
        return;

    $('select#searchable').select2({
        ajax: {
            url: SEARCH_URL,
            cache: true,
            delay: 250,
            allowClear: true,
            dataType: 'json',
            data: function (param) {
                var obj = {search: param.term};
                if (BANK_CALL)
                    obj.isBank = 1;
                return obj;
            },
            processResults: function (data, param) {
                return {results: data};
            }

        }
    });
    $('#searchable').on('select2:select', function (evt) {
        selectedItem = $('#searchable option:selected').val();
        drawTable();
    });
}

function initColorBoxLinks() {
    $('a.user_params').unbind('click')
            .bind('click', function (evt) {
                evt.preventDefault();
                $.colorbox({href: this.href, width: '60%', height: '30%'});
            });
}

function shouldRefresh() {
    if (null == gStartDate || null == gEndDate)
        return true;
    var isNewerStartDate = gStartDate >= moment().startOf('day');
    var isNewerEndDate = gEndDate >= moment().startOf('day');
    return isNewerStartDate || isNewerEndDate;
}

function triggerDownload(type) {
    var link = $('#transactionDownloadLink').attr('href') + type;
    open(link);
}


function abortCurrentRequest() {
    try {
        if (typeof $ !== "undefined" && $.fn.dataTable) {
            var all_settings = $($.fn.dataTable.tables()).DataTable().settings();
            for (var i = 0, settings; (settings = all_settings[i]); ++i) {
                if (settings.jqXHR)
                    settings.jqXHR.abort();
            }
        }
    } catch (e) {
    }
}
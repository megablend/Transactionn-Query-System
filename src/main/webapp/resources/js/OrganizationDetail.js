$(function () {

    if ($('#settingsForm').size() > 0) {
        settingsValidation();
    }
    if ($('#ebillsPayBillerForm').size() > 0) {
        ebillspayValidation();
    }
    if ($('#centralPayMerchantForm').size() > 0) {
        cpayValidation();
    }

    if ($('#merchantPayForm').size() > 0)
        merchantpayValidation();

    if ($('#ussdBillerForm').size() > 0)
        ussdBillerFormValidation();

    if ($('#ebillsTable').size() > 0)
        getDataTableForProduct('ebillsTable', 'EBILLS');

    if ($('#cpayTable').size() > 0)
        getDataTableForProduct('cpayTable', 'CPAY');

    if ($('#mcashTable').size() > 0)
        getDataTableForProduct('mcashTable', 'MPAY');

    if ($('#ussdTable').size() > 0)
        getDataTableForProduct('ussdTable', 'BPAY');

    validateBankAccountForm();
    validateProductForm();
    initSelect2Searches();

    $('.table').DataTable();
});

function initSelect2Searches() {

    $('#ebillspayBillers').select2(getSelect2Data(eBillsSearchUrl));
    $('#cpayMerchants').select2(getSelect2Data(cpaySearchUrl));
    $('#merchantPayBillers').select2(getSelect2Data(mpaySearchUrl));
    $('#ussdBillers').select2(getSelect2Data(ussdSearchUrl));

}

function getSelect2Data(url) {
    var obj = {
        ajax: {
            url: url,
            cache: true,
            delay: 250,
            dataType: 'json',
            data: function (param) {
                return {
                    search: param.term
                };
            },
            processResults: function (data, param) {
                return {results: data};
            }

        }
    }

    return obj;
}

function validateProductForm() {
    $('#productForm').validate({
        errorClass: 'invalid',
        validClass: 'valid',
        rules: {
            product: {
                minlength: 1
            }
        },
        messages: {
            product: {
                minlength: $.validator.format('At least {0} product should be selected')
            }
        },
        submitHandler: function (productForm) {
            var postUrl = $(productForm).attr('action');
            var postObj = {url: postUrl, data: $(productForm).serialize()};
            postObj.onSuccess = function (data) {
                App.success('Products have been successfully updated');
                setTimeout(function () {
                    location.reload();
                }, 500);
            }
            App.post(postObj);
            return false;
        }
    });
}
function validateBankAccountForm() {
    $('#bankAccountForm').validate({
        errorClass: 'invalid',
        validClass: 'valid',
        submitHandler: function (form) {
            $('#newBankAccountDialog').modal('hide');
            var postObj = {url: $(form).attr('action'), data: $(form).serialize()};
            postObj.onSuccess = function (data) {
                App.success('Bank Account was successfully saved');
                setTimeout(function () {
                    location.reload();
                }, 500);
            }
            App.post(postObj);
            return false;

        }
    });
}
function settingsValidation() {
    $('#settingsForm').validate({
        errorClass: 'invalid',
        validClass: 'valid',

        submitHandler: function (settingForm) {
            var url = $(settingForm).attr('action');
            var postObj = {url: url, data: $(settingForm).serialize()};
            postObj.onSuccess = function (data) {
                App.success('Organization Settings have been successfully updated');
            };
            App.post(postObj);
            return false;
        }
    });
}

function ebillspayValidation() {
    $('#ebillsPayBillerForm').validate({
        errorClass: 'invalid',
        validClass: 'valid',
        rules: {
            billers: {
                minlength: 1
            }
        },
        messages: {
            billers: {
                minlength: $.validator.format('At least {0} biller should be selected')
            }
        },
        submitHandler: function (ebillsForm) {
            var action = $(ebillsForm).attr('action');
            var postObj = {url: action, data: $(ebillsForm).serialize()};
            postObj.onSuccess = function (data) {
                App.success('Biller information has been updated successfully');
                redrawTable('ebillsTable','ebillspayBillers');
            };
            App.post(postObj);
            return false;
        }
    });
}

function cpayValidation() {
    $('#centralPayMerchantForm').validate({
        errorClass: 'invalid',
        validClass: 'valid',
        rules: {
            merchant: {
                minlength: 1
            }
        },
        messages: {
            merchant: {
                minlength: $.validator.format('At least {0} CentralPay merchant should be selected')
            }
        },
        submitHandler: function (cpayForm) {
            var url = $(cpayForm).attr('action');
            var postObj = {url: url, data: $(cpayForm).serialize()};
            postObj.onSuccess = function (data) {
                App.success('CentralPay merchant information has been updated successfully');
                redrawTable('cpayTable','cpayMerchants');
            };
            App.post(postObj);
            return false;
        }
    });
}

function merchantpayValidation() {
    $('#merchantPayForm').validate({
        errorClass: 'invalid',
        validClass: 'valid',
        rules: {
            merchants: {
                minlength: 1
            }
        },
        messages: {
            merchants: {
                minlength: $.validator.format('At least {0} merchant should be selected')
            }
        },
        submitHandler: function (form) {
            var action = $(form).attr('action');
            var postObj = {url: action, data: $(form).serialize()};
            postObj.onSuccess = function (data) {
                App.success('Merchant Payment merchant has been updated successfully');
                redrawTable('mcashTable','merchantPayBillers');
            };
            App.post(postObj);
            return false;
        }
    });
}

function getDataTableForProduct(tableId, productId) {
    $('#' + tableId).DataTable({
        // autoWidth: true,
        // dom: '<"H"<"pull-left tInfo"l><"pull-left dtSearchBox"f><"pull-right"><"pull-right"T>>t<"F"<"pull-left"i>p>',
        ordering: false,
        serverSide: true,
        pagingType: 'full_numbers',
        searching: false,
        ajax: {
            type: 'POST',
            url: productTableUrl,
            dataSrc: function (json) {
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
                $('.tInfo label span.mydtProc').remove();
            },
            data: function (dataObj) {
                dataObj.organizationId = ORGANIZATION_ID;
                dataObj.productCode = productId;
            }
        }
    });
}

function ussdBillerFormValidation() {
    //

    $('#ussdBillerForm').validate({
        errorClass: 'invalid',
        validClass: 'valid',
        rules: {
            ussd: {
                minlength: 1
            }
        },
        messages: {
            ussd: {
                minlength: $.validator.format('At least {0} USSD biller should be selected')
            }
        },
        submitHandler: function (form) {
            var action = $(form).attr('action');
            var postObj = {url: action, data: $(form).serialize()};
            postObj.onSuccess = function (data) {
                App.success('USSD Biller has been updated successfully');
                redrawTable('ussdTable','ussdBillers');
            };
            App.post(postObj);
            return false;
        }
    });
}

function redrawTable(tableId,selectId) {
    var $obj = $('#'+tableId);
    if($obj.size() > 0)
        $obj.DataTable().draw();

    var $sel = $('#'+selectId);

    if($sel.size() > 0)
        $sel[0].options = 0;
}
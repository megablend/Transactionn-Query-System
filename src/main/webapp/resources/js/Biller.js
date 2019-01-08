var ebillsSharingConfig = null;
$(function () {
    initEbillsSharingConfig();
    $('#sharingConfiguration').change(updateConfig).change();
    initEbillsConfigForm();
});

function updateConfig(evt) {
    var selected = $('option:selected', this).val();
    var config = getSharingConfigById(selected);
    if (!config)
        return;
    $('#aggregatorShare').val(config.aggregatorShare);
    $('#collectingBankShare').val(config.collectingBankShare);
    $('#nibssShare').val(config.nibssShare);
    $('#billerBankShare').val(config.billerBankShare);
    // $('#percentage')[0].checked = config.isPercentage;
    var state = config.isPercentage ? 'check' : 'uncheck';
    $('#percentage').iCheck(state);
    $('#billerBankCode').val('');

}

function initEbillsConfigForm() {
    $('#ebillsSharingForm').validate({
        submitHandler: function (form) {
            var total = 0;
            $('#ebillsSharingForm input[type=number]').each(function (idx, elem) {
                var val = $.trim(this.value);
                var fltVal = parseFloat(val);
                if (!isNaN(fltVal))
                    total += fltVal;
            });
            if ($('#percentage').is(':checked')) {
                if (total != 1) {
                    App.error('Percentage values should sum up to one');
                    return;
                }
            }
            var postObj = {url: $(form).attr('action'), data: $(form).serialize()};
            postObj.onSuccess = function (data) {
                App.success('Sharing configuration was successfully saved');
                setTimeout(function () {
                    location.reload();
                }, 300);
            }

            $('#ebillsPayConfigDialog').modal('hide');
            App.post(postObj);
            return false;
        }
    });
}

function initEbillsSharingConfig() {
    ebillsSharingConfig = new Array();
    var first_config = {id: 1, nibssShare: 0.40, aggregatorShare: 0.00, billerBankShare: 0.00, collectingBankShare: 0.60, isPercentage: true};
    var second_config = {id: 2, nibssShare: 0.30, aggregatorShare: 0.10, billerBankShare: 0.00, collectingBankShare: 0.60, isPercentage: true};
    var third_config = {id: 3, nibssShare: 15, aggregatorShare: 5, billerBankShare: 0.00, collectingBankShare: 30, isPercentage: false};
    var custom_config = {id: 4, nibssShare: 0.00, aggregatorShare: 0.00, billerBankShare: 0.00, collectingBankShare: 0.00, isPercentage: false};
    ebillsSharingConfig.push(first_config);
    ebillsSharingConfig.push(second_config);
    ebillsSharingConfig.push(third_config);
    ebillsSharingConfig.push(custom_config);
}
function getSharingConfigById(id) {
    var theConfig = null;
    for (var i = 0; i < ebillsSharingConfig.length; i++) {
        if (ebillsSharingConfig[i].id === parseInt(id)) {
            theConfig = ebillsSharingConfig[i];
            break;
        }
    }
    return theConfig;
}
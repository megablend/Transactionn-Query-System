$(function () {
    initMerchantPayConfigForm();
    initUssdForm();
});

function initMerchantPayConfigForm() {
    $('#merchantPaymentConfigForm').validate({
       errorClass: 'invalid',
        validClass: 'valid',
        submitHandler: function(form) {
            var postObj = { url: $(form).attr('action'), data: $(form).serialize()};
            $.blockUI();

            App.post(postObj);
            return false;
        }
    });
}

function initUssdForm() {
    $('#ussdSharingForm').validate({
        errorClass: 'invalid',
        validClass: 'valid',
        submitHandler: function(form) {
            if( !form.billerBankShare.value)
                form.billerBankShare.value = 0;

            if($('#percentage').is(':checked')) {
                var total = 0;
                $('.txtFeeConfig').each( function(elem,idx) {
                    if(this.value) {
                        var amt =parseFloat($.trim(this.value));
                        if(!isNaN(amt))
                            total += amt;
                    }
                });

                if(total !== 1) {
                    App.error('Total configuration sum must be equal to 1 if percentage');
                    return false;
                }
            }
            var postObj = { url: $(form).attr('action'), data: $(form).serialize()};
            postObj.onSuccess = function(data) {
                App.success('Sharing config was successfully maintained');
              setTimeout(function () {location.reload();},500);
            };

            $('#ussdSharingDialog').modal('hide');
            $.blockUI();
            App.post(postObj);
            return false;
        }
    });
}
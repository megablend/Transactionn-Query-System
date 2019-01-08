$(function () {
    initCpayForm();
});

function initCpayForm() {
    $('#cpaySharingForm').validate({
        errorClass: 'invalid',
        validClass: 'valid',
        submitHandler: function(form) {
            var postObj = { url: $(form).attr('action'), data: $(form).serialize()};
            postObj.onSuccess = function(data) {
                App.success('Sharing config was successfully maintained');
              setTimeout(function () {location.reload();},500);
            };

            $('#cpaySharingDialog').modal('hide');
            $.blockUI();
            App.post(postObj);
            return false;
        }
    });
}
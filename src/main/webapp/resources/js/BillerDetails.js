$(function() {

    $('#settingsForm').validate({
        submitHandler: function(form) {
            var postObj = { url: $(form).attr('action'), data: $(form).serialize()};
            App.post(postObj);
        }
    });

    $('#feeForm').validate({
        submitHandler: function(form) {
            var postObj = { url: $(form).attr('action'), data: $(form).serialize()};
            App.post(postObj);
        }
    });

    $('#percentage').on('ifChanged', function (evt) {
         if(evt.target.checked)
             $('.percentageBounds').slideDown();
        else
             $('.percentageBounds').slideUp();
    });

    if($('#percentage').is(':checked'))
        $('.percentageBounds').show();
});
$( function() {

    $('#clientForm').validate({
        rules : {
            name: {
                required: true,
                minlength: 3
            },
            institutionCode:  {
              required: true,
                minlength: 6,
                maxlength: 6
            },
            maxRequestSize:{
                required: true,
                digits: true,
                min: 1
            },
            emails: {
                required: true
            }
        },
        messages: {
            name: {
                required: 'Please specify organization name',
                minlength: 'The organization name has to be greater than 3 characters'
            },
            institutionCode: {
                required: 'Please specify Institution Code',
                minlength: $.validator.format('Institution Code should be {0} digits'),
                maxlength: $.validator.format('Institution Code should be {0} digits')
            },
            emails: {
                required: 'Please specify Emails'
            }
        },
        submitHandler: function(form) {
            $('.organizationCreationDialog').modal('hide');
            var postUrl = $(form).attr('action');
            var formData = $(form).serialize();
            var postObj = {url: postUrl, data:formData};
            postObj.onSuccess = function(data) {
                    App.success('Organization was successfully created');
                    setTimeout( function() {
                        location.reload();
                    },500);
            };
            App.post(postObj);
            return false;
        },
        errorClass :'invalid',
        validClass: 'valid'
    });

});
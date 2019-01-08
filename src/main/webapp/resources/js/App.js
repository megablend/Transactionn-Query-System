var AppConstants = {};
var App = {};
$(function () {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    $(document).ajaxSend(function(e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });

    $('input[type="checkbox"].flat-red, input[type="radio"].flat-red').iCheck({
        checkboxClass: 'icheckbox_square-blue',
        radioClass: 'iradio_flat-green'
    });

    $('.mySelect2').select2();
    $('.myDataTable').DataTable({ordering: false});
    changePasswordSetup();

});

App.success = function(msg) {
    notif(
        {
            msg: msg,
            bgcolor: '#294447',
            color: '#F19C65',
            fade: true,
            multiline:true,
            zindex: 1000
        }
    );

};

App.error = function(msg) {
    notif({
        msg: msg,
        type:'error',
        multiline:true,
        zindex: 1000
    });
};

App.log = function(msg) {
    if(window.console)
        console.log(msg);
}
App.info = function(msg) {
    if(window.console)
        console.info(msg);
}
App.warn = function(msg) {
    if(window.console)
        console.warn(msg);
}
App.post = function(postObj) {
    if( !postObj || !postObj.url)
        return;
    var ajaxData = { url: postObj.url};
    if(postObj.data)
        ajaxData.data = postObj.data;
    if(postObj.dataType)
        ajaxData.dataType = postObj.dataType;

    ajaxData.method = 'POST';
    ajaxData.beforeSend = function() {
        $.blockUI();
    };

    ajaxData.error = function(xhr, status,error) {
        App.error(error);
    };
    ajaxData.complete = function() {
        $.unblockUI();
    };
    ajaxData.statusCode =   {
        404: function() {
            location.reload(); //should I be doing this?
        }
    };

    ajaxData.success = function(data) {
        if(data.status && data.status.search(/success/i) !== -1) {
            if(postObj.onSuccess)
                postObj.onSuccess(data);
            else {
                App.success(data.message);
            }
        } else {
            var msg = data.message;
            if(data.errors) {
                msg += "<ul>";
                for(var i=0;i<data.errors.length;i++){
                    msg += "<li>" + data.errors[i] + "</li>";
                }
                msg += "</ul>";
            }
            App.error(msg);
            App.log('Response from server: ' + data.toString());
        }
    };

    $.ajax(ajaxData);
};

function changePasswordSetup() {
    $('#changePasswordForm').validate({
        rules: {
            newPassword: {
                required: true,
                minlength: 8
            },
            confirmPassword: {
                required: true,
                minlength: 8,
                equalTo: '#newPassword'
            }
        },
        messages: {
            newPassword: {
                required: 'Please specify your new password',
                minlength: $.validator.format('At least {0} characters should be specified')
            },
            confirmPassword: {
                required: 'Please confirm your new password',
                minlength: $.validator.format('At least {0} characters should be specified'),
                equalTo: 'The values entered do not match'
            }
        },
        submitHandler: function(form) {
            var url = $(form).attr('action');
            var postObj = { url : url, data: $(form).serialize()};
            $('#changePasswordDialog').modal('hide');
            postObj.onSuccess = function(data) {
                if($('#userPasswordChange').size() > 0)
                    $('#userPasswordChange').remove();
                 $('#btnClosePasswordBox').attr('disabled',false);
                App.success(data.message);
                setTimeout(function() {location.reload();},500);
            };
            
            App.post(postObj);
        },
        errorClass :'invalid',
        validClass: 'valid'

    });
    
    if( $('#userPasswordChange').size() > 0) {
        $('#changePasswordDialog').modal({
            backdrop: 'static',
            keyboard: false
        });
        $('#btnClosePasswordBox').attr('disabled',true);
        $('#changePasswordDialog').modal('open');
    }
}
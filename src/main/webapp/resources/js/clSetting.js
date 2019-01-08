/**
 * Created by eoriarewo on 10/19/2017.
 */
$( function() {
   $('#settingForm').validate({
       errorClass :'invalid',
       validClass: 'valid',
       submitHandler: function(form) {
           var postUrl = $(form).attr('action');
           var formData = $(form).serialize();
           var postObj = {url: postUrl, data:formData};
           App.post(postObj);
           return false;
       }
   });
});

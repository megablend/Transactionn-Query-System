$( function() {

    initTable();

    $('#organizationForm').validate({
        rules : {
            name: {
                required: true,
                minlength: 3
            },
            organizationType: "required",
            noOfOperators:{
                required: true,
                digits: true,
            },
            noOfAdmins: {
                required: true,
                digits: true,
                min: 2
            },
            product: {
                required: function () {
                    var orgType = $('#organizationType option:selected').val();
                    return orgType && orgType != 3;//product not required for bank
                },
                minlength: 1
            },
            code : {
                required: function () {
                    var orgType = $('#organizationType option:selected').val();
                    return orgType && orgType != 1; //code is not required for merchants
                }
            }
        },
        messages: {
            name: {
                required: 'Please specify organization name',
                minlength: 'The organization name has to be greater than 3 characters'
            },
            code: 'Please specify code for bank or aggregregator',
            product:{
                required:'Please select products for organization',
                minlength: 'You have to select at least one product'
            },
            organizationType: 'Please select organization type',
            noOfOperators: {
                required: 'Please specify number of operators',
                digits: 'Only digits are allowed here'
            },
            noOfAdmins: {
                required: 'Please specify number of administrators',
                digits: 'Only digits are allowed here',
                min: $.validator.format('At least {0} Administrators required')
            }
        },
        submitHandler: function(form) {
            $('.organizationCreationDialog').modal('hide');
            var postUrl = $(form).attr('action');
            App.log('Post URL: ' + postUrl);
            var formData = $(form).serialize();
            var postObj = {url: postUrl, data:formData};
            postObj.onSuccess = function(data) {
              if(!isNaN(parseInt(data.message))) {
                  App.success('Organization was successfully created');
                  //change this  to go to proper page
                  setTimeout( function() {
                      location.href = encodeURI(location.href + '/' + data.message);
                  },500);

              }

            };
          App.post(postObj);
            return false;
        },
        errorClass :'invalid',
        validClass: 'valid'
    });

    $('#organizationType').on('change', function(evt) {
        var value = $('option:selected',this).val();
        switch(parseInt(value)) {
            case 2:
                $('.organizationCreationDialog div.bank').hide();
                $('.organizationCreationDialog div.aggregator').show();
                $('.organizationCreationDialog .prodBank').hide();
                break;
            case 3:
                $('.organizationCreationDialog div.aggregator').hide();
                $('.organizationCreationDialog div.bank').show();
                $('.organizationCreationDialog .prodBank').show();
                break;
            default:
                $('.organizationCreationDialog div.aggregator').hide();
                $('.organizationCreationDialog div.bank').hide();
                $('.organizationCreationDialog .prodBank').hide();
                break;
        }
        $('.organizationCreationDialog').modal('handleUpdate');
    });
});


function initTable() {
   var  trxnTable = $('#orgTable').DataTable({
        // autoWidth: true,
        ordering: false,
        serverSide: true,
        // dom: '<"H"<"pull-left tInfo"l><"pull-left dtSearchBox"f><"pull-right"><"pull-right"T>>t<"F"<"pull-left"i>p>',
       pagingType: 'full_numbers',
        ajax: {
            type: 'POST',
            url: TABLE_URL,
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
            }
        }
    });

   trxnTable.on('click','.details', function() {
       var orgId = $(this).attr('id');
       location.href = $.trim(DETAIL_URL) + $.trim(orgId);
       return false;
   });
}
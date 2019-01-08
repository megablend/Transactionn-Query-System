$( function() {
    $('#selAll').click( function(evt) {
        var checked = this.checked;
        $(':checkbox.accts').each(function(e,i) {
            this.checked = checked;
        } );
    });
    //todo write code for account status updating n expiry date change

    $('#btnUpdateStatus').click(updateStatus);
    $('#btnUpdateEmail').click(updateEmail);
    $('#btnDates').click(updateDates);
});

function getSelectedAccounts() {
    var ids = [];
    $(':checkbox.accts:checked').each(function(el,idx) {
        ids.push(this.id);
    });
    return ids;
}

function updateStatus(evt) {
    evt.preventDefault();

    var ids = getSelectedAccounts();
    if( ids.length == 0) {
        App.error('No account was selected');
        return;
    }

    var acctStatus = $('#accountStatus option:selected').val();
    if(!acctStatus || $.trim(acctStatus).length == 0) {
        App.error('Kindly select a valid Account Status');
        return;
    }
    $('#statusChangeDialog').modal('hide');
    var postObj = {
        data: {acctIds: ids.join(), action: acctStatus},
        url: ACCOUNT_LINK,
        onSuccess: function(data) {
            App.success('Statuses for accounts were successfully updated');
            setTimeout(function() {location.reload()}, 500);
        }
    };

    App.post(postObj);
}

function updateEmail(evt) {
    evt.preventDefault();
    var ids = getSelectedAccounts();
    if( ids.length === 0) {
        App.error('No account was selected');
        return;
    }

    var email = $('#email').val();
    if(!email || $.trim(email).length === 0) {
        App.error('No email was specified');
        return;
    }

    $('#emailDialog').modal('hide');
    var postObj = {
      data: { acctIds: ids.join(), email : email},
        url: EMAIL_LINK,
        onSuccess: function(data) {
          App.success('Account emails successfully updated');
            setTimeout(function() {location.reload()}, 500);
        }
    };

    App.post(postObj);
}

function updateDates(evt) {
    evt.preventDefault();

    var ids = getSelectedAccounts();
    if( ids.length == 0) {
        App.error('No accounts were selected');
        return;
    }

    var postObj = {
        data: {acctIds: ids, action: acctStatus},
        url: DATE_LINK,
        onSuccess: function(data) {
            App.success('Dates for accounts were successfully updated');
            setTimeout(function() {location.reload()}, 500);
        }
    };

    App.post(postObj);
}
$(function () {

    $('#userForm').validate({
        errorClass: 'invalid',
        validClass: 'valid',
        submitHandler: function (form) {
            $('#usrCreationDialog').modal('hide');
            var url = $(form).attr('action');
            var postObj = {url: url, data: $(form).serialize()};

            postObj.onSuccess = function (data) {
                App.success('User was successfully created');
                setTimeout(function () {
                    location.reload();
                }, 500);
            }
            App.post(postObj);
            return false;
        }
    });

    $('#btnResetPasswords').click(function () {
        var selectedUsers = getSelectedUsers();
        if (!selectedUsers) {
            App.error('Nothing was selected');
            return;
        } else {
            var postData = {url: ResetUserPasswordUrl, data: {userIds: selectedUsers.join()}};
            $.blockUI();
            App.post(postData);
            clearSelectedUsers();
        }

    });

    $('#btnEnableUsers').click(function (evt) {
        evt.preventDefault();
        var userIds = getSelectedUsers();
        updateUserStatus('enable', userIds);

    });

    $('#btnDisableUsers').click(function (evt) {
        evt.preventDefault();
        var userIds = getSelectedUsers();
        updateUserStatus('disable', userIds);

    });

    $('#chkAll').on('ifChanged', function (evt) {
        var newState = evt.target.checked ? "check" : "uncheck";
        $('.chkUsers').iCheck(newState);
    });
});


function getSelectedUsers() {
    if ($('.chkUsers:checked').size() == 0)
        return null;
    var arr = new Array();

    $('.chkUsers:checked').each(function (a, b) {
        arr.push(this.id);
    });
    return arr;
}

function clearSelectedUsers() {
    try {
        $('.chkUsers:checked').iCheck('uncheck');
    } catch (e) {
    }
    $('.chkUsers:checked').each(function (a, b) {
        this.checked = false;
    });
}

function updateUserStatus(action, userIds) {
    if (!userIds) {
        App.error('No users were selected');
        return;
    }
    var postData = {url: UpdateUserStatusUrl, data: {action: action, userIds: userIds.join()}};
    postData.onSuccess = function (data) {
        App.success('Action was successfully executed');
        setTimeout(function () {
            location.reload();
        }, 500);
    };

    $.blockUI();
    App.post(postData);
}
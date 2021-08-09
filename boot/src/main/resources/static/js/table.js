var Formatters = {
    gender: function (value, row, index) {
        if (value == 'MALE') {
            return '男';
        } else if (value == 'FEMALE') {
            return '女';
        } else {
            return value;
        }
    },
    face: function (value, row, index) {
        if (value) {
            return '<img width="50" class="img-rounded" src="' + ContextPath + 'rest/icf/face/' + value + '.jpeg" />';
        } else {
            return '<img width="50" class="img-rounded" src="' + ContextPath + 'static/images/avatar.png" />';
        }
    },
    sequence: function (value, row, index) {
        return (index + 1);
    },
    date: function (value, row, index) {
        if (undefined !== value) {
            return new Date(value).format('yyyy-MM-dd HH:mm');
        } else {
            return value;
        }
    },
    day: function (value, row, index) {
        if (undefined !== value) {
            return new Date(value).format('yyyy-MM-dd');
        } else {
            return value;
        }
    },
    time: function (value, row, index) {
        if (undefined !== value) {
            return new Date(value).format('HH:mm:ss');
        } else {
            return value;
        }
    },
    objName: function (value, row, index) {
        if (undefined !== value) {
            return value.name;
        } else {
            return value;
        }
    },
    defunct: function (value, row, index) {
        if (row.defunct == true) {
            return value + '<font style="color:red;">(已离职)</font>';
        } else {
            return value;
        }
    }
};

function addEntity(url, form, done) {
    var btn = $(this).attr('disabled', 'disabled');
    clearErrors(form);
    $.ajax(url, {
        dataType: "json",
        method: 'PUT',
        data: form.serializeArray()
    }).done(function (data, textStatus, jqXHR) {
        var refresh = true;
        if ('function' === typeof done) {
            refresh = done.call(this, true, data, textStatus, jqXHR);
        }
        if (true === refresh) {
            $('#table').bootstrapTable('refresh');
        }
    }).fail(function (jqXHR, textStatus, errorThrown) {
        popErrors(jqXHR.responseJSON, btn, form);
        if ('function' === typeof done) {
            done.call(this, false, errorThrown, textStatus, jqXHR);
        }
    }).always(function () {
        btn.removeAttr('disabled');
    });
}

function errorItem(content, field) {
    if (undefined === content) {
        return false;
    } else if ($.isArray(content)) {
        var errors = [];
        $.each(content, function (_, item) {
            errors.push(errorItem(item, field));
        });
        return errors;
    } else {
        return $.html.div().addClass('text-danger').append(undefined === field ? false : field + ':').append(content);
    }
}

function clearErrors(container) {
    $('.form-group', container).removeClass('has-error');
    $('.popover', container).remove();
}

function popErrors(data, actionErrorPoper, container) {
    var unknownErrors = $.html.div();
    $.each(data.fieldErrors, function (field, errors) {
        var input = $('.form-control[name=' + field + ']', container);
        if (input.length == 0) {
            unknownErrors.append(errorItem(errors, field));
        } else {
            input.parents('.form-group').addClass('has-error');
            var poper = input.parents('.error-poper');
            doPopover(poper.length > 0 ? poper : input, errorItem(errors), container);
        }
    });
    unknownErrors.append(errorItem(data.actionErrors));
    if (unknownErrors.children().length > 0) {
        doPopover(actionErrorPoper, unknownErrors, container);
    }
}

function doPopover(poper, content, container) {
    poper.popover({
        container: undefined !== container ? container : false,
        content: content,
        html: true,
        trigger: 'manual'
    }).popover('show');
}

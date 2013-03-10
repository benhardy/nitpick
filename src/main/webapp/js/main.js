/**
 * Functions for use on the Main page.
 * Mainly deals with review lists.
 * Requires jQuery to be already loaded.
 *
 */
var mainScripts = function() {

    var hideCreateDialog = function(e) {
        $("#dialog_overlay").hide();
        $("#create_dialog").hide();
    };

    var showCreateDialog = function(e) {
       $("#dialog_overlay").show();
       $("#create_dialog").fadeIn(300);
       // close the dialog when anywhere outside it is clicked
       $("#dialog_overlay").click(hideCreateDialog);
    };

    /**
     * Set up actions for Create Review dialog.
     * When the Create Review link gets clicked, open
     * the Create Review dialog.
     */
    var onLoadHandler = function() {
        $("#a_create_review").click(showCreateDialog);

        $('form[name=new_review_form]').submit(function(e){
            e.preventDefault();
            $.ajax({
                type: 'POST',
                cache: false,
                url: '/review/new',
                data: 'id=header_contact_send&'+$(this).serialize()
            })
            .done(function(data) {
                if (data.reviewId) {
                    window.location.href = "/review/"+data.reviewId;
                }
            })
            .fail(function(xhr, textStatus, errorThrown) {
                $("#result").html(xhr.responseText);
            });
        });
    };

    return {
        "onLoadHandler": onLoadHandler
    };
}();


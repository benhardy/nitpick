/**
 * Functions for use on the Main page.
 * Mainly deals with review lists.
 * Requires jQuery to be already loaded.
 *
 */
var mainScripts = function() {

    /* set up actions for Create Review dialog */
    var onLoadHandler = function() {
        $("#a_create_review").click(function(e) {
            $("#dialog_overlay").show();
            $("#create_dialog").fadeIn(300);
            $("#dialog_overlay").click(function(e) {
                $("#dialog_overlay").hide();
                $("#create_dialog").hide();
            });
        });
    };

    return {
        "onLoadHandler": onLoadHandler
    };
}();


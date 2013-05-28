/**
 * Functions for use on the Review page. Pertains to a single review.
 * Requires jQuery to be already loaded.
 *
 * Requires parameter reviewScriptsForId -
 * given a reviewId returns a map of functions for operating on that reviewId
 */
function reviewScriptsForId(reviewId) {

    function foreach(items, action) {
        for (var i = 0; i < items.length; i++) {
            action(items[i]);
        }
    }

    function map(transform, items) {
        var result = [];
        foreach(items, function (item) {
            result.push(transform(item));
        });
        return result;
    }

    /**
     * Transform the affected path tree into a nested unordered list.
     * Calls itself recursively on sub-trees to make sub-lists.
     * The root node will be returned as a naked "li" tag.
     */
    function pathToListItem(path) {
        console.log(path);
        var item = document.createElement('li');
        var itemText = document.createTextNode(path.name);
        var itemLink = document.createElement('a');
        itemLink.appendChild(itemText);
        item.appendChild(itemLink);
        kids = path.children || path.trees
        if (kids && kids.length > 0) { // it's a directory, even if this is empty
            var kidList = document.createElement('ul');
            foreach(kids, function(child){
                kidList.appendChild(pathToListItem(child))
            });
            item.appendChild(kidList);
            item.className = "folder";
        } else {
            item.className = "file";
        }
        return item;
    }

    /**
     * Callback for handling incoming file tree data, creates HTML
     * and sets up behaviours for collapsing subdirectories.
     */
    function affectedFileListHandler(pathTreeData, status, req) {
        $("#fileList").children('ul').append(pathToListItem(pathTreeData));

        // Find list items representing folders and turn them
        // into links that can expand/collapse the tree leaf.
        $('#fileList li.folder').each(function(i) {
            var sublist = $(this).children('ul');
            $(this).children('a').click(function() {
                // Make the anchor toggle the leaf display.
                sublist.toggle();
            });
        });
        $('#fileList li.file').each(function(i) {
            $(this).children('a').click(function() {
                // TODO display diff for this file
            });
        });

    }

    function onLoadHandler() {
        $("fileList").empty();
        $.getJSON("/review/"+reviewId+"/change-summary", affectedFileListHandler);
    }

    return {
        "onLoadHandler": onLoadHandler
    };
}

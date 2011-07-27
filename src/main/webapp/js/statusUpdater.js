ajaxUpdaters.subProjectViewAjaxUpdater.registerUpdater('status',{
    extractFunction: function(node) {
        var size = jQuery(node).children('img').attr('class');
        return { 'size': size }
    },
    updateFunction: function(node,status) {
        jQuery(node).children('img').each(function() {
            var imgTag = this;
            imgTag.alt = status.description;
            imgTag.src = status.image;
            imgTag.tooltip = status.description;
        })
    }
});

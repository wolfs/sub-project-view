var ajaxUpdater = function(spec) {
    var my = {};
    my.jsProxy = spec.jsProxy;
    my.extraArgs = {
        name: 'status',
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
    }
    my.updaters = {};

    var that = {};

    that.registerUpdater = function(name,updater) {
        my.updaters[name] = updater;
    }

    that.ajaxUpdate = function() {
        var jobList = new Array();
        jQuery("td[jobname]").each(function() {
            var toSend = {
                'jobname': this.getAttribute('jobname')
            }
            for (updaterName in my.updaters) {
                if (my.updaters.hasOwnProperty(updaterName)) {
                    toSend[updaterName] = my.updaters[updaterName].extractFunction(this);
                }
            }
            jobList.push(toSend);
        });
        my.jsProxy.areRunningBalls(jobList, function(t){
          var result = t.responseObject();
          jQuery("td[jobname]").each(function() {
            var jobname = this.getAttribute('jobname');
            for (updaterName in my.updaters) {
               if (my.updaters.hasOwnProperty(updaterName)) {
                   var updater = my.updaters[updater];
                   updater.updateFunction(this,result[jobname][updaterName]);
               }
            }
          });
          setTimeout(function() {that.ajaxUpdate()},4000);
        });
    }
    return that;
}

// spec must contain the property jsProxy
// which will be used to call the backend.
var ajaxUpdaters = {
    newUpdater: function(spec) {
        var my = spec || {};
        my.updaters = {};

        var that = {};

        that.registerUpdater = function(name,updater) {
            my.updaters[name] = updater;
        };

//        that.registerUpdater('status',{
//            extractFunction: function(node) {
//                var size = jQuery(node).children('img').attr('class');
//                return { 'size': size }
//            },
//            updateFunction: function(node,status) {
//                jQuery(node).children('img').each(function() {
//                    var imgTag = this;
//                    imgTag.alt = status.description;
//                    imgTag.src = status.image;
//                    imgTag.tooltip = status.description;
//                })
//            }
//        });

        that.ajaxUpdate = function() {
            var jobList = new Array();
            jQuery("td[jobname]").each(function() {
                var toSend = {
                    'jobname': this.getAttribute('jobname')
                };
                for (var updaterName in my.updaters) {
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
                    for (var updaterName in my.updaters) {
                        if (my.updaters.hasOwnProperty(updaterName)) {
                            var updater = my.updaters[updaterName];
                            updater.updateFunction(this,result[jobname][updaterName]);
                        }
                    }
                });
                setTimeout(function() {that.ajaxUpdate()},4000);
            });
        };
        return that;
    }
};

var m = require('mithril');

module.exports = function(ctrl) {

    var drawMentionedNotification = function(notification) {
        var content = notification.content;

        return m('div', {}, 'mentioned');
    };

    var drawNotification = function (notification) {
        switch (notification.type) {
            case "mentioned" : return drawMentionedNotification(notification);
            default: console.error("unhandled notification");
        }
    };

    function recentNotifications(ctrl) {
        return ctrl.data.map(drawNotification);
    }

    return m('div', {}, recentNotifications(ctrl));
};

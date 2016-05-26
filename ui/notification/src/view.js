var m = require('mithril');

module.exports = function(ctrl) {

    var drawMentionedNotification = function(notification) {



        var content = notification.content;
        var category = content.category;
        var topic = content.topic;
        var mentionedBy = content.mentionedBy;

        var mentionedByProfile = location.origin + "/@/" + mentionedBy;
        var postUrl = location.origin + "/forum/" + category + "/" + topic;

        console.dir(content);

        return m('div', {}, [
                m('span', {}, 'You were mentioned by '),
                m('a', {href: mentionedByProfile}, mentionedBy),
                m('span', {}, ' in a '),
                m('a', {href: postUrl},'forum thread.')
            ]
        );
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

    return m('div', {class: "site_notifications_box"}, recentNotifications(ctrl));
};

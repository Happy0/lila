var m = require('mithril');

module.exports = function(ctrl) {

    var drawMentionedNotification = function(notification) {

        var content = notification.content;
        var category = content.category;
        var topic = content.topic;
        var mentionedBy = content.mentionedBy;
        var postId = content.postId;

        var mentionedByProfile = location.origin + "/@/" + mentionedBy;
        var postUrl = location.origin + "/forum/redirect/post/" + postId;

        return m('div', [
                m('a', {href: mentionedByProfile}, mentionedBy),
                m('span', ' mentioned you in the '),
                m('a', {href: postUrl, class: "forum_post_link"}, "[" + topic + "]"),
                m('span', ' thread')
            ]
        );
    };

    var drawNotification = function (notification) {
        var content = null;
        switch (notification.type) {
            case "mentioned" : content =  drawMentionedNotification(notification); break;
            default: console.error("unhandled notification");
        }

        console.dir(notification);
        return m('div', {class: 'site_notification'}, content);
    };

    function recentNotifications(ctrl) {
        return ctrl.data.map(drawNotification);
    }

    return m('div', {class: "site_notifications_box"}, recentNotifications(ctrl));
};

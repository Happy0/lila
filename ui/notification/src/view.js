var m = require('mithril');

    var drawMentionedNotification = function(notification) {

        var content = notification.content;
        var category = content.category;
        var topic = content.topic;
        var mentionedBy = content.mentionedBy.name;
        var postId = content.postId;

        var mentionedByProfile = location.origin + "/@/" + mentionedBy;
        var postUrl = "/forum/redirect/post/" + postId;

        return m('div', [
                m('a', {href: mentionedByProfile}, mentionedBy),
                m('span', ' mentioned you in the '),
                m('a', {href: postUrl, class: "forum_post_link"}, topic),
                m('span', ' forum thread')
            ]
        );
    };

    var drawNotification = function (notification) {
        var content = null;
        switch (notification.type) {
            case "mentioned" : content =  drawMentionedNotification(notification); break;
            default: console.dir(notification); console.error("unhandled notification"); break;
        }

        console.dir(notification);
        return m('div', {class: 'site_notification'}, content);
    };

    function recentNotifications(ctrl) {
        return ctrl.data.map(drawNotification);
    }

module.exports = function(ctrl) {

    if (ctrl.vm.initiating) return m('div.initiating', m.trust(lichess.spinnerHtml));

    return m('div', {class: "site_notifications_box"}, recentNotifications(ctrl));
};

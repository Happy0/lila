var xhr = require('./xhr');

module.exports = function(env) {

    var data = {};

    this.vm = {
        initiating: true,
        reloading: false
    };

    this.resetNotificationCount = function() {
    }

    this.setInitialNotifications = function(data) {
        console.info("data");
        console.dir(data);

        this.vm.initiating = false;
        this.vm.reloading = false;
        this.data = data;
    }.bind(this);

    this.markAllRead = function () {
        xhr.markAllRead();
        env.resetNotificationCount();
    }.bind(this);

    this.newNotificationReceived = function(newNotification) {
        data.unshift(newNotification);

        // We only show the most recent notifications - the user should click 'see more' if they want
        // to see older notifications
        if (data.length > env.maxNotifications) data.pop();

    }.bind(this);

    xhr.load().then(this.setInitialNotifications);
};

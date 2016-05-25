var xhr = require('./xhr');

module.exports = function(env) {

    var data = {};

    this.vm = {
        initiating: true,
        reloading: false
    };

    this.resetNotificationCount = function() {
    }

    this.update = function(data) {
        console.info("data");
        console.dir(data);

        this.vm.initiating = false;
        this.vm.reloading = false;
        this.data = data;

        xhr.
        env.resetNotificationCount();
    }.bind(this)

    xhr.load().then(this.update);
};

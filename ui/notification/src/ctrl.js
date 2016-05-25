var xhr = require('./xhr');

module.exports = function(env) {

    var data = {};

    this.update = function(data) {
        this.data = data;
    }


    xhr.load().then(this.update);
};

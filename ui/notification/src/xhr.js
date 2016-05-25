var m = require('mithril');

var xhrConfig = function(xhr) {
  xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
  xhr.setRequestHeader('Accept', 'application/vnd.lichess.v1+json');
}


module.exports = {
    load: function() {
        return m.request({
            method: 'GET',
            url: uncache('/notification'),
            config: xhrConfig

        });
    }
};

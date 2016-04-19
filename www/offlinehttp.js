
  var argscheck = require('cordova/argscheck'),
    exec = require('cordova/exec');

  module.exports = {

    getThumbnail: function (uri, maxWidth, maxHeight, quality, successCallback, errorCallback) {
      exec(successCallback, errorCallback, 'Thumbnail', 'getThumbnail', [uri, maxWidth, maxHeight, quality]);
    }

  };

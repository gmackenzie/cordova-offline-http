
	module.exports = {
		getAllPreferences: function( success, fail ) {
			try {
				cordova.exec(success, fail, "Preferences", "getAllPreferences", []);
			} catch (e) {
            }
       	}
	};

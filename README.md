# Preferences plugin

### Calls

**_plugins.preferences.getAllPreferences( success, fail );_**
```
plugins.preferences.getAllPreferences(function(result) { 
	console.log(JSON.stringify(result));
}, function(error) { 
	console.error(result); 
});

{"test2":"false","foo":"bar","loglevel":"DEBUG"}
```

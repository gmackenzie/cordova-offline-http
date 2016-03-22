package com.commontime.cordova.plugins.preferences;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class Preferences extends CordovaPlugin {

	private static final String TAG = "Preferences";
	private static final String ACTION_GET_ALL_PREFERENCES = "getAllPreferences";
    private JSONObject preferencesJson;

    @Override
	protected void pluginInitialize() {
        preferencesJson = new JSONObject();
        Map<String, String> map = preferences.getAll();
        for( String key : map.keySet() ) {
            try {
                preferencesJson.put(key, map.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
	}

	@Override
	public void onDestroy() {
	}

	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		if (action.equals(ACTION_GET_ALL_PREFERENCES)) {
			callbackContext.success(preferencesJson);
			return true;
		}

		return false;
	}
}

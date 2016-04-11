package com.commontime.cordova.plugins.offlinehttp;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OfflineHTTP extends CordovaPlugin {

	private static final String TAG = "OfflineHTTP";

	private static final String ACTION_ADD_REQUEST = "addRequest";
    private static final String ACTION_GET_RESPONSE = "getResponse";

    HashMap<String, List<CallbackContext>> callbacks;
//    private ResponseReceiver responseReceiver;
//    private boolean responseReceiverStarted = false;

    public static Bus bus = new Bus(ThreadEnforcer.ANY);

    @Override
	protected void pluginInitialize() {
        callbacks = new HashMap<String, List<CallbackContext>>();

//        responseReceiver = new ResponseReceiver();
//        responseReceiverStarted = true;
//        cordova.getActivity().registerReceiver(new ResponseReceiver(), new IntentFilter(OfflineHTTPService.ACTION_RESPONSE_RECEIVED));

        bus.register(this);
	}

    @Override
    public void onReset() {
        callbacks = new HashMap<String, List<CallbackContext>>();
    }

	@Override
	public void onDestroy() {
//        if( responseReceiverStarted )
//            cordova.getActivity().unregisterReceiver(responseReceiver);
//
//        responseReceiverStarted = false;
	}

	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		if (action.equals(ACTION_ADD_REQUEST)) {

            JSONObject jsonObject = args.getJSONObject(0);
            String id = jsonObject.getString("id");
            int method = jsonObject.getInt("method");
            String uri = jsonObject.getString("uri");

            OfflineHTTPService.addDownload(cordova.getActivity(), id, method, uri);
			callbackContext.success();
			return true;
		}
        if (action.equals(ACTION_GET_RESPONSE)) {
            JSONObject jsonObject = args.getJSONObject(0);
            String id = jsonObject.getString("id");

            if( !callbacks.containsKey(id) )
                callbacks.put(id, new ArrayList<CallbackContext>());

            callbacks.get(id).add(callbackContext);

            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);

            return true;
        }

		return false;
	}

    @Subscribe
    public void getMessage(Request r) {
        List<CallbackContext> list = callbacks.get(r.getId());
        if( list != null )
        for( CallbackContext cc : list ) {
            try {
                cc.success(r.toJSON());
            } catch (JSONException e) {
                e.printStackTrace();
                cc.error(e.getMessage());
            }
        }
    }

//	public class ResponseReceiver extends BroadcastReceiver {
//		public ResponseReceiver() {
//		}
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//            String id = intent.getStringExtra(OfflineHTTPService.PARAM_REQUEST_ID);
//
//            for( CallbackContext cc : callbacks.get(id) ) {
//
//            }
//		}
//	}
}

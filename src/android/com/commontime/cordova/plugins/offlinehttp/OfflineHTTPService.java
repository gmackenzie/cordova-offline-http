package com.commontime.cordova.plugins.offlinehttp;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class OfflineHTTPService extends IntentService {

    static final String ACTION_START = "com.commontime.cordova.plugins.offlinehttp.action.Start";
    static final String ACTION_ADD_DOWNLOAD = "com.commontime.cordova.plugins.offlinehttp.action.AddDownload";
    static final String ACTION_RESPONSE_RECEIVED = "com.commontime.cordova.plugins.offlinehttp.action.ResponseReceived";

    static final String PARAM_METHOD = "com.commontime.cordova.plugins.offlinehttp.param.Method";
    static final String PARAM_URI = "com.commontime.cordova.plugins.offlinehttp.param.Uri";
    static final String PARAM_REQUEST_ID = "com.commontime.cordova.plugins.offlinehttp.param.RequestId";

    List<Request> requestList;
    private JsonFactory jfactory;
    private RequestQueue requestQueue;

    public OfflineHTTPService() {
        super("OfflineHTTPService");
        requestList = new CopyOnWriteArrayList<Request>();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, OfflineHTTPService.class);
        intent.setAction(ACTION_START);
        context.startService(intent);
    }

    public static void addDownload(Context context, String id, int method, String uri) {
        Intent intent = new Intent(context, OfflineHTTPService.class);
        intent.putExtra(PARAM_REQUEST_ID, id);
        intent.putExtra(PARAM_METHOD, method);
        intent.putExtra(PARAM_URI, uri);
        intent.setAction(ACTION_ADD_DOWNLOAD);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        requestList = new ArrayList<Request>();
        jfactory = new JsonFactory();
        requestQueue = Volley.newRequestQueue(this);

        try {
            readJSON();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void writeJSON() throws IOException {

        File f = new File( getFilesDir(), "requests.json" );
        JsonGenerator jGenerator = jfactory.createGenerator(f, JsonEncoding.UTF8 );

        jGenerator.writeStartArray();

        for (Request r : requestList) {
            r.writeRequestJSON(jGenerator);
        }

        jGenerator.writeEndArray();

        jGenerator.close();
    }

    private synchronized void readJSON() throws IOException {

        requestList = new ArrayList<Request>();

        File f = new File( getFilesDir(), "requests.json" );
        String x = FileUtils.readFileToString(f);

        if(x.length() == 0)
            return;

        JsonParser jParser = jfactory.createParser(f);

        jParser.nextToken();
        JsonToken t1 = jParser.getCurrentToken();
        while (jParser.nextToken() != JsonToken.END_ARRAY) {
            Request r = new Request("");
            r.readRequestJSON(jParser);
            requestList.add(r);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                handleStartAction();
            }
            if (ACTION_ADD_DOWNLOAD.equals(action)) {
                int method = intent.getIntExtra(PARAM_METHOD, 0);
                String uri = intent.getStringExtra(PARAM_URI);
                String id = intent.getStringExtra(PARAM_REQUEST_ID);
                handleAddDownloadAction(id, method, uri);
            }
        }
    }

    private void handleAddDownloadAction(String id, int method, String uri) {
        Request r = new Request(id);
        r.setMethod(method);
        r.setUri(uri);
        requestList.add(r);

        try {
            writeJSON();
        } catch (IOException e) {
            e.printStackTrace();
        }

        handleStartAction();
    }

    private void handleStartAction() {
        for (final Request r : requestList) {

            if( r.complete() )
                continue;

            final CountDownLatch countDownLatch = new CountDownLatch(1);
            final boolean[] currentRequestSuccess = new boolean[1];

            StringRequest stringRequest = new StringRequest(r.getMethod(), r.getUri(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            r.setResponse(response);
                            r.setComplete(true);
                            countDownLatch.countDown();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError e) {
                            r.setError(e.toString());
                            if( e.networkResponse != null ) {
                                try {
                                    r.setResponse(new String( e.networkResponse.data, "UTF_8"));
                                } catch (UnsupportedEncodingException e1) {
                                    e1.printStackTrace();
                                }
                                r.setStatusCode(e.networkResponse.statusCode);
                            }
                            countDownLatch.countDown();
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    return params;
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return r.getBody().getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    r.setStatusCode(response.statusCode);

                    return Response.success(new String(response.data), HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            stringRequest.setTag(r.getId());
            requestQueue.add(stringRequest);
            r.incrementAttempts();

            try {
                boolean result = countDownLatch.await(1, TimeUnit.MINUTES);
                if( result ) {

                } else {
                    r.setComplete(false);
                }
                writeJSON();

                OfflineHTTP.bus.post(r);

//                Intent i = new Intent(this, OfflineHTTP.ResponseReceiver.class);
//                i.setAction(ACTION_RESPONSE_RECEIVED);
//                i.putExtra(PARAM_REQUEST_ID, r.getId());
//                sendBroadcast(i);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}

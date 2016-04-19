package com.commontime.cordova.plugins.offlinehttp;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Request {
    private String uri = "";
    private String id = "";
    private int method;
    private Map<String, String> extraHeaders = new HashMap<String, String>();
    private String body = "";
    private int attempts;
    private boolean complete = false;
    private int statusCode;
    private String response = "";
    private String error = "";

    public Request( String id ) {
        this.id = id;
    }

    public JSONObject toJSON() throws JSONException {

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JsonGenerator jGenerator = new JsonFactory().createGenerator(baos);
            writeRequestJSON(jGenerator);
            jGenerator.close();

            JSONObject jsonObject = new JSONObject(baos.toString("UTF_8"));
            return jsonObject;

        } catch (IOException e) {
            e.printStackTrace();
            throw new JSONException(e.getMessage());
        }
    }

    public static Request fromJSON(JSONObject jsonObject) throws JSONException {
        String id = jsonObject.getString("id");
        String uri = jsonObject.getString("uri");
        int method = jsonObject.getInt("method");
        Request request = new Request(id);
        request.setUri(uri);
        request.setMethod(method);
        return request;
    }

    synchronized void writeRequestJSON(JsonGenerator jGenerator) throws IOException {

        jGenerator.writeStartObject();

        jGenerator.writeNumberField("method", getMethod());
        jGenerator.writeStringField("uri", getUri());
        jGenerator.writeBooleanField("complete", complete());
        jGenerator.writeNumberField("statusCode", getStatusCode());
        jGenerator.writeStringField("id", getId());
        jGenerator.writeStringField("body", getBody());
        jGenerator.writeNumberField("attempts", getAttempts());
        jGenerator.writeStringField("response", getResponse());
        jGenerator.writeStringField("error", getError());

        jGenerator.writeObjectFieldStart("extraHeaders");
        for (String name : getExtraHeaders().keySet()) {
            jGenerator.writeStringField(name, getExtraHeaders().get(name));
        }
        jGenerator.writeEndObject();

        jGenerator.writeEndObject();

        jGenerator.flush();
    }

    public void readRequestJSON(JsonParser jParser) throws IOException {

        JsonToken t = jParser.getCurrentToken();
        while (jParser.nextToken() != JsonToken.END_OBJECT) {

            String fieldname = jParser.getCurrentName();
            jParser.nextToken();

            if ("method".equals(fieldname)) {
                setMethod(jParser.getIntValue());
            } else if ("uri".equals(fieldname)) {
                setUri(jParser.getValueAsString());
            } else if ("complete".equals(fieldname)) {
                setComplete(jParser.getValueAsBoolean());
            } else if ("statusCode".equals(fieldname)) {
                setStatusCode(jParser.getValueAsInt());
            } else if ("id".equals(fieldname)) {
                setId(jParser.getValueAsString());
            } else if ("body".equals(fieldname)) {
                setBody(jParser.getValueAsString());
            } else if ("attempts".equals(fieldname)) {
                setAttempts(jParser.getValueAsInt());
            } else if ("response".equals(fieldname)) {
                setResponse(jParser.getValueAsString());
            } else if ("error".equals(fieldname)) {
                setError(jParser.getValueAsString());
            } else if ("extraHeaders".equals(fieldname)) {
                JsonToken tx = jParser.getCurrentToken();
                while (jParser.nextToken() != JsonToken.END_OBJECT) {
                    String name = jParser.getCurrentName();
                    jParser.nextToken();
                    String value = jParser.getValueAsString();
                    addExtraHeader(name, value);
                }
            }
        }
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public int getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri( String uri ) {
        this.uri = new String(uri);
    }

    public String getBody() {
        return body;
    }

    public String getId() {
        return id;
    }

    public boolean complete() {
        return false;
    }

    public void setComplete(boolean b) {
        complete = true;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, String> getExtraHeaders() {
        return extraHeaders;
    }

    public int getAttempts() {
        return attempts;
    }

    public String getResponse() {
        return response;
    }

    public String getError() {
        return error;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public void addExtraHeader(String name, String value) {
        extraHeaders.put(name, value);
    }

    public void incrementAttempts() {
        attempts++;
    }


}

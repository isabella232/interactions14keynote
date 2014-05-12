package com.inin.gearphoneapp.app.icws;

import android.os.AsyncTask;
import android.os.Looper;
import android.support.v7.appcompat.R;
import android.util.Log;

import com.inin.gearphoneapp.app.util.HelperModel;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;
import org.json.JSONArray;

import org.json.JSONException;
import java.io.IOException;
import java.io.InputStream;


import org.json.JSONObject;

/**
 * Created by kevin.glinski on 3/25/14.
 */
public class IcwsClient {
    private String _csrfToken;
    private String _cookie;
    private String _sessionId;
    private String _server;

    public static IcwsClient instance;

    public IcwsClient(String server, String sessionId, String cookie,String csrfToken){
        _csrfToken = csrfToken;
        _cookie = cookie;
        _sessionId = sessionId;
        _server = server;

        instance = this;
    }

    protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
        InputStream in = entity.getContent();


        StringBuffer out = new StringBuffer();
        int n = 1;
        while (n>0) {
            byte[] b = new byte[4096];
            n =  in.read(b);


            if (n>0) out.append(new String(b, 0, n));
        }


        return out.toString();
    }

    private HttpResponse performOperation(AbstractHttpMessage request) throws IOException
    {
        request.setHeader("ININ-ICWS-CSRF-Token",_csrfToken);
        request.setHeader("Cookie", _cookie);

        HttpClient httpClient = new DefaultHttpClient();

        HttpContext localContext = new BasicHttpContext();

        HttpResponse response = httpClient.execute((HttpUriRequest)request, localContext);
        return response;
    }

    public JSONObject get(String url)
    {
        String requestUrl = String.format("%s/icws/%s%s", _server, _sessionId, url);
        Log.d(HelperModel.TAG_ICWS, "GET: " + requestUrl);
        HttpGet get = new HttpGet(requestUrl);
        String text = null;

        try {
            HttpResponse response = performOperation(get);
            HttpEntity entity = response.getEntity();

            text = getASCIIContentFromEntity(entity);
            JSONObject obj = new JSONObject(text);

            return obj;

        } catch (Exception e) {
            Log.e(HelperModel.TAG_ICWS, "General error.", e);
        }
        return new JSONObject();
    }

    public JSONArray getArray(String url)
    {
        String requestUrl = String.format("%s/icws/%s%s", _server, _sessionId, url);
        // Supress the trace if it's the messaging message; it fires every second
        if (!url.contains("messaging/messages")){
            Log.d(HelperModel.TAG_ICWS, "GET(array): " + requestUrl);
        }
        HttpGet get = new HttpGet(requestUrl);

        String text = null;

        try {
            HttpResponse response = performOperation(get);
            HttpEntity entity = response.getEntity();

            text = getASCIIContentFromEntity(entity);
            JSONArray obj = new JSONArray(text);

            return obj;

        } catch (Exception e) {
            Log.e(HelperModel.TAG_ICWS, "General error.", e);
        }
        return new JSONArray();
    }


    public void post(String url, JSONObject data){
        // This must be done off the UI thread because Android will flat out block usage of certain classes on the UI thread.
        new PostAsync().execute(new Object[]{url, data});
    }

    private class PostAsync extends AsyncTask<Object[], Integer, Integer>{
        @Override
        protected Integer doInBackground(Object[]... objects) {
            try {
                // This ends up being an array of objects...
                String url = objects[0][0].toString();
                JSONObject data = (JSONObject) objects[0][1];

                String requestUrl = String.format("%s/icws/%s%s", _server, _sessionId, url);
                Log.d(HelperModel.TAG_ICWS, "POST: " + requestUrl);
                HttpPost post = new HttpPost(requestUrl);
                post.setEntity(new ByteArrayEntity(data.toString().getBytes("UTF8")));
                HttpResponse response = performOperation(post);
            }
            catch (Exception e) {
                Log.e(HelperModel.TAG_ICWS, "Error on PUT.", e);
                return -1;
            }
            return 0;
        }
    }

    public void put(String url, JSONObject data){
        // This must be done off the UI thread because Android will flat out block usage of certain classes on the UI thread.
        new PutAsync().execute(new Object[]{url, data});
    }

    private class PutAsync extends AsyncTask<Object[], Integer, Integer>{
        @Override
        protected Integer doInBackground(Object[]... objects) {
            try {
                // This ends up being an array of objects...
                String url = objects[0][0].toString();
                JSONObject data = (JSONObject) objects[0][1];

                String requestUrl = String.format("%s/icws/%s%s", _server, _sessionId, url);
                Log.d(HelperModel.TAG_ICWS, "PUT: " + requestUrl);
                HttpPut put = new HttpPut(requestUrl);
                put.setEntity(new ByteArrayEntity(data.toString().getBytes("UTF8")));
                HttpResponse response = performOperation(put);
            }
            catch (Exception e) {
                Log.e(HelperModel.TAG_ICWS, "Error on PUT.", e);
                return -1;
            }
            return 0;
        }
    }




}

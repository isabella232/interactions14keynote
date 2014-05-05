package com.inin.gearphoneapp.app.icws;

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
        HttpGet get = new HttpGet(String.format("%s/icws/%s%s", _server, _sessionId, url));
        String text = null;

        try {
            HttpResponse response = performOperation(get);
            HttpEntity entity = response.getEntity();

            text = getASCIIContentFromEntity(entity);
            JSONObject obj = new JSONObject(text);

            return obj;

        } catch (Exception e) {
            return null;// e.getLocalizedMessage();
        }
    }

    public JSONArray getArray(String url)
    {
        HttpGet get = new HttpGet(String.format("%s/icws/%s%s", _server, _sessionId, url));
        String text = null;

        try {
            HttpResponse response = performOperation(get);
            HttpEntity entity = response.getEntity();

            text = getASCIIContentFromEntity(entity);
            JSONArray obj = new JSONArray(text);

            return obj;

        } catch (Exception e) {
            return null;// e.getLocalizedMessage();
        }
    }


    public void post(String url, JSONObject data)
    {
        HttpPost post = new HttpPost(String.format("%s/icws/%s%s", _server, _sessionId, url));

        try {
            post.setEntity(new ByteArrayEntity(data.toString().getBytes("UTF8")));
            HttpResponse response = performOperation(post);
            return;
        } catch (Exception e) {
            return;// e.getLocalizedMessage();
        }
    }

    public void put(String url, JSONObject data)
    {
        HttpPut put = new HttpPut(String.format("%s/icws/%s%s", _server, _sessionId, url));

        try {
            put.setEntity(new ByteArrayEntity(data.toString().getBytes("UTF8")));
            HttpResponse response = performOperation(put);
            return;
        }
        catch (Exception e) {
            return;
        }

    }

}

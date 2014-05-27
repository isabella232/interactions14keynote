package com.inin.gearphoneapp.app.icws;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import java.io.IOException;
import java.io.InputStream;
import org.json.JSONObject;
import org.apache.http.entity.ByteArrayEntity;

import com.inin.gearphoneapp.app.MainActivity;
import com.inin.gearphoneapp.app.util.AppLog;
import android.telephony.TelephonyManager;

/**
 * Created by kevin.glinski on 3/25/14.
 */
public class ConnectionService {

    String TAG = "ConnectionService";

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


    public IcwsClient connect(String user, String password, String server, String devicePhoneNumber){
        HttpClient httpClient = new DefaultHttpClient();

        HttpContext localContext = new BasicHttpContext();
        AppLog.d("","Connecting to " + server);
        HttpPost post = new HttpPost(String.format("%s/icws/connection", server));
        String text = null;

        try {
            JSONObject data = new JSONObject();
            data.put("__type", "urn:inin.com:connection:icAuthConnectionRequestSettings");
            data.put("applicationName", "Interactions Demo");
            data.put("userID", user);
            data.put("password", password);

            String dataString = data.toString();
            post.setEntity(new ByteArrayEntity(dataString.getBytes("UTF8")));

            post.setHeader("Accept-Language", "en-us");
            HttpResponse response = httpClient.execute(post, localContext);
            HttpEntity entity = response.getEntity();

            text = getASCIIContentFromEntity(entity);
            JSONObject obj = new JSONObject(text);

            if(obj.has("errorCode")){
                AppLog.e(TAG, obj.getString("message"));

                MainActivity.setCicConnectionState(false);
                return null;
            }


            String csrfToken = obj.getString("csrfToken");
            String cookie = response.getFirstHeader("set-cookie").getValue();
            String sessionId = obj.getString("sessionId");
            AppLog.d(TAG,"Connected Session: " + sessionId);

            IcwsClient client = new IcwsClient(server,sessionId,cookie,csrfToken);

            JSONObject stationData = new JSONObject();
            data.put("__type", "urn:inin.com:connection:remoteNumberSettings");
            data.put("remoteNumber", devicePhoneNumber );
           // client.put("/connection/station", data);


            MainActivity.setCicConnectionState(true);

            return client;

        } catch (Exception e) {
            AppLog.d(TAG,"unable to connect to the server " + e);
            return null;// e.getLocalizedMessage();
        }
    }
}

package com.example.serverutveckling.labb4;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.serverutveckling.labb4.gcm.QuickstartPreferences;
import com.example.serverutveckling.labb4.gcm.RegistrationIntentService;
import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by Julia on 2016-01-18.
 */
public class ListViewAdapter extends BaseAdapter {
    ArrayList<User> users = new ArrayList<>();
    public Context context;
    public String id;

    public ListViewAdapter(Context context, ArrayList<User> users, String id) {
        this.users = users;
        this.context = context;
        this.id = id;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.simple_item_arraylist,parent,false);
        TextView t = (TextView) rowView.findViewById(R.id.username);
        t.setText(users.get(position).getUsername());
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new WebServiceCall().execute(users.get(position).getFacebookId());
            }
        });
        return rowView;
    }

    public static HttpResponse makeRequest(String uri, String json) {
        try {
            HttpPost httpPost = new HttpPost(uri);
            HttpParams httpParameters = new BasicHttpParams();
            int timeoutConnection = 3000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 3000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            httpPost.setParams(httpParameters);
            httpPost.setEntity(new StringEntity(json));
            httpPost.setHeader("Accept", "text/plain");
            httpPost.setHeader("Content-type", "text/plain");
            return new DefaultHttpClient().execute(httpPost);
        } catch (UnsupportedEncodingException e) {
            Log.e("ERROR", e.getMessage() + "");
        } catch (ClientProtocolException e) {
            Log.e("ERROR", e.getMessage() + "");
        } catch (IOException e) {
            Log.e("ERROR", e.getMessage() + "");
        }
        return null;
    }

    private class WebServiceCall extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                String facebookIdTo = params[0];
                ResultGson rg = new ResultGson();
                Gson gson = new Gson();
                Login_id fr = new Login_id();
                fr.username = id;
                fr.password = facebookIdTo;
                rg.Json = gson.toJson(fr, Login_id.class);
                String json = gson.toJson(rg, ResultGson.class);
                //  String json = new GsonBuilder().create().toJson(user, UserGson.class);
                //   HttpResponse response = makeRequest(ipAdress + "user/findUser", json);
                HttpResponse response = makeRequest("http://130.237.84.10:8081/starter/rest/poke",json);
                HttpEntity entity = response.getEntity();
                String content = null;
                content = EntityUtils.toString(entity);
                return content;
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage() + "");
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println(" uuu ");
        }

        @Override
        protected void onPreExecute() {
        }

    }

    public class ResultGson {
        public String Json;
    }

    public class Login_id {
        public String username;
        public String password;
    }
}

package com.example.serverutveckling.labb4;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;


public class ListOfPeopleActivity extends FragmentActivity {
    ListView list_of_people;
    public Activity a = this;
    String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_of_people);
        list_of_people = (ListView) findViewById(R.id.list_of_people);
        Bundle extras = getIntent().getExtras();
        id = extras.getString("id");
        new WebServiceCall().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_of_people, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static HttpResponse makeRequest(String uri) {
        try {
            HttpGet httpGet = new HttpGet(uri);
            HttpParams httpParameters = new BasicHttpParams();
            int timeoutConnection = 3000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 3000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            httpGet.setParams(httpParameters);
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-type", "application/json");
            return new DefaultHttpClient().execute(httpGet);
        } catch (UnsupportedEncodingException e) {
            Log.e("ERROR", e.getMessage() + "");
        } catch (ClientProtocolException e) {
            Log.e("ERROR", e.getMessage() + "");
        } catch (IOException e) {
            Log.e("ERROR", e.getMessage() + "");
        }
        return null;
    }

    private class WebServiceCall extends AsyncTask<ArrayList<String>, String, ArrayList<Login_id>> {

        @Override
        protected ArrayList<Login_id> doInBackground(ArrayList<String>... params) {
            try {

                HttpResponse response = makeRequest("http://130.237.84.10:8081/starter/rest/getpeople");
                HttpEntity entity = response.getEntity();
                String content = null;
                content = EntityUtils.toString(entity);
                Gson gson1 = new Gson();
                ResultGson rg_result = gson1.fromJson(content, ResultGson.class);
                Type type = new TypeToken<ArrayList<Login_id>>() {
                }.getType();
                ArrayList<Login_id> li_arr = gson1.fromJson(rg_result.Json, type);
                return li_arr;
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage() + "");
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Login_id> result) {
            try {
                ArrayList<User> users = new ArrayList<>();
                for (int i = 0; i < result.size(); i++) {
                    users.add(new User(result.get(i).password, result.get(i).username));
                }
                ListViewAdapter adapter = new ListViewAdapter(a, users, id);
                list_of_people.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
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

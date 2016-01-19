package com.example.serverutveckling.labb4;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.serverutveckling.labb4.gcm.QuickstartPreferences;
import com.example.serverutveckling.labb4.gcm.RegistrationIntentService;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
import org.json.JSONObject;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends FragmentActivity {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    CallbackManager callbackManager;
    AccessToken accessToken;
    static String userid;
    login_json login;
    BroadcastReceiver mRegistrationBroadcastReceiver;
    Activity a = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        callbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_facebook);
        loginButton.setReadPermissions(Arrays.asList("public_profile, email, user_friends, user_birthday, user_events"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                        accessToken = loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {

                                JSONObject arr = response.getJSONObject();
                                Gson gson = new Gson();
                                Type type = new TypeToken<login_json>() {
                                }.getType();
                                login = gson.fromJson(arr.toString(), type);
                                new WebServiceCall().execute();


                            }
                        });
                userid = loginResult.getAccessToken().getUserId();
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name");
                request.setParameters(parameters);
                request.executeAsync();


            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException e) {

            }


        });
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    System.out.println(getString(R.string.gcm_send_message));
                } else {
                    System.out.println(getString(R.string.token_error_message));
                }
            }
        };


    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public class login_json {
        public String id;
        public String name;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private class WebServiceCall extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected String doInBackground(ArrayList<String>... params) {
            try {
                ResultGson rg = new ResultGson();
                Gson gson = new Gson();
                Login_id fr = new Login_id();
                fr.username = userid;
                fr.password = login.name;
                rg.Json = gson.toJson(fr, Login_id.class);
                String json = gson.toJson(rg, ResultGson.class);
                //  String json = new GsonBuilder().create().toJson(user, UserGson.class);
                //   HttpResponse response = makeRequest(ipAdress + "user/findUser", json);
                HttpResponse response = makeRequest("http://130.237.84.10:8081/starter/rest/login/facebookId", json);
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
            if (checkPlayServices()) {
                // Start IntentService to register this application with GCM.
                QuickstartPreferences.activity = a;
                Intent intent = new Intent(a, RegistrationIntentService.class);
                startService(intent);
            }
            Intent j = new Intent(getApplicationContext(), HomeActivity.class);
            j.putExtra("name", login.name);
            j.putExtra("id", userid);
            startActivity(j);
        }

        @Override
        protected void onPreExecute() {
        }

    }

    public static void helpWebServiceCallGcm(String token) {
        new WebServiceCallGcm().execute(token);
    }

    private static class WebServiceCallGcm extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                String token = params[0];
                ResultGson rg = new ResultGson();
                Gson gson = new Gson();
                Login_id fr = new Login_id();
                fr.username = userid;
                fr.password = token;
                rg.Json = gson.toJson(fr, Login_id.class);
                String json = gson.toJson(rg, ResultGson.class);
                //  String json = new GsonBuilder().create().toJson(user, UserGson.class);
                //   HttpResponse response = makeRequest(ipAdress + "user/findUser", json);
                HttpResponse response = makeRequest("http://130.237.84.10:8081/starter/rest/login/gcmId", json);
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
            System.out.println(result);
        }

        @Override
        protected void onPreExecute() {
        }


    }

    public static class ResultGson {
        public String Json;
    }

    public static class Login_id {
        public String username;
        public String password;
    }
}

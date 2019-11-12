package com.example.onlinewebwithmysql;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ContactAdapter adapter;
    private Intent intent;
    private ListView contactlist = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapter = new ContactAdapter(new ArrayList<Contact>(), this);
        contactlist = findViewById(R.id.listview);
        contactlist.setAdapter(adapter);
        (new ConnectMysql()).execute("https://mysqlcontact.000webhostapp.com/list_contacts.php");
        contactlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //點選列表上的資料就跳往編輯好友頁面
                intent = new Intent();
                intent.putExtra("name",adapter.getItem(position).getName());

                intent.putExtra("title","編輯好友");
                intent.putExtra("type","edit");
                Log.i("name=",adapter.getItem(position).getName());
                changeView(MainActivity.this, EditActivity.class);
            }
        });
    }
    private class ConnectMysql extends AsyncTask<String, Void, List<Contact>> {

        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        @Override
        protected List<Contact> doInBackground(String... params) {
            List<Contact> result = new ArrayList<Contact>();
            URL u = null;
            try {
                //依據傳過來的網址參數建立建線
                u = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.setRequestMethod("GET");

                conn.connect();
                //讀取網頁上的資料
                InputStream is = conn.getInputStream();
                // Read the stream
                byte[] b = new byte[1024];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ( is.read(b) != -1)
                    baos.write(b);

                String JSONResp = new String(baos.toByteArray());
                Log.i("JSONResp=", JSONResp);

                JSONArray arr = new JSONArray(JSONResp);
                for (int i=0; i < arr.length(); i++) {
                    if(arr.getJSONObject(i)!=null) {
                        result.add(convertContact(arr.getJSONObject(i)));
                        Log.v("data=", arr.getJSONObject(i).toString());
                    }
                }
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
        private Contact convertContact(JSONObject obj) throws JSONException {
            Bitmap bitmap;
            if(obj.getString("Picture") != null) {
                bitmap = LoadImage("https://mysqlcontact.000webhostapp.com/upload/" + obj.getString("Picture").toString());
            }else{
                bitmap = LoadImage("https://mysqlcontact.000webhostapp.com/upload/supportmale.png");
            }
            String name = obj.getString("Name");
            String phoneNum = obj.getString("Phone");
            String email = obj.getString("Email");
            String birthday = obj.getString("Birthday");
            Log.v("jsonObj=",obj.getString("Picture").toString());

            return new Contact(bitmap, name, phoneNum, email, birthday);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog.setMessage("資料下載中...");
            dialog.show();
        }
        //載入遠端圖片，轉換成bitmap
        private Bitmap LoadImage(String imageUrl){
            Bitmap bitmap = null;
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();


                conn.setRequestMethod("GET");
                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = conn.getInputStream();

                    bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    return bitmap;
                }
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        @Override
        protected void onPostExecute(List<Contact> result) {
            super.onPostExecute(result);
            dialog.dismiss();
            adapter.setItemList(result);
            adapter.notifyDataSetChanged();
        }
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
        switch(id){
            case R.id.action_add:
                intent = new Intent();
                intent.putExtra("title","新增好友");
                intent.putExtra("type","new");

                changeView(this, EditActivity.class);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    //intent跳轉activity共用函式
    public void changeView(Context context, Class<?> cla){

        intent = intent.setClass(context, cla);
        startActivity(intent);
        this.finish();
    }
}

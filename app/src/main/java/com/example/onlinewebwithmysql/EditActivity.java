package com.example.onlinewebwithmysql;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

public class EditActivity extends AppCompatActivity implements View.OnClickListener {
    private Bundle bData;
    private EditText editName, editTel, editEmail, editBirth;
    private Button btnConfirm,btnSelect;
    private String type;
    private Intent intent;
    private String newName,queryName,newPhone,newEmail,newBirth,oldPic;

    private int index;
    private boolean isEdit = false;
    private Boolean isDeleted = false;
    private ImageView image;
    private Bitmap bitmap;
    private String picturePath;
    private String filename;
    private Uri selectedImage;
    // number of images to select
    private static final int PICK_IMAGE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnSelect = findViewById(R.id.btnSelect);
        initView();
        bData = this.getIntent().getExtras();
        type = bData.getString("type");
        if(type.equals("edit")){
            queryName = bData.getString("name");
            isEdit = true;
            (new ConnectMysql()).execute("https://mysqlcontact.000webhostapp.com/query.php");
        }
    }
    private void initView(){

        editName = findViewById(R.id.name);
        editTel = findViewById(R.id.phone);
        editEmail = findViewById(R.id.email);
        editBirth = findViewById(R.id.birthday);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(this);
        btnSelect = findViewById(R.id.btnSelect);
        btnSelect.setOnClickListener(this);
        image = findViewById(R.id.image);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
            case R.id.action_back:
                intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);
                this.finish();
                break;
            case R.id.action_delete:

                (new ConnectMysql()).execute("https://mysqlcontact.000webhostapp.com/delete.php");

                break;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnConfirm:
                newName = editName.getText().toString();
                newPhone = editTel.getText().toString();
                newEmail = editEmail.getText().toString();
                newBirth = editBirth.getText().toString();
                isEdit = false;
                if(type.equals("new")){
                    (new ConnectMysql()).execute("https://mysqlcontact.000webhostapp.com/insert2.php");
                }else{
                    Log.i("edit=","start edit");
                    (new ConnectMysql()).execute("https://mysqlcontact.000webhostapp.com/update.php");
                }
                break;
            case R.id.btnSelect:
                selectImageFromGallery();
                break;
        }
    }
    public void selectImageFromGallery() {
            if (Build.VERSION.SDK_INT <19) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "請選擇圖片"), PICK_IMAGE);
            }else{
                Log.v("test","abc");
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) return;

        if (resultCode == Activity.RESULT_OK && data != null) {

            selectedImage = data.getData();
            image.setImageURI(selectedImage);
            String id = selectedImage.getLastPathSegment().split(":")[1];
            final String[] imageColumns = {MediaStore.Images.Media.DATA };
            final String imageOrderBy = null;
            if (Build.VERSION.SDK_INT >= 23) {
                int REQUEST_CODE_IMAGE = 101;
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                //验证是否许可权限
                for (String str : permissions) {
                    if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                        //申请权限
                        this.requestPermissions(permissions, REQUEST_CODE_IMAGE);
                        return;
                    }
                }
            }
            Uri uri = getUri();
            picturePath = "path";

            Cursor imageCursor = getContentResolver().query(uri, imageColumns,
                    MediaStore.Images.Media._ID + "="+id, null, null);

            if (imageCursor.moveToFirst()) {

                picturePath = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            Log.v("picturePath=",picturePath);
        }
    }
    private Uri getUri() {
        String state = Environment.getExternalStorageState();
        if(!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }
    private class ConnectMysql extends AsyncTask<String, Void, String> {
        //定義image上傳相關屬性
        String attachmentFileName = picturePath;
        String crlf = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";
        private final ProgressDialog dialog = new ProgressDialog(EditActivity.this);
        @Override
        protected String doInBackground(String... params) {
            int result = 0;
            URL u = null;

            try {
                u = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                if(isEdit){
                    queryData(conn);
                }else {
                    add_updateData(conn);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
        private String add_updateData(HttpURLConnection conn){
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            try{
                //設定圖片欄位相關屬性
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + this.boundary);
                conn.setRequestProperty("Charset", "UTF-8");
                //將圖片資訊組成串流
                DataOutputStream request = new DataOutputStream(conn.getOutputStream());
                //圖片上傳函式
                if(this.attachmentFileName != null){
                    File sourceFile = new File(attachmentFileName);
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
                    request.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + this.attachmentFileName + "\"" + this.crlf);
                    request.writeBytes(this.crlf);

                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        request.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    }
                    request.write(buffer);
                }else if(type.equals("edit")){
                    request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
                    request.writeBytes("Content-Disposition: form-data; name=\"Picture\"" + "\"" + this.crlf);
                    request.writeBytes(this.crlf);
                    request.writeBytes(oldPic);
                    request.writeBytes(this.crlf);

                } //結束判斷圖片函式
                request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
                request.writeBytes("Content-Disposition: form-data; name=\"ContactID\"" + "\"" + this.crlf);
                request.writeBytes(this.crlf);
                Log.i("index=====>",String.valueOf(index));
                request.writeBytes(String.valueOf(index));
                request.writeBytes(this.crlf);

                request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
                request.writeBytes("Content-Disposition: form-data; name=\"Name\"" + "\"" + this.crlf);
                request.writeBytes(this.crlf);
                request.writeBytes(newName);
                request.writeBytes(this.crlf);
                request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
                request.writeBytes("Content-Disposition: form-data; name=\"Phone\"" + "\"" + this.crlf);
                request.writeBytes(this.crlf);
                request.writeBytes(newPhone);
                request.writeBytes(this.crlf);
                request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
                request.writeBytes("Content-Disposition: form-data; name=\"Email\"" + "\"" + this.crlf);
                request.writeBytes(this.crlf);
                request.writeBytes(newEmail);
                request.writeBytes(this.crlf);
                request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
                request.writeBytes("Content-Disposition: form-data; name=\"Birthday\"" + "\"" + this.crlf);
                request.writeBytes(this.crlf);
                request.writeBytes(newBirth);
                request.writeBytes(this.crlf);
                request.writeBytes(this.twoHyphens + this.boundary + this.twoHyphens + this.crlf);

                request.flush();
                request.close();

                Log.i("postString=", request.toString());
                conn.connect();

                InputStream is = conn.getInputStream();
                // Read the stream
                byte[] b = new byte[1024];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while (is.read(b) != -1)
                    baos.write(b);

                String response = new String(baos.toByteArray());
                Log.i("response=", response);
                return response;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        private String queryData(HttpURLConnection conn){
            //讀取單一筆資料
            try {
                String data = "Name=" + URLEncoder.encode(queryName, "UTF-8");
                OutputStream os = null;
                os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(data);
                writer.flush();
                writer.close();
                os.close();
                Log.i("postString=", data);
                conn.connect();

                InputStream is = conn.getInputStream();
                // Read the stream
                byte[] b = new byte[1024];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ( is.read(b) != -1)
                    baos.write(b);

                String response = new String(baos.toByteArray());
                Log.i("JSONResp=", response);
                JSONArray arr = new JSONArray(response);
                for (int i=0; i < arr.length(); i++) {
                    loadContact(arr.getJSONObject(i));
                    Log.v("data=",arr.getJSONObject(i).toString());
                }
                Log.i("response=", response);
                return response;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        private void loadContact(JSONObject obj) throws JSONException {

            if(obj.getString("Picture") != null) {
                bitmap = LoadImage("https://mysqlcontact.000webhostapp.com/images/" + obj.getString("Picture").toString());
            }else{
                bitmap = LoadImage("https://mysqlcontact.000webhostapp.com/images/supportmale.png");
            }

            Log.v("jsonObj=",obj.getString("Picture").toString());
            oldPic = obj.getString("Picture");
            index = obj.getInt("ContactID");
            newName = obj.getString("Name");
            newPhone = obj.getString("Phone");
            newEmail = obj.getString("Email");
            newBirth = obj.getString("Birthday");


        }
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
        protected void onPreExecute() {
            super.onPreExecute();
//            dialog.setMessage("連線中...");
//            dialog.show();
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            int isSuccess = 0;
            String result_msg = "";
            dialog.dismiss();
            if(type.equals("new")){
                isSuccess = result.indexOf("add Success");
                result_msg = "新增成功!";
            }else if(type.equals("edit")){
                if(isEdit){
                    editName.setText(newName);
                    editTel.setText(newPhone);
                    editEmail.setText(newEmail);
                    editBirth.setText(newBirth);
                    image.setImageBitmap(bitmap);
                    isEdit = false;
                }else {
                    isSuccess = result.indexOf("edit Success");
                    result_msg = "修改成功!";
                }
            }else{
                isDeleted = true;
                isSuccess = result.indexOf("delete Success");
                if (isDeleted)
                    result_msg = "刪除成功!";


            }
            if(isSuccess !=-1 || isDeleted){
                Toast.makeText(EditActivity.this, result_msg, Toast.LENGTH_SHORT).show();
            }
        }
    }
}

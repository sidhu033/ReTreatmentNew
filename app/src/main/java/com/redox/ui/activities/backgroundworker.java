package com.redox.ui.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
//import org.apache.http.client.HttpClient;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by User on 8/6/2018.
 */
public class backgroundworker extends AsyncTask<String,Void,String> {
    Context context;
    AlertDialog alertDialog;
    String json_url;

    backgroundworker(Context ctx){
        context = ctx;
    }



    @Override
    protected String doInBackground(String ...params)
    {
        String JSON_STRING;

        String type=params[0];

      // String info_url="http://gamsystech.com/info.php";
        if(type.equals("login")){
            try {
                String user_name=params[1];
                String password=params[2];
                URL url=new URL(json_url);
                HttpURLConnection httpURLConnection =(HttpURLConnection)url.openConnection();
              //  httpURLConnection.setRequestMethod("POST");
                //httpURLConnection.setDoOutput(true);
                //httpURLConnection.setDoInput(true);
                //OutputStream outputStream = httpURLConnection.getOutputStream();
                //BufferedWriter bufferedWriter =new BufferedWriter(new OutputStreamWriter(outputStream ,"UTF-8"));
                //String post_data= URLEncoder.encode("user_name","UTF-8")+"="+ URLEncoder.encode(user_name,"UTF-8")+"&"+ URLEncoder.encode("password","UTF-8")+"="+ URLEncoder.encode(password,"UTF-8");
               // bufferedWriter.write(post_data);
                //bufferedWriter.flush();
                //bufferedWriter.close();
                //outputStream.close();
                InputStream inputStream= httpURLConnection.getInputStream();
                BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
                //String result="Login";
                //String line="";
                StringBuilder stringBuilder=new StringBuilder();
                while ((JSON_STRING = bufferedReader.readLine())!= null){
                   // result +=line;
                    stringBuilder.append(JSON_STRING+"\n");

                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                //return result;
                return stringBuilder.toString().trim();

            }
            catch (RuntimeException e) {
                e.printStackTrace();
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;

    }


    @Override
    protected  void onPreExecute(){
       // super.onPreExecute();
        json_url="http://gamsystech.com/login.php";
    }

    @Override
    protected  void onPostExecute(String result)
    {
        alertDialog=new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Login Status");
        alertDialog.setMessage(result);
       /* if(result!="Login Not Success") {
            Intent i = new Intent(context.getApplicationContext(), SplashActivity.class);
            context.startActivity(i);
        }
        else
        {
            Toast toast=Toast.makeText(context.getApplicationContext(),"Login Failed",Toast.LENGTH_SHORT);
        }*/
        alertDialog.show();



    }


    @Override
    protected void onProgressUpdate(Void... values){
        super.onProgressUpdate(values);
    }
}

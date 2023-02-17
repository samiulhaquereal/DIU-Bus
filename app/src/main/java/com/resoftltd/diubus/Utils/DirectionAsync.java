package com.resoftltd.diubus.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DirectionAsync extends AsyncTask<Object, String, String> {
    Context c;
    LatLng endLatLng;
    GoogleMap mMap;
    Marker marker;
    String myurl;
    LatLng startLatLng;
    HttpURLConnection httpURLConnection = null;
    String data = "";
    InputStream inputStream = null;

    public DirectionAsync(Context context) {
        this.c = context;
    }

    @Override // android.os.AsyncTask
    public String doInBackground(Object... objArr) {
        this.mMap = (GoogleMap) objArr[0];
        this.myurl = (String) objArr[1];
        this.startLatLng = (LatLng) objArr[2];
        this.endLatLng = (LatLng) objArr[3];
        this.marker = (Marker) objArr[4];
        try {
            this.httpURLConnection = (HttpURLConnection) new URL(this.myurl).openConnection();
            this.httpURLConnection.connect();
            this.inputStream = this.httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
                stringBuffer.append(readLine);
            }
            this.data = stringBuffer.toString();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.data;
    }

    @Override // android.os.AsyncTask
    public void onPostExecute(String str) {
        try {
            JSONObject jSONObject = new JSONObject(str);
            JSONArray jSONArray = jSONObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
            String string = jSONObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text");
            this.marker.setTitle(string);
            Toast.makeText(this.c, string + " away.", Toast.LENGTH_SHORT).show();
            int length = jSONArray.length();
            String[] strArr = new String[length];
            for (int i = 0; i < length; i++) {
                strArr[i] = jSONArray.getJSONObject(i).getJSONObject("polyline").getString("points");
            }
            for (String str2 : strArr) {
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(-16711936);
                polylineOptions.width(10.0f);
                polylineOptions.addAll(PolyUtil.decode(str2));
                this.mMap.addPolyline(polylineOptions);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

package com.resoftltd.diubus.Services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.resoftltd.diubus.Navigation;
import com.resoftltd.diubus.R;

public class LocationShareService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public LocationShareService(){
    }
    FirebaseAuth auth;
    Notification.Builder builder;
    GoogleApiClient client;
    LatLng latLngCurrent;
    NotificationManagerCompat nmc;
    DatabaseReference reference;
    LocationRequest request;
    FirebaseUser user;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        reference = FirebaseDatabase.getInstance().getReference().child("Drivers");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        client.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        request = new LocationRequest().create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(600);
        if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 || ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(client,request, this);
        showNotifications();
    }

    private void showNotifications() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel("channelid1", "001", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("This is description");
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(notificationChannel);
            builder = new Notification.Builder(getApplicationContext(), notificationChannel.getId());
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle("School Bus Tracker");
            builder.setContentText("You are sharing your location.!");
            builder.setSmallIcon(R.drawable.share_location).setPriority(Notification.PRIORITY_DEFAULT);
            nmc = NotificationManagerCompat.from(getApplicationContext());
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            this.nmc.notify(654321, this.builder.build());
        } else {
            this.builder = new Notification.Builder(getApplicationContext());
            this.builder.setSmallIcon(R.mipmap.ic_launcher);
            this.builder.setContentTitle("School Bus Tracker");
            this.builder.setContentText("You are sharing your location.!").setPriority(Notification.PRIORITY_DEFAULT);
            this.nmc = NotificationManagerCompat.from(getApplicationContext());
            this.nmc.notify(654321, this.builder.build());
        }
        this.builder.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), Navigation.class), PendingIntent.FLAG_UPDATE_CURRENT));
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(654321, this.builder.build());
    }

    @Override
    public void onLocationChanged(Location location) {
        latLngCurrent = new LatLng(location.getLatitude(), location.getLongitude());
        shareLocation();
    }

    public void shareLocation() {
        try {
            reference.child(user.getUid()).child("issharing").setValue("True");
            reference.child(user.getUid()).child("lat").setValue(String.valueOf(latLngCurrent.latitude));
            reference.child(user.getUid()).child("lng").setValue(String.valueOf(latLngCurrent.longitude)).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        return;
                    }
                    Toast.makeText(LocationShareService.this.getApplicationContext(), "Could not share Location.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Only drivers can share their location", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        reference.child(user.getUid()).child("issharing").setValue("False");
        LocationServices.FusedLocationApi.removeLocationUpdates(this.client, this);
        client.disconnect();
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(654321);
    }
}

package com.resoftltd.diubus;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.resoftltd.diubus.Services.LocationShareService;
import com.resoftltd.diubus.Utils.DirectionAsync;

import java.text.DecimalFormat;
import java.util.HashMap;


public class Navigation extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleMap.OnMarkerClickListener, ResultCallback {
    FirebaseAuth auth;
    GoogleApiClient client;
    HashMap<String, Marker> hashMap;
    LatLng latLngCurrentuserLocation,u;
    GoogleMap mMap;
    DatabaseReference referenceDrivers;
    DatabaseReference referenceUsers;
    LocationRequest request;
    RequestQueue requestQueue;
    DatabaseReference scheduleReference;
    TextView textEmail;
    FirebaseUser user;
    TextView textName;
    LatLng updateLatLng;
    boolean driver_profile = false;
    boolean user_profile = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        auth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(this);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.Open, R.string.Colse);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        textName = headerView.findViewById(R.id.title_text);
        textEmail = headerView.findViewById(R.id.email_text);

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync( this);

        referenceDrivers = FirebaseDatabase.getInstance().getReference().child("Drivers");
        referenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        scheduleReference = FirebaseDatabase.getInstance().getReference().child("uploads").child("0");
        hashMap = new HashMap<>();

        referenceDrivers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser currentUser = auth.getCurrentUser();
                if (dataSnapshot.child(currentUser.getUid()).child("lat").exists()) {
                    driver_profile = true;
                    textName.setText(dataSnapshot.child(currentUser.getUid()).child("name").getValue(String.class));
                    textEmail.setText(dataSnapshot.child(currentUser.getUid()).child("email").getValue(String.class));
                    navigationView.getMenu().clear();
                    navigationView.inflateMenu(R.menu.driver_menu);
                    return;
                }
                Navigation navigationActivity = Navigation.this;
                navigationActivity.user_profile = true;
                navigationActivity.referenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot2) {
                        FirebaseUser currentUser2 = auth.getCurrentUser();
                        textName.setText(dataSnapshot2.child(currentUser2.getUid()).child("name").getValue(String.class));
                        textEmail.setText(dataSnapshot2.child(currentUser2.getUid()).child("email").getValue(String.class));
                        navigationView.getMenu().clear();
                        navigationView.inflateMenu(R.menu.user_menu);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        referenceDrivers.addChildEventListener(new ChildEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String str) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String str) {
                try {
                    LatLng latLng = new LatLng(Double.parseDouble(dataSnapshot.child("lat").getValue(String.class)), Double.parseDouble( dataSnapshot.child("lng").getValue(String.class)));
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.title(dataSnapshot.child("name").getValue(String.class));
                    markerOptions.snippet("Van number: " + (dataSnapshot.child("vehiclenumber").getValue(String.class)));
                    markerOptions.position(latLng);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.busicon));
                    Marker addMarker = mMap.addMarker(markerOptions);
                    hashMap.put(addMarker.getTitle(), addMarker);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String str) {
                try {
                    String obj = dataSnapshot.child("name").getValue().toString();
                    String obj2 = dataSnapshot.child("lat").getValue().toString();
                    String obj3 = dataSnapshot.child("lng").getValue().toString();
                    updateLatLng = new LatLng(Double.parseDouble(obj2), Double.parseDouble(obj3));
                    Marker marker = hashMap.get(obj);
                    if (marker != null) {
                        marker.setPosition(updateLatLng);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);


        client = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addOnConnectionFailedListener(this).addConnectionCallbacks(this).build();
        client.connect();
    }

    public boolean onMarkerClick(Marker marker) {
        try {
            LatLng position = marker.getPosition();
            String format = new DecimalFormat("#.##").format(CalculationByDistance(this.u, position));
            Toast.makeText(this, format + " KM far.", Toast.LENGTH_SHORT).show();
            StringBuilder sb = new StringBuilder();
            sb.append("https://maps.googleapis.com/maps/api/directions/json?");
            sb.append("origin=" + position.latitude + "," + position.longitude);
            sb.append("&destination=" + this.u.latitude + "," + this.u.longitude);
            sb.append("&key=AIzaSyDPqeShdSvznztq8n8Y0RZTMdm_BE9Ks88");
            new DirectionAsync(getApplicationContext()).execute(this.mMap, sb.toString(), new LatLng(position.latitude, position.longitude), new LatLng(this.u.latitude, this.u.longitude), marker);

        }catch (Exception e) {
            Toast.makeText(this, "Sorry , You are not User", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private double CalculationByDistance(LatLng latLng, LatLng latLng2) {
        double d = latLng.latitude;
        double d2 = latLng2.latitude;
        double d3 = latLng.longitude;
        double d4 = latLng2.longitude;
        double radians = Math.toRadians(d2 - d) / 2.0d;
        double radians2 = Math.toRadians(d4 - d3) / 2.0d;
        double d5 = 6371;
        Double.isNaN(d5);
        double asin = d5 * Math.asin(Math.sqrt((Math.sin(radians) * Math.sin(radians)) + (Math.cos(Math.toRadians(d)) * Math.cos(Math.toRadians(d2)) * Math.sin(radians2) * Math.sin(radians2)))) * 2.0d;
        DecimalFormat decimalFormat = new DecimalFormat("####");
        Integer.valueOf(decimalFormat.format(asin / 1.0d)).intValue();
        double d6 = asin % 1000.0d;
        Integer.valueOf(decimalFormat.format(d6)).intValue();
        return d6;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout =findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("WrongConstant")
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        FirebaseAuth firebaseAuth;
        int itemId = menuItem.getItemId();
        if (this.driver_profile) {
            if (itemId == R.id.nav_signout) {
                FirebaseAuth firebaseAuth2 = this.auth;
                if (firebaseAuth2 != null) {
                    firebaseAuth2.signOut();
                    finish();
                    startActivity(new Intent(this, MainActivity.class));
                }
            } else if (itemId == R.id.nav_share_Location) {
                if (isServiceRunning(getApplicationContext(), LocationShareService.class)) {
                    Toast.makeText(getApplicationContext(), "You are already sharing your location.", 0).show();
                } else if (this.driver_profile) {
                    startService(new Intent(this, LocationShareService.class));
                    Toast.makeText(getApplicationContext(), "Started", 0).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Only driver can share location", 0).show();
                }
            } else if (itemId == R.id.nav_stop_Location) {
                stopService(new Intent(this, LocationShareService.class));
                Toast.makeText(getApplicationContext(), "Stoped", 0).show();
            }
        } else if (itemId == R.id.nav_signout_user && (firebaseAuth = this.auth) != null) {
            firebaseAuth.signOut();
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(335544320);
            startActivity(intent);
        }
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        return true;
    }


    public boolean isServiceRunning(Context context, Class<?> cls) {
        for (ActivityManager.RunningServiceInfo runningServiceInfo : ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
            if (runningServiceInfo.service.getClassName().equals(cls.getName())) {
                return true;
            }
        }
        return false;
    }


    @SuppressLint({"RestrictedApi"})
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        request = LocationRequest.create();
        request.setPriority(100);
        request.setInterval(5000L);
        if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 || ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
            LocationSettingsRequest.Builder addLocationRequest = new LocationSettingsRequest.Builder().addLocationRequest(request);
            addLocationRequest.setAlwaysShow(true);
            LocationServices.SettingsApi.checkLocationSettings(client, addLocationRequest.build()).setResultCallback((ResultCallback<? super LocationSettingsResult>) this);
            LocationServices.FusedLocationApi.requestLocationUpdates(client,request,this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void onLocationChanged(Location location) {
        LocationServices.FusedLocationApi.removeLocationUpdates(client,this);
        if (location == null) {
            Toast.makeText(getApplicationContext(), "Could not find location", Toast.LENGTH_SHORT).show();
            return;
        }

        if(driver_profile==false){
            u = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(this.u).icon(BitmapDescriptorFactory.defaultMarker())).setVisible(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(u, 15.0f));

        }else{
            user = auth.getCurrentUser();
            FirebaseDatabase.getInstance().getReference().child("Drivers").child(user.getUid()).child("lat").setValue(location.getLatitude());
            FirebaseDatabase.getInstance().getReference().child("Drivers").child(user.getUid()).child("lng").setValue(location.getLongitude());

            latLngCurrentuserLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(this.latLngCurrentuserLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.busicon))).setVisible(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.latLngCurrentuserLocation, 15.0f));
        }

    }


    public void onResult(@NonNull Result result) {
        Status status = result.getStatus();
        int statusCode = ((Status) status).getStatusCode();
        if (statusCode == 0 || statusCode != 6) {
            return;
        }
        try {
            status.startResolutionForResult(this, 202);
        } catch (IntentSender.SendIntentException unused) {
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
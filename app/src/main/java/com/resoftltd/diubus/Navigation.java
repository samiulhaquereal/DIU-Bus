package com.resoftltd.diubus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.resoftltd.diubus.Services.LocationShareService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;


public class Navigation extends AppCompatActivity {
    FirebaseAuth auth;
    GoogleApiClient client;
    HashMap<String, Marker> hashMap;
    LatLng latLngCurrentuserLocation;
    GoogleMap mMap;
    DatabaseReference referenceDrivers;
    DatabaseReference referenceUsers;
    LocationRequest request;
    RequestQueue requestQueue;
    DatabaseReference scheduleReference;
    TextView textEmail;
    TextView textName;
    LatLng updateLatLng;
    boolean driver_profile = false;
    boolean user_profile = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbarc = findViewById(R.id.toolbar);

        setSupportActionBar(toolbarc);


        auth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(this);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbarc, R.string.Open, R.string.Colse);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);

        View headerView = navigationView.getHeaderView(0);
        this.textName = (TextView) headerView.findViewById(R.id.title_text);
        this.textEmail = (TextView) headerView.findViewById(R.id.email_text);

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync((OnMapReadyCallback) this);
        this.referenceDrivers = FirebaseDatabase.getInstance().getReference().child("Drivers");
        this.referenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        this.scheduleReference = FirebaseDatabase.getInstance().getReference().child("uploads").child("0");
        this.hashMap = new HashMap<>();
        this.referenceDrivers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser currentUser = auth.getCurrentUser();
                if (dataSnapshot.child(currentUser.getUid()).child("lat").exists()) {
                    driver_profile = true;
                    textName.setText((String) dataSnapshot.child(currentUser.getUid()).child(AppMeasurementSDK.ConditionalUserProperty.NAME).getValue(String.class));
                    textEmail.setText((String) dataSnapshot.child(currentUser.getUid()).child("email").getValue(String.class));
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
                        textName.setText((String) dataSnapshot2.child(currentUser2.getUid()).child(AppMeasurementSdk.ConditionalUserProperty.NAME).getValue(String.class));
                        textEmail.setText((String) dataSnapshot2.child(currentUser2.getUid()).child("email").getValue(String.class));
                        FirebaseMessaging.getInstance().subscribeToTopic("news");
                        navigationView.getMenu().clear();
                        navigationView.inflateMenu(R.menu.user_menu);
                    }

                    @Override // com.google.firebase.database.ValueEventListener
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override // com.google.firebase.database.ValueEventListener
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        this.referenceDrivers.addChildEventListener(new ChildEventListener() { // from class: com.haroonfazal.haroonapps.bustracker.Activities.NavigationActivity.2
            @Override // com.google.firebase.database.ChildEventListener
            public void onCancelled(DatabaseError databaseError) {
            }

            @Override // com.google.firebase.database.ChildEventListener
            public void onChildMoved(DataSnapshot dataSnapshot, String str) {
            }

            @Override // com.google.firebase.database.ChildEventListener
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override // com.google.firebase.database.ChildEventListener
            public void onChildAdded(DataSnapshot dataSnapshot, String str) {
                try {
                    LatLng latLng = new LatLng(Double.parseDouble((String) dataSnapshot.child("lat").getValue(String.class)), Double.parseDouble((String) dataSnapshot.child("lng").getValue(String.class)));
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.title((String) dataSnapshot.child(AppMeasurementSdk.ConditionalUserProperty.NAME).getValue(String.class));
                    markerOptions.snippet("Van number: " + ((String) dataSnapshot.child("vehiclenumber").getValue(String.class)));
                    markerOptions.position(latLng);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.busicon));
                    Marker addMarker = mMap.addMarker(markerOptions);
                    hashMap.put(addMarker.getTitle(), addMarker);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override // com.google.firebase.database.ChildEventListener
            public void onChildChanged(DataSnapshot dataSnapshot, String str) {
                try {
                    String obj = dataSnapshot.child(AppMeasurementSdk.ConditionalUserProperty.NAME).getValue().toString();
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
        this.mMap = googleMap;
        this.mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);
        this.client = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this).addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this).build();
        this.client.connect();
    }

    public boolean onMarkerClick(Marker marker) {
        LatLng position = marker.getPosition();
        String format = new DecimalFormat("#.##").format(CalculationByDistance(this.latLngCurrentuserLocation, position));
        Toast.makeText(this, format + " KM far.", Toast.LENGTH_SHORT).show();
        StringBuilder sb = new StringBuilder();
        sb.append("https://maps.googleapis.com/maps/api/directions/json?");
        sb.append("origin=" + position.latitude + "," + position.longitude);
        sb.append("&destination=" + this.latLngCurrentuserLocation.latitude + "," + this.latLngCurrentuserLocation.longitude);
        sb.append("&key=AIzaSyCsThl1-hAeG2EscPb69ii0hdSXkUJ6-x0");
        new DirectionAsync(getApplicationContext()).execute(this.mMap, sb.toString(), new LatLng(position.latitude, position.longitude), new LatLng(this.latLngCurrentuserLocation.latitude, this.latLngCurrentuserLocation.longitude), marker);
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
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
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
                } else {
                    Toast.makeText(getApplicationContext(), "Only driver can share location", 0).show();
                }
            } else if (itemId == R.id.nav_stop_Location) {
                stopService(new Intent(this, LocationShareService.class));
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

    private void openDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(50, 0, 50, 100);
        final EditText editText = new EditText(this);
        editText.setLayoutParams(layoutParams);
        editText.setGravity(8388659);
        editText.setInputType(16384);
        editText.setLines(1);
        editText.setHint("Enter title");
        editText.setMaxLines(1);
        final EditText editText2 = new EditText(this);
        editText2.setLayoutParams(layoutParams);
        editText2.setGravity(8388659);
        editText2.setHint("Enter message");
        editText2.setInputType(16384);
        editText2.setLines(1);
        editText2.setMaxLines(1);
        linearLayout.addView(editText, layoutParams);
        linearLayout.addView(editText2, layoutParams);
        builder.setMessage("Enter your notification details");
        builder.setTitle("Send Notifications");
        builder.setView(linearLayout);
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() { // from class: com.haroonfazal.haroonapps.bustracker.Activities.NavigationActivity.3
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    sendFcm(editText.getText().toString(), editText2.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.haroonfazal.haroonapps.bustracker.Activities.NavigationActivity.4
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendFcm(String str, String str2) throws JSONException {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("to", "/topics/news");
        JSONObject jSONObject2 = new JSONObject();
        jSONObject2.put("title", str);
        jSONObject2.put("body", str2);
        jSONObject.put("notification", jSONObject2);
        this.requestQueue.add(new JsonObjectRequest(1, "https://fcm.googleapis.com/fcm/send", jSONObject, new Response.Listener<JSONObject>() { // from class: com.haroonfazal.haroonapps.bustracker.Activities.NavigationActivity.5
            @Override // com.android.volley.Response.Listener
            public void onResponse(JSONObject jSONObject3) {
                Log.d("response", jSONObject3.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Context applicationContext = getApplicationContext();
                Toast.makeText(applicationContext, "error in response=" + volleyError.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap hashMap = new HashMap();
                hashMap.put("content-type", "application/json");
                hashMap.put("authorization", "key=AIzaSyB9PCayi2q5kN7R0bS8l7Ykk5YbQyfG2Fw");
                return hashMap;
            }
        });
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
        this.request = LocationRequest.create();
        this.request.setPriority(100);
        this.request.setInterval(5000L);
        if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 || ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
            LocationSettingsRequest.Builder addLocationRequest = new LocationSettingsRequest.Builder().addLocationRequest(this.request);
            addLocationRequest.setAlwaysShow(true);
            LocationServices.SettingsApi.checkLocationSettings(this.client, addLocationRequest.build()).setResultCallback((ResultCallback<? super LocationSettingsResult>) this);
            LocationServices.FusedLocationApi.requestLocationUpdates(this.client, this.request, (LocationListener) this);
        }
    }

    public void onLocationChanged(Location location) {
        LocationServices.FusedLocationApi.removeLocationUpdates(this.client, (LocationListener) this);
        if (location == null) {
            Toast.makeText(getApplicationContext(), "Could not find location", Toast.LENGTH_SHORT).show();
            return;
        }
        this.latLngCurrentuserLocation = new LatLng(location.getLatitude(), location.getLongitude());
        this.mMap.addMarker(new MarkerOptions().position(this.latLngCurrentuserLocation).icon(BitmapDescriptorFactory.defaultMarker(210.0f))).setVisible(true);
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.latLngCurrentuserLocation, 15.0f));
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

}
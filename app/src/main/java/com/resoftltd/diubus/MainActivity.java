package com.resoftltd.diubus;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    String[] permissions = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};
    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            setContentView(R.layout.activity_main);
            checkPermissions();
            return;
        }
        startActivity(new Intent(this, Navigation.class));
        finish();

    }
    public void registerAsUser(View view) {
        startActivity(new Intent(this, UserRegistrationActivity.class));
        }

    public void registerAsDriver(View view) {

        startActivity(new Intent(this, DriverRegistrationActivity.class));

    }

    public void login(View view) {
        startActivity(new Intent(this, Login.class));
    }

    private boolean checkPermissions() {
        String[] strArr;
        ArrayList arrayList = new ArrayList();
        for (String str : this.permissions) {
            if (ContextCompat.checkSelfPermission(this, str) != 0) {
                arrayList.add(str);
            }
        }
        if (arrayList.isEmpty()) {
            return true;
        }
        ActivityCompat.requestPermissions(this, (String[]) arrayList.toArray(new String[arrayList.size()]), 100);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 100) {
            for (int i2 = 0; i2 < strArr.length; i2++) {
                if (iArr[i2] != 0 && Build.VERSION.SDK_INT >= 23) {
                    requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, 100);
                }
            }
        }
    }

}
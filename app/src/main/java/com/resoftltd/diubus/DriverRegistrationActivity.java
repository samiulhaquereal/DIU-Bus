package com.resoftltd.diubus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.resoftltd.diubus.Models.Driver;

public class DriverRegistrationActivity extends AppCompatActivity {

    FirebaseAuth auth;
    DatabaseReference databaseReference;
    ProgressDialog dialog;


    EditText editTextDriverBus;

    EditText editTextDriverEmail;

    EditText editTextDriverName;
    EditText editTextDriverPassword;

    Toolbar toolbar;
    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_registration);
        //toolbar.setTitle("Driver Register");
        //setSupportActionBar(toolbar);
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);
        editTextDriverName = findViewById(R.id.editTextdriverName);
        editTextDriverEmail = findViewById(R.id.editTextdriverEmail);
        editTextDriverPassword = findViewById(R.id.editTextdriverPassword);
        editTextDriverBus = findViewById(R.id.editTextDriverBus);

    }

    public void registerDriver(View view) {
        dialog.setTitle("Creating account");
        dialog.setMessage("Please wait");
        this.dialog.show();
        String obj = editTextDriverName.getText().toString();
        String obj2 = editTextDriverEmail.getText().toString();
        String obj3 = editTextDriverPassword.getText().toString();
        if (obj.equals("") && obj2.equals("") && obj3.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter correct details", Toast.LENGTH_SHORT).show();
            this.dialog.dismiss();
            return;
        }
        doAllStuff();
    }

    public void doAllStuff() {
        this.auth.createUserWithEmailAndPassword(this.editTextDriverEmail.getText().toString(), this.editTextDriverPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Driver driver = new Driver(editTextDriverName.getText().toString(), editTextDriverEmail.getText().toString(), editTextDriverPassword.getText().toString(), editTextDriverBus.getText().toString(), "24.886436", "91.880722");
                    DriverRegistrationActivity driverRegistrationActivity = DriverRegistrationActivity.this;
                    driverRegistrationActivity.user = driverRegistrationActivity.auth.getCurrentUser();
                    DriverRegistrationActivity.this.databaseReference = FirebaseDatabase.getInstance().getReference().child("Drivers").child(user.getUid());
                    DriverRegistrationActivity.this.databaseReference.setValue(driver).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task2) {
                            if (task2.isSuccessful()) {
                                dialog.dismiss();
                                Toast.makeText(DriverRegistrationActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                finish();
                                Intent intent = new Intent(DriverRegistrationActivity.this, Navigation.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                return;
                            }
                            Toast.makeText(DriverRegistrationActivity.this, "Could not register driver", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });
                    return;
                }
                DriverRegistrationActivity driverRegistrationActivity2 = DriverRegistrationActivity.this;
                Toast.makeText(driverRegistrationActivity2, "Could not register. " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
    }
}
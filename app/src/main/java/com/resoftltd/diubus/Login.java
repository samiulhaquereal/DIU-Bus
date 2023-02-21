package com.resoftltd.diubus;

import android.annotation.SuppressLint;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.ButterKnife;

public class Login extends AppCompatActivity {
    FirebaseUser user;
    FirebaseAuth auth;
    DatabaseReference referenceDrivers, referenceUsers;
    ProgressDialog dialog;
    EditText editTextUserEmail;
    EditText editTextUserPassword;

    boolean driver_profile = false;
    boolean user_profile = false;

    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        toolbar = (Toolbar) findViewById(R.id.userToolbar);
        toolbar.setTitle("Login");

        editTextUserPassword = findViewById(R.id.loginidpass);
        editTextUserEmail = findViewById(R.id.loginidemail);

        auth = FirebaseAuth.getInstance();

        this.dialog = new ProgressDialog(this);
        referenceDrivers = FirebaseDatabase.getInstance().getReference().child("Drivers");
        referenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");

    }

    public void login(View view) {
        this.dialog.setMessage("Logging in. Please wait.");
        this.dialog.show();
        if (this.editTextUserEmail.getText().toString().equals("") || this.editTextUserPassword.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Blank fields not allowed.", Toast.LENGTH_SHORT).show();
            this.dialog.dismiss();
            return;
        }

        auth.signInWithEmailAndPassword(editTextUserEmail.getText().toString(), editTextUserPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            @SuppressLint("WrongConstant")
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    referenceDrivers.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            user = auth.getCurrentUser();
                            if (dataSnapshot.child(user.getUid()).child("lat").exists()) {
                                driver_profile = true;
                                dialog.dismiss();
                                Intent intent = new Intent(Login.this, DriversMaps.class);
                                intent.addFlags(335544320);
                                startActivity(intent);
                                finish();
                                return;
                            }
                            user_profile = true;
                            referenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot2) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(Login.this, Navigation.class);
                                    intent.addFlags(335544320);
                                    startActivity(intent);
                                    finish();
                                    return;
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


                } else {
                    Toast.makeText(getApplicationContext(), "Wrong email/password combination. Try again.", 0).show();
                    dialog.dismiss();
                }

            }
        });

    }
}
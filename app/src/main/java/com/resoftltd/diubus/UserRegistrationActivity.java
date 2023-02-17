package com.resoftltd.diubus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.resoftltd.diubus.Models.User;

public class UserRegistrationActivity extends AppCompatActivity {

    FirebaseAuth auth;
    ProgressDialog dialog;

    EditText editTextUserEmail;

    EditText editTextUserName;

    EditText editTextUserPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        this.auth = FirebaseAuth.getInstance();
        FirebaseDatabase.getInstance().goOnline();
        this.dialog = new ProgressDialog(this);

        editTextUserName = findViewById(R.id.UserNameid);
        editTextUserPassword = findViewById(R.id.Passwordid);
        editTextUserEmail = findViewById(R.id.Emailid);

    }

    public void registerUser(View view) {
        this.dialog.setTitle("Creating account");
        this.dialog.setMessage("Please wait");
        this.dialog.show();
        String obj = this.editTextUserName.getText().toString();
        String obj2 = this.editTextUserEmail.getText().toString();
        String obj3 = this.editTextUserPassword.getText().toString();
        if (obj.equals("") && obj2.equals("") && obj3.equals("")) {
            this.dialog.dismiss();
            Toast.makeText(getApplicationContext(), "Please enter correct details", Toast.LENGTH_SHORT).show();
            return;
        }
        doStuffUser();
    }

    public void doStuffUser() {
        this.auth.createUserWithEmailAndPassword(this.editTextUserEmail.getText().toString(), this.editTextUserPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseDatabase.getInstance().getReference().child("Users").child(UserRegistrationActivity.this.auth.getCurrentUser().getUid()).setValue(new User(UserRegistrationActivity.this.editTextUserName.getText().toString(), UserRegistrationActivity.this.editTextUserEmail.getText().toString(), UserRegistrationActivity.this.editTextUserPassword.getText().toString())).addOnCompleteListener(new OnCompleteListener<Void>() {

                        public void onComplete(@NonNull Task<Void> task2) {
                            if (task2.isSuccessful()) {
                                UserRegistrationActivity.this.dialog.dismiss();
                                Toast.makeText(UserRegistrationActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                UserRegistrationActivity.this.finish();
                                Intent intent = new Intent(UserRegistrationActivity.this, Navigation.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                UserRegistrationActivity.this.startActivity(intent);
                                return;
                            }
                            Toast.makeText(UserRegistrationActivity.this.getApplicationContext(), "Could not create account", Toast.LENGTH_SHORT).show();
                            UserRegistrationActivity.this.dialog.dismiss();
                        }
                    });
                    return;
                }
                UserRegistrationActivity.this.dialog.dismiss();
                Toast.makeText(UserRegistrationActivity.this, "Could not register student.", Toast.LENGTH_LONG).show();
            }
        });
    }

}
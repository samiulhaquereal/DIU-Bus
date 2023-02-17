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

public class Login extends AppCompatActivity {
    FirebaseAuth auth;
    ProgressDialog dialog;
    EditText editTextUserEmail;
    EditText editTextUserPassword;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUserPassword = findViewById(R.id.loginidpass);
        editTextUserEmail = findViewById(R.id.loginidemail);

        auth = FirebaseAuth.getInstance();

        this.dialog = new ProgressDialog(this);

    }

    public void login(View view) {
        this.dialog.setMessage("Logging in. Please wait.");
        this.dialog.show();
        if (this.editTextUserEmail.getText().toString().equals("") || this.editTextUserPassword.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Blank fields not allowed.", Toast.LENGTH_SHORT).show();
            this.dialog.dismiss();
            return;
        }
        this.auth.signInWithEmailAndPassword(this.editTextUserEmail.getText().toString(), this.editTextUserPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            @SuppressLint("WrongConstant")
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    dialog.dismiss();
                    Intent intent = new Intent(Login.this, Navigation.class);
                    intent.addFlags(335544320);
                    startActivity(intent);
                    finish();
                    return;
                }
                Toast.makeText(getApplicationContext(), "Wrong email/password combination. Try again.", 0).show();
                dialog.dismiss();
            }
        });
    }
}
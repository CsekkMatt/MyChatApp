package com.example.mychatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private Button mLoginBtn;

    private ProgressDialog mLoginProgressDialog;

    private TextInputEditText mLoginEmail;
    private TextInputEditText mLoginPassword;


    private FirebaseAuth mAuth;

    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mLoginBtn = (Button)findViewById(R.id.login_btn);
        mLoginEmail = (TextInputEditText)findViewById(R.id.login_email);
        mLoginPassword = (TextInputEditText)findViewById(R.id.login_password);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login");

        mLoginProgressDialog = new ProgressDialog(this);

      mLoginBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              String email = mLoginEmail.getText().toString();
              String password = mLoginPassword.getText().toString();

              if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                  mLoginProgressDialog.setTitle("Logging In");
                  mLoginProgressDialog.setMessage("Please wait while we check your credentials");
                  mLoginProgressDialog.setCanceledOnTouchOutside(false);
                  mLoginProgressDialog.show();
                  loginUser(email,password);
              }
          }
      });
    }

    private void loginUser(String email,String password) {
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    mLoginProgressDialog.dismiss();

                    //Add Device Token to Users-database.
                    String current_user_id = mAuth.getCurrentUser().getUid();

                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    mUserDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Intent loginIntent = new Intent(LoginActivity.this,MainActivity.class);
                            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(loginIntent);
                            finish();
                        }
                    });

                }else{
                    mLoginProgressDialog.hide();
                    Toast.makeText(LoginActivity.this,"Cannot sign in,Please check the form and try again.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}

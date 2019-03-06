package com.example.mychatapp.Helper;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.util.Patterns;

import com.google.firebase.auth.FirebaseAuth;

public class LoginValidate {

    //Log out the actual user.
    public void LogOut(FirebaseAuth mAuth) {
        mAuth.signOut();
    }

    //Validate email&password field. return true if correct
    public boolean validate(TextInputEditText mEmail, TextInputEditText mPassword) {

        boolean valid = true;

        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError("Enter a valid email address");
            valid = false;
        } else {
            mEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 6 || password.length() > 10) {
            mPassword.setError("between 6 and 10 characters");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        return valid;
    }




}

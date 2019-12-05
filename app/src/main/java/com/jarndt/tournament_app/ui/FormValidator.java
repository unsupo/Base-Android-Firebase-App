package com.jarndt.tournament_app.ui;

import android.text.TextUtils;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;

public class FormValidator {
    boolean valid = true;
    String password;
    public FormValidator(){}

    public FormValidator validateEmail(EditText _emailText){
        String email = _emailText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            _emailText.setError("Required.");
            this.valid = false;
        } else if(!email.matches("\\w+@\\w+\\.com")){
            _emailText.setError("email badly formatted.");
            this.valid = false;
        } else
            _emailText.setError(null);
        return this;
    }

    public FormValidator validatePassword(EditText _passwordText){
        this.password = _passwordText.getText().toString();
        if (TextUtils.isEmpty(password)) {
            _passwordText.setError("Required.");
            valid = false;
        } else if(password.length() < 6){
            _passwordText.setError("Password can't be less than 6 characters.");
            valid = false;
        }else
            _passwordText.setError(null);
        return this;
    }

    public FormValidator validateSecondPassword(EditText _secondPassword){
        String second = _secondPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            _secondPassword.setError("Required.");
            valid = false;
        }else if(!this.password.equals(second)) {
            _secondPassword.setError("Passwords must match");
            valid = false;
        }else
            _secondPassword.setError(null);
        return this;
    }

    public boolean validate(){
        return valid;
    }
}

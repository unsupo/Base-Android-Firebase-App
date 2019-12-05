package com.jarndt.tournament_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.jarndt.tournament_app.ui.BaseActivity;
import com.jarndt.tournament_app.ui.FormValidator;
import com.jarndt.tournament_app.utilities.Authentication;
import com.jarndt.tournament_app.utilities.Network;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends BaseActivity {

    private static final String TAG = "SignupActivity";

    @BindView(R.id.input_name) EditText _nameText;
//    @BindView(R.id.input_address) EditText _addressText;
    @BindView(R.id.input_email) EditText _emailText;
//    @BindView(R.id.input_mobile) EditText _mobileText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.input_reEnterPassword)
    EditText _reEnterPasswordText;
    @BindView(R.id.btn_signup)
    Button _signupButton;
    @BindView(R.id.link_login)
    TextView _loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        _signupButton.setOnClickListener(v -> signup());
        _loginLink.setOnClickListener(v -> {
            // Finish the registration screen and return to the Login activity
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        });
    }

    private void signup() {
        hideKeyboard(this.getCurrentFocus());
        if (!new FormValidator().validateEmail(_emailText)
                .validatePassword(_passwordText)
                .validateSecondPassword(_reEnterPasswordText).validate())
            return;
        runOnUiThread(this::showProgressDialog);
        if(!Network.isNetworkConnected(this)){
            Toast.makeText(SignupActivity.this, "No Internet Available.",
                    Toast.LENGTH_SHORT).show();
            runOnUiThread(this::hideProgressDialog);
            return;
        }
        try {
            Authentication.signUp(
                    _emailText.getText().toString(),
                    _passwordText.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = Authentication.getUser();
                            if(!_nameText.getText().toString().isEmpty())
                                Authentication.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(_nameText.getText().toString()))
                                        .addOnCompleteListener(secondTask -> {
                                            if(task.isSuccessful())
                                                updateUI(user);
                                            else
                                                failure(task.getException());
                                        });
                            else
                                updateUI(user);
                        } else
                            failure(task.getException());
//                            updateUI(null);

                        // [START_EXCLUDE]
//                        if (!task.isSuccessful())
//                            runOnUiThread(() -> mStatusTextView.setText(R.string.auth_failed+" "+task.getException().getMessage()));
                        runOnUiThread(this::hideProgressDialog);
                        // [END_EXCLUDE]
                    });
        }catch (Exception e){
            runOnUiThread(() -> {
//                mStatusTextView.setText(e.getMessage());
//                mStatusTextView.setVisibility(View.VISIBLE);
                hideProgressDialog();
            });
        }
    }

    private void failure(Exception exception) {
        // If sign in fails, display a message to the user.
        Log.w(TAG, "signInWithEmail:failure", exception);
        String error = exception.getMessage(), err = error.toLowerCase();
        Toast.makeText(SignupActivity.this, "Authentication failed." + error,
                Toast.LENGTH_SHORT).show();
        if(err.contains("email") || err.contains("user"))
            _emailText.setError(error);
        if(err.contains("password"))
            _passwordText.setError(error);
    }

    private void updateUI(FirebaseUser user) {
        if(user == null) return;
        // just send the link, no need to do anything after
        Authentication.sendVerifyLink();
        /*
        // https://github.com/firebase/quickstart-android/blob/34ffee4430c4fcce994c4f38d7669a6a73617bfa/database/app/src/main/java/com/google/firebase/quickstart/database/java/SignInActivity.java#L119
        String username = usernameFromEmail(user.getEmail());

        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail());
         */
        // Go to MainActivity
        startActivity(new Intent(SignupActivity.this, MainActivity.class));
        finish();
    }
}

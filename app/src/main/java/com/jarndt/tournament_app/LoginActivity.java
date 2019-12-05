package com.jarndt.tournament_app;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.jarndt.tournament_app.ui.BaseActivity;
import com.jarndt.tournament_app.ui.FormValidator;
import com.jarndt.tournament_app.utilities.Authentication;
import com.jarndt.tournament_app.utilities.Network;
import com.mikhaellopez.circularimageview.CircularImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static final int RC_SIGN_IN = 1;

    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_password)
    EditText _passwordText;
    @BindView(R.id.btn_login)
    Button _loginButton;
    @BindView(R.id.link_signup)
    TextView _signupLink;
    @BindView(R.id.failure)
    TextView mStatusTextView;
    @BindView(R.id.google)
    CircularImageView _google;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Authentication.isLoggedIn())
            updateUI(Authentication.getUser());
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        _loginButton.setOnClickListener(v -> login());
        _signupLink.setOnClickListener(v -> {
            // Start the Signup activity
            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivityForResult(intent, REQUEST_SIGNUP);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        });
        _google.setOnClickListener(v ->{
            Authentication.loginWithGoogle(this);

            Intent signInIntent = Authentication.getGoogleAuth().getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    private void login() {
        hideKeyboard(this.getCurrentFocus());
        Log.d(TAG, "signIn:" + _emailText.getText().toString());
        if (!new FormValidator().validateEmail(_emailText).validatePassword(_passwordText).validate())
            return;
        runOnUiThread(this::showProgressDialog);
        if(!Network.isNetworkConnected(this)){
            Toast.makeText(LoginActivity.this, "No Internet Available.",
                    Toast.LENGTH_SHORT).show();
            runOnUiThread(this::hideProgressDialog);
            return;
        }
        try {
            Authentication.loginUserNamePassword(
                    _emailText.getText().toString(),
                    _passwordText.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = Authentication.getUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            String error = task.getException().getMessage(), err = error.toLowerCase();
                            Toast.makeText(LoginActivity.this, "Authentication failed." + error,
                                    Toast.LENGTH_SHORT).show();
                            if(err.contains("email") || err.contains("user"))
                                _emailText.setError(error);
                            if(err.contains("password")) {
                                _passwordText.setError(error);
                                mStatusTextView.setText("Forgot password, Click here to Send Password Resent Link");
                                mStatusTextView.setVisibility(View.VISIBLE);
                                mStatusTextView.setOnClickListener(v ->
                                        Authentication.getAuth().sendPasswordResetEmail(_emailText.getText().toString())
                                            .addOnCompleteListener(task1 -> {
                                                if(task1.isSuccessful())
                                                    Toast.makeText(LoginActivity.this, "Email Sent",
                                                            Toast.LENGTH_SHORT).show();
                                                else
                                                    Toast.makeText(LoginActivity.this, "Failure: "+task1.getException().getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                            })
                                );
                            }
//                            updateUI(null);
                        }

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
        }finally {
//            runOnUiThread(this::hideProgressDialog);
        }
    }

    private void updateUI(FirebaseUser user) {
        if(user == null) return;
        /*
        // https://github.com/firebase/quickstart-android/blob/34ffee4430c4fcce994c4f38d7669a6a73617bfa/database/app/src/main/java/com/google/firebase/quickstart/database/java/SignInActivity.java#L119
        String username = usernameFromEmail(user.getEmail());

        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail());
         */
        // Go to MainActivity
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        Authentication.getAuth().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = Authentication.getUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Failure: "+task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();

                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

}

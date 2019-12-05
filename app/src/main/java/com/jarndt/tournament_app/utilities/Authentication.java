package com.jarndt.tournament_app.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jarndt.tournament_app.R;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.ByteArrayOutputStream;

public class Authentication {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    //Singleton
    private Authentication(){
        mAuth = FirebaseAuth.getInstance();
    }

    private static Authentication instance;

    public static Authentication getInstance(){
        if(instance == null)
            instance = new Authentication();
        return instance;
    }

    public static boolean isLoggedIn(){
        return getInstance().mAuth.getCurrentUser() != null;
    }

    public static Task<AuthResult> loginUserNamePassword(String email, String password) {
        return getInstance().mAuth.signInWithEmailAndPassword(email, password);
    }

    public static GoogleSignInClient getGoogleAuth() {
        return getInstance().mGoogleSignInClient;
    }

    public void _loginWithGoogle(Context context){
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }
    public static void loginWithGoogle(Context context){
        getInstance()._loginWithGoogle(context);
    }

    public static FirebaseAuth getAuth() {
        return getInstance().mAuth;
    }

    public static FirebaseUser getUser() {
        return getInstance().mAuth.getCurrentUser();
    }

    public static void logout() {
        getInstance().mAuth.signOut();
    }

    public static Task<AuthResult> signUp(String email, String password) {
        return getAuth().createUserWithEmailAndPassword(email,password);
    }

    public static Task<Void> sendVerifyLink() {
        return getUser().sendEmailVerification();
    }

    public static Task<Void> updateProfile(UserProfileChangeRequest.Builder userProfileChangeRequestBuilder) {
        return getUser().updateProfile(userProfileChangeRequestBuilder.build());
    }

    public static String stringToColour(String str) {
        return String.format("#%X", str.hashCode());
    }

    public static void setProfile(View view,Context context) {
        CircularImageView profileImage1 = view.findViewById(R.id.imageView1);
        ImageView profileImage2 = view.findViewById(R.id.imageView2);
        if(Authentication.getUser().getPhotoUrl() == null) {
            profileImage1.setVisibility(View.GONE);
            profileImage2.setVisibility(View.VISIBLE);
            String email = Authentication.getUser().getEmail(), name = Authentication.getUser().getDisplayName(), v = name;
            if (name == null || name.isEmpty())
                v = email;
            String letter = v.substring(0, 1);
            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
            TextDrawable d = TextDrawable.builder().buildRound(letter, generator.getColor(v));
            d.setIntrinsicHeight(profileImage2.getMaxHeight());
            d.setIntrinsicWidth(profileImage2.getMaxWidth());
            profileImage2.setImageDrawable(d);
        }else {
            profileImage1.setVisibility(View.VISIBLE);
            profileImage2.setVisibility(View.GONE);
//            profileImage1.setImageURI(Authentication.getUser().getPhotoUrl());
            Glide.with(view).load(Authentication.getUser().getPhotoUrl()).fitCenter().listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    Toast.makeText(context, e.getMessage().split("\n")[0],
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            }).into(profileImage1);
        }
    }


    public static void updateNavProfile(NavigationView navigationView,Context context) {
        View headerView = navigationView.getHeaderView(0);
        ((TextView)headerView.findViewById(R.id.name)).setText(
                getUser().getDisplayName()==null || Authentication.getUser().getDisplayName().isEmpty() ?
                        "No Name Given" :
                        getUser().getDisplayName());
        ((TextView)headerView.findViewById(R.id.email)).setText(Authentication.getUser().getEmail());
        setProfile(headerView,context);
    }

    public static UploadTask uploadImage(Bitmap bitmap,String name){
        // Create a storage reference from our app
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(name);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        return imageRef.putBytes(data);
//        uploadTask.addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                // Handle unsuccessful uploads
//            }
//        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
//                // ...
//            }
//        });
    }

//    public static FutureTask<Boolean> signUp(EditText _emailText, EditText _passwordText, Context context) {
//        FutureTask<Boolean> future =
//                new FutureTask<>(() -> {
//                    StringBuilder res = new StringBuilder();
//                    signUp(_emailText.getText().toString(), _passwordText.getText().toString())
//                            .addOnCompleteListener(task -> {
//                                if (task.isSuccessful()) {
//                                    // Sign in success, update UI with the signed-in user's information
//                                    FirebaseUser user = Authentication.getUser();
//                                    res.append("true");
//                                    //                updateUI(user);
//                                } else {
//                                    // If sign in fails, display a message to the user.
//                                    String error = task.getException().getMessage(), err = error.toLowerCase();
//                                    Toast.makeText(context, "Authentication failed." + error,
//                                            Toast.LENGTH_SHORT).show();
//                                    if (err.contains("email") || err.contains("user"))
//                                        _emailText.setError(error);
//                                    if (err.contains("password"))
//                                        _passwordText.setError(error);
//                                    //                            updateUI(null);
//                                }
//
//                                // [START_EXCLUDE]
//                                //                        if (!task.isSuccessful())
//                                //                            runOnUiThread(() -> mStatusTextView.setText(R.string.auth_failed+" "+task.getException().getMessage()));
//                                //            runOnUiThread(this::hideProgressDialog);
//                                // [END_EXCLUDE]
//                            });
//                    return !res.toString().isEmpty();
//                });
//        return future;
//    }
}

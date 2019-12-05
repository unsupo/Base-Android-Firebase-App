package com.jarndt.tournament_app.ui.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jarndt.tournament_app.R;
import com.jarndt.tournament_app.ui.BaseFragment;
import com.jarndt.tournament_app.utilities.Authentication;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileFragment extends BaseFragment {

    private static final int PHOTO_PICKER_FRAGMENT_DIALOG = 1;
    private ProfileViewModel profileViewModel;

    @BindView(R.id.profile_picture)
    RelativeLayout _profile_picture;
    @BindView(R.id.btn_save)
    Button _save_btn;
    @BindView(R.id.input_name)
    TextView _name;
    @BindView(R.id.imageView1)
    ImageView imageView1;
    @BindView(R.id.imageView2)
    ImageView imageView2;

    View root;

    private String url;
    private Bitmap bitmap;
    private Uri uri;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        root = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, root);
        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
//        final TextView textView = root.findViewById(R.id.text_profile);
//        profileViewModel.getText().observe(this, s -> textView.setText(s));
        Authentication.setProfile(root,getContext());

        _profile_picture.setOnClickListener(v -> {
            ProfileImageDialogFragment dialog = new ProfileImageDialogFragment();
            dialog.setTargetFragment(this, PHOTO_PICKER_FRAGMENT_DIALOG);
            dialog.show(getFragmentManager().beginTransaction(), "MyProgressDialog");
//            .show(getFragmentManager(),"PROFILE_IMAGE_DIALOG");
        });
        _name.setText(Authentication.getUser().getDisplayName());
        _save_btn.setOnClickListener(v -> {
            showProgressDialog();
            if(this.uri != null) {
                // Get the data from an ImageView as bytes
                imageView1.setDrawingCacheEnabled(true);
                imageView1.buildDrawingCache();
                this.bitmap = ((BitmapDrawable) imageView1.getDrawable()).getBitmap();
            }
            if(this.bitmap != null) {
                String name = String.format("profile/images/%s.jpg", Authentication.getUser().getEmail().replace(".","").replace("@",""));
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference imageRef = storageRef.child(name);
                Authentication.uploadImage(this.bitmap,name).continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(root.getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        hideProgressDialog();
                        return null;
                    }
                    // Continue with the task to get the download URL
                    return imageRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Authentication.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(task.getResult()))
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()) {
                                        Authentication.updateNavProfile(navigationView,getContext());
                                        Toast.makeText(root.getContext(), "SAVED", Toast.LENGTH_SHORT).show();
                                    }else
                                        Toast.makeText(root.getContext(),"FAILED: "+task1.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                    hideProgressDialog();
                                });
                    } else {
                        Toast.makeText(root.getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        hideProgressDialog();
                    }
                });
            }
            UserProfileChangeRequest.Builder profile = new UserProfileChangeRequest.Builder();
            if(!_name.getText().toString().equals(Authentication.getUser().getDisplayName()))
                profile = profile.setDisplayName(_name.getText().toString());
            if(this.url != null)
                profile = profile.setPhotoUri(Uri.parse(this.url));
            Authentication.updateProfile(profile)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Authentication.updateNavProfile(navigationView,getContext());
                        Toast.makeText(root.getContext(), "SAVED", Toast.LENGTH_SHORT).show();
                    }else
                        Toast.makeText(root.getContext(),"FAILED: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                });
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PHOTO_PICKER_FRAGMENT_DIALOG){
            Uri uri = (Uri) data.getExtras().get("uri");
            Bitmap bitmap = (Bitmap) data.getExtras().get("bitmap");
            String url = (String) data.getExtras().get("url");
            this.url = url;
            this.uri = uri;
            this.bitmap = bitmap;

            imageView1.setVisibility(View.VISIBLE);
            imageView2.setVisibility(View.GONE);

            if(url != null)
                Glide.with(ProfileFragment.this).load(url).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Toast.makeText(root.getContext(), e.getMessage().split("\n")[0],
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).into(imageView1);
            else if(uri != null)
                Glide.with(ProfileFragment.this).load(uri).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Toast.makeText(root.getContext(), e.getMessage().split("\n")[0],
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).into(imageView1);
            else if(bitmap != null)
                Glide.with(ProfileFragment.this).asBitmap().load(bitmap).listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Toast.makeText(root.getContext(), e.getMessage().split("\n")[0],
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).into(imageView1);
        }
    }
}
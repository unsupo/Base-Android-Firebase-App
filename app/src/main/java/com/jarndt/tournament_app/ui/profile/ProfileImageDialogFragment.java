package com.jarndt.tournament_app.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.jarndt.tournament_app.MainActivity;
import com.jarndt.tournament_app.R;
import com.jarndt.tournament_app.utilities.Authentication;

import java.io.FileNotFoundException;
import java.net.URI;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

public class ProfileImageDialogFragment extends DialogFragment {
    @BindView(R.id.input_url)
    TextView _url;
    @BindView(R.id.btn_camera)
    ImageButton _camera;
    @BindView(R.id.btn_upload)
    ImageButton _upload;
    @BindView(R.id.btn_save)
    Button _save;
    @BindView(R.id.imageView1)
    ImageView imageView1;
    @BindView(R.id.imageView2)
    ImageView imageView2;

    private View view;

    private String URL;
    private Bitmap photo;
    private Uri uri;


    private final int CAMERA_REQUEST_CODE = 1, PICKER_REQUEST_CODE = 2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_profile_image_dialog,container,false);
        ButterKnife.bind(this,view);
        Authentication.setProfile(view,getContext());
        _camera.setOnClickListener(v -> {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            startActivityForResult(intent,CAMERA_REQUEST_CODE);
        });
        _upload.setOnClickListener(v -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, PICKER_REQUEST_CODE);
        });
        _url.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                System.out.println();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().isEmpty())
                    return;
                imageView1.setVisibility(View.VISIBLE);
                imageView2.setVisibility(View.GONE);
                URL = s.toString();
                photo = null;
                uri = null;
                Glide.with(ProfileImageDialogFragment.this).load(s.toString()).fitCenter().listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Toast.makeText(view.getContext(), e.getMessage().split("\n")[0],
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).into(imageView1);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        _save.setOnClickListener(v -> {
            Intent i = new Intent()
                    .putExtra("bitmap",photo)
                    .putExtra("uri",uri)
                    .putExtra("url", URL);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
            dismiss();
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == CAMERA_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                this.photo = photo;
                URL = null;
                uri = null;
                imageView1.setVisibility(View.VISIBLE);
                imageView2.setVisibility(View.GONE);
                Glide.with(ProfileImageDialogFragment.this).asBitmap().load(photo).fitCenter().listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Toast.makeText(view.getContext(), e.getMessage().split("\n")[0],
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).into(imageView1);
            }
        } else if(requestCode == PICKER_REQUEST_CODE){
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                this.uri = selectedImage;
                photo = null;
                URL = null;
                imageView1.setVisibility(View.VISIBLE);
                imageView2.setVisibility(View.GONE);
                Glide.with(ProfileImageDialogFragment.this).load(selectedImage).fitCenter().listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Toast.makeText(view.getContext(), e.getMessage().split("\n")[0],
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).into(imageView1);
            }

        }
    }

    View.OnClickListener doneAction = v -> Toast.makeText(getActivity(),"Test", Toast.LENGTH_LONG).show();

}

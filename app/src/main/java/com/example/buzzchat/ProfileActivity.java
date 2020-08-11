package com.example.buzzchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class ProfileActivity extends AppCompatActivity {

    private EditText userName, userBio;
    private ImageButton imgSelectBtn;
    private ImageView profileImg;
    private Button saveBtn;
    private static int galleryPick = 1;
    private Uri imageUri;
    private StorageReference userProfileImgRef;
    private String downloadUrl;
    private DatabaseReference userRef;
    private ProgressDialog progressDialog;
    private boolean isImageFitToScreen;
    private ConstraintLayout totalImage;
    private byte[] finalImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        userProfileImgRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userName = (EditText) findViewById(R.id.userName);
        userBio = (EditText) findViewById(R.id.userBio);
        imgSelectBtn = (ImageButton) findViewById(R.id.selectImgBtn);
        profileImg = (ImageView) findViewById(R.id.profile_pic);
        saveBtn = (Button) findViewById(R.id.profileSaveBtn);
        progressDialog = new ProgressDialog(this);
        totalImage = findViewById(R.id.imageView);

        retrieveUserProfile();

        imgSelectBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPick);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserData();
            }
        });

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isImageFitToScreen) {
                    showImageFullScreen(isImageFitToScreen);
                }
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == galleryPick && resultCode == RESULT_OK && data!=null) {
            imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                imageUri = result.getUri();


                File actualImageFile = new File(imageUri.getPath());


                try {
                    Bitmap compressedImageFile = new Compressor(this)
                            .setMaxWidth(250)
                            .setMaxHeight(250)
                            .setQuality(30)
                            .compressToBitmap(actualImageFile);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 30, baos);
                    finalImage = baos.toByteArray();


                    profileImg.setImageURI(imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }


    private void saveUserData() {
        final String getUserName = userName.getText().toString();
        final String getUserBio = userBio.getText().toString();

        if(imageUri==null){
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("image")){
                        saveInfoOnlyWithoutImage();
                    }else{
                        Toast.makeText(ProfileActivity.this,"Select Profile Image",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else if(getUserName.equals("")){
            Toast.makeText(ProfileActivity.this,"Enter your name",Toast.LENGTH_SHORT).show();
        }else if(getUserBio.equals("")){
            Toast.makeText(ProfileActivity.this,"Enter your bio",Toast.LENGTH_SHORT).show();
        }else{
            progressDialog.setTitle("Updating Profile");
            progressDialog.setMessage("Please wait.....");
            progressDialog.show();

            final StorageReference filePath = userProfileImgRef.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            final UploadTask uploadTask = filePath.putBytes(finalImage);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }

                    downloadUrl = filePath.getDownloadUrl().toString();
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        downloadUrl = task.getResult().toString();

                        HashMap<String, Object> profileMap = new HashMap<>();
                        profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        profileMap.put("name", getUserName);
                        profileMap.put("bio", getUserBio);
                        profileMap.put("image", downloadUrl);

                        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    finish();
                                    progressDialog.dismiss();

                                    Toast.makeText(ProfileActivity.this,"Profile updated",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void saveInfoOnlyWithoutImage() {
        final String getUserName = userName.getText().toString();
        final String getUserBio = userBio.getText().toString();

        if(getUserName.equals("")){
            Toast.makeText(ProfileActivity.this,"Enter your name",Toast.LENGTH_SHORT).show();
        }else if(getUserBio.equals("")){
            Toast.makeText(ProfileActivity.this,"Enter your bio",Toast.LENGTH_SHORT).show();
        }else{

            progressDialog.setTitle("Updating Profile");
            progressDialog.setMessage("Please wait.....");
            progressDialog.show();

            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
            profileMap.put("name", getUserName);
            profileMap.put("bio", getUserBio);

            userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        finish();
                        progressDialog.dismiss();

                        Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private void retrieveUserProfile(){
        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String imageDb = snapshot.child("image").getValue().toString();
                            String nameDb = snapshot.child("name").getValue().toString();
                            String bioDb = snapshot.child("bio").getValue().toString();

                            userName.setText(nameDb);
                            userBio.setText(bioDb);
                            Picasso.get().load(imageDb).placeholder(R.drawable.default_profile_pic).into(profileImg);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logOut:
                FirebaseAuth.getInstance().signOut();
                Intent logOutIntent = new Intent(this, LoginActivity.class);
                startActivity(logOutIntent);
                finishAffinity();
                return true;
            case android.R.id.home:
                if(isImageFitToScreen){
                    showImageFullScreen(isImageFitToScreen);
                    return true;
                }else{
                    return super.onOptionsItemSelected(item);
                }

        }
        return super.onOptionsItemSelected(item);
    }

    public void showImageFullScreen(boolean isImageIsFitToScreen){
        if(isImageIsFitToScreen){
            isImageFitToScreen = false;
            RelativeLayout.LayoutParams paramss = new RelativeLayout.LayoutParams
                    ((int) ViewGroup.LayoutParams.WRAP_CONTENT, (int) ViewGroup.LayoutParams.WRAP_CONTENT);

            paramss.addRule(RelativeLayout.CENTER_HORIZONTAL);
            totalImage.setLayoutParams(paramss);

            profileImg.requestLayout();
            profileImg.getLayoutParams().height = 650;
            profileImg.getLayoutParams().width = 650;
            profileImg.setScaleType(ImageView.ScaleType.FIT_XY);
            imgSelectBtn.setVisibility(View.VISIBLE);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) totalImage.getLayoutParams();
            params.topMargin = 210;


        }else{
            isImageFitToScreen = true;
            totalImage.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
            imgSelectBtn.setVisibility(View.INVISIBLE);
            profileImg.requestLayout();

            profileImg.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            profileImg.getLayoutParams().height = 1050;

        }
    }
    @Override
    public void onBackPressed() {
        if(isImageFitToScreen){
            showImageFullScreen(isImageFitToScreen);
        }else{
            finish();
        }
    }

}
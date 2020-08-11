package com.example.buzzchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.util.HashMap;
import java.util.Objects;

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

        retrieveUserProfile();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == galleryPick && resultCode == RESULT_OK && data!=null){
            imageUri = data.getData();
            profileImg.setImageURI(imageUri);
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
            final UploadTask uploadTask = filePath.putFile(imageUri);

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

        }
        return super.onOptionsItemSelected(item);
    }

}
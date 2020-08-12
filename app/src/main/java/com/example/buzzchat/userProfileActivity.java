package com.example.buzzchat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class userProfileActivity extends AppCompatActivity {

    private String receiverId = "", receiverImage = "", receiverName = "";
    private ImageView userProfileImg;
    private Button cancelRequest, addUserFriend;
    private TextView userUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        receiverId = getIntent().getExtras().get("visit_user_id").toString();
        receiverImage = getIntent().getExtras().get("profile_image").toString();
        receiverName = getIntent().getExtras().get("profile_name").toString();

        userProfileImg = (ImageView) findViewById(R.id.userProfileImg);
        cancelRequest = (Button) findViewById(R.id.cancelRequest);
        addUserFriend = (Button) findViewById(R.id.addUserFriend);
        userUsername = (TextView) findViewById(R.id.userUsername);

        Picasso.get().load(receiverImage).into(userProfileImg);
        userUsername.setText(receiverName);

    }
}
package com.example.buzzchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class userProfileActivity extends AppCompatActivity {

    private String receiverId = "", receiverImage = "", receiverName = "";
    private ImageView userProfileImg;
    private Button cancelRequest, addUserFriend;
    private TextView userUsername;
    private String currentState = "new";
    private FirebaseAuth mAuth;
    private String senderUid;
    private DatabaseReference friendRequestRef, friendsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUid = mAuth.getCurrentUser().getUid();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        receiverId = getIntent().getExtras().get("visit_user_id").toString();
        receiverImage = getIntent().getExtras().get("profile_image").toString();
        receiverName = getIntent().getExtras().get("profile_name").toString();

        userProfileImg = (ImageView) findViewById(R.id.userProfileImg);
        cancelRequest = (Button) findViewById(R.id.cancelRequest);
        addUserFriend = (Button) findViewById(R.id.addUserFriend);
        userUsername = (TextView) findViewById(R.id.userUsername);

        Picasso.get().load(receiverImage).into(userProfileImg);
        userUsername.setText(receiverName.toUpperCase());
        cancelRequest.setVisibility(View.GONE);
        addUserFriend.setVisibility(View.GONE);

        manageClickEvents();
        addUserFriend.setVisibility(View.VISIBLE);

    }

    private void manageClickEvents() {

        friendRequestRef.child(senderUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChild(receiverId)){
                            String requestType = snapshot.child(receiverId).child("requestType").getValue().toString();
                            if(requestType.equals("sent")){

                                currentState = "requestSent";
                                addUserFriend.setVisibility(View.VISIBLE);
                                addUserFriend.setText("Request Sent");
                                addUserFriend.setEnabled(false);
                                cancelRequest.setVisibility(View.VISIBLE);
                                cancelRequest.setText("Cancel Request");
                            }else if(requestType.equals("received")){

                                currentState = "requestReceived";
                                addUserFriend.setVisibility(View.VISIBLE);
                                addUserFriend.setText("Accept Friend Request");
                                addUserFriend.setEnabled(true);
                                cancelRequest.setVisibility(View.VISIBLE);
                                cancelRequest.setText("Reject Friend Request");
                            }
                        }else{
                            friendsRef.child(senderUid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.hasChild(receiverId)){
                                                currentState = "friends";
                                                addUserFriend.setVisibility(View.GONE);
                                                cancelRequest.setVisibility(View.VISIBLE);
                                                cancelRequest.setText("Delete Friend");
                                            }else{
                                                currentState = "new";
                                                addUserFriend.setVisibility(View.VISIBLE);
                                                addUserFriend.setText("Add Friend");
                                                addUserFriend.setEnabled(true);
                                                cancelRequest.setVisibility(View.GONE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        addUserFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentState.equals("new")){
                    sendFriendRequest();
                }

                if(currentState.equals("requestReceived")){
                    acceptFriendRequest();
                }
            }
        });

        cancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentState.equals("requestSent")){
                    cancelFriendRequest();
                }
                if(currentState.equals("requestReceived")){
                    cancelFriendRequest();
                }
                if(currentState.equals("friends")){
                    removeFromFriendList();
                }
            }
        });
    }

    private void removeFromFriendList() {
        friendsRef.child(senderUid).child(receiverId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendsRef.child(receiverId).child(senderUid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                currentState = "new";
                                                addUserFriend.setVisibility(View.VISIBLE);
                                                addUserFriend.setText("Add Friend");
                                                addUserFriend.setEnabled(true);
                                                cancelRequest.setVisibility(View.GONE);
                                                Toast.makeText(userProfileActivity.this,"Friend Removed",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFriendRequest() {
        friendsRef.child(senderUid).child(receiverId)
                .child("Friends").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            friendsRef.child(receiverId).child(senderUid)
                                    .child("Friends").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){
                                                friendRequestRef.child(senderUid).child(receiverId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    friendRequestRef.child(receiverId).child(senderUid)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        currentState = "friends";
                                                                                        addUserFriend.setVisibility(View.GONE);
                                                                                        cancelRequest.setVisibility(View.VISIBLE);
                                                                                        cancelRequest.setText("Delete Friend");
                                                                                        Toast.makeText(userProfileActivity.this,"Friend Added",Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelFriendRequest() {
        friendRequestRef.child(senderUid).child(receiverId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestRef.child(receiverId).child(senderUid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                currentState = "new";
                                                addUserFriend.setVisibility(View.VISIBLE);
                                                addUserFriend.setEnabled(true);
                                                addUserFriend.setText("Add Friend");
                                                cancelRequest.setVisibility(View.GONE);
                                                Toast.makeText(userProfileActivity.this,"Request Cancelled",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendFriendRequest() {
        friendRequestRef.child(senderUid).child(receiverId)
                .child("requestType").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestRef.child(receiverId).child(senderUid)
                                    .child("requestType").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                currentState = "requestSent";
                                                addUserFriend.setVisibility(View.VISIBLE);
                                                addUserFriend.setText("Request Sent");
                                                addUserFriend.setEnabled(false);
                                                cancelRequest.setVisibility(View.VISIBLE);
                                                cancelRequest.setText("Cancel Request");
                                                Toast.makeText(userProfileActivity.this,"Friend Request Sent",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }

                    }
                });
    }
}
package com.example.buzzchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView NotificationsList;
    private DatabaseReference friendRequestRef, friendsRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);


        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        NotificationsList = (RecyclerView) findViewById(R.id.notificationList);
        NotificationsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(friendRequestRef.child(currentUid), Friends.class)
                .build();

        FirebaseRecyclerAdapter<Friends, NotificationsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, NotificationsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final NotificationsViewHolder holder, int position, @NonNull Friends model) {
                 holder.acceptBtn.setVisibility(View.VISIBLE);
                 holder.rejectBtn.setVisibility(View.VISIBLE);
                 holder.bioText.setVisibility(View.GONE);

                 final String listUserId = getRef(position).getKey();

                 DatabaseReference requestTypeRef = getRef(position).child("requestType").getRef();
                 requestTypeRef.addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String type = snapshot.getValue().toString();

                            if(type.equals("received")){
                                holder.cardViewNotification.setVisibility(View.VISIBLE);

                                userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild("image")){
                                            final String imgStr = snapshot.child("image").getValue().toString();
                                            Picasso.get().load(imgStr).into(holder.profileImageView);
                                        }
                                        final String nameStr = snapshot.child("name").getValue().toString();
                                        holder.userNameText.setText(nameStr);

                                        holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                acceptFriendRequest(listUserId);
                                            }
                                        });

                                        holder.rejectBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                cancelFriendRequest(listUserId);

                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }else {
                                holder.cardViewNotification.setVisibility(View.GONE);
                            }
                        }
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError error) {

                     }
                 });
            }

            @NonNull
            @Override
            public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friend_design, parent, false);
                NotificationsActivity.NotificationsViewHolder viewHolder = new NotificationsViewHolder(view);
                return viewHolder;
            }
        };
        NotificationsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();


    }

    public class NotificationsViewHolder extends RecyclerView.ViewHolder{

        TextView userNameText, bioText;
        Button acceptBtn, rejectBtn;
        ImageView profileImageView;
        RelativeLayout cardViewNotification;

        public NotificationsViewHolder(@NonNull View itemView) {
            super(itemView);


            userNameText = (TextView) itemView.findViewById(R.id.findFriendUsername);
            bioText = (TextView) itemView.findViewById(R.id.findFriendBio);
            acceptBtn = (Button) itemView.findViewById(R.id.acceptBtn);
            rejectBtn = (Button) itemView.findViewById(R.id.rejectBtn);
            profileImageView = (ImageView) itemView.findViewById(R.id.findFriendImage);
            cardViewNotification = (RelativeLayout) itemView.findViewById(R.id.cardViewNotification);
        }
    }

    private void cancelFriendRequest(final String listUserId) {
        friendRequestRef.child(currentUid).child(listUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestRef.child(listUserId).child(currentUid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(NotificationsActivity.this,"Friend Request Cancelled",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFriendRequest(final String listUserId) {
        friendsRef.child(currentUid).child(listUserId)
                .child("Friends").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            friendsRef.child(listUserId).child(currentUid)
                                    .child("Friends").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){
                                                friendRequestRef.child(currentUid).child(listUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    friendRequestRef.child(listUserId).child(currentUid)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        Toast.makeText(NotificationsActivity.this,"Friend Added",Toast.LENGTH_SHORT).show();
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
}
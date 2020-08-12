package com.example.buzzchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FriendsListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
    }


    public static class findFriendsViewHolder extends RecyclerView.ViewHolder{

        TextView userNameText;
        ImageButton audioCallBtn, videoCallBtn, messageBtn;
        ImageView profileImageView;
        RelativeLayout cardViewFriend;

        public findFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            userNameText = (TextView) itemView.findViewById(R.id.FriendUsername);
            audioCallBtn = (ImageButton) itemView.findViewById(R.id.audioCallFriend);
            videoCallBtn = (ImageButton) itemView.findViewById(R.id.videoCallFriend);
            messageBtn = (ImageButton) itemView.findViewById(R.id.messageFriend);
            profileImageView = (ImageView) itemView.findViewById(R.id.FriendImage);
            cardViewFriend = (RelativeLayout) itemView.findViewById(R.id.cardViewFriendsList);
        }
    }
}
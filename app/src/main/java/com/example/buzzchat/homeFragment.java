package com.example.buzzchat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class homeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    boolean clicked = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){



        View view =  inflater.inflate(R.layout.activity_home,container,false);
        setHasOptionsMenu(true);


        final Animation rotateOpen = AnimationUtils.loadAnimation(getContext(),R.anim.rotate_open_anim);
        final Animation rotateClose = AnimationUtils.loadAnimation(getContext(),R.anim.rotate_close_anim);
        final Animation fromBottom = AnimationUtils.loadAnimation(getContext(),R.anim.from_bottom_anim);
        final Animation toBottom = AnimationUtils.loadAnimation(getContext(),R.anim.to_bottom_anim);
        final FloatingActionButton fabExpander = (FloatingActionButton) view.findViewById(R.id.fabExpander);
        final FloatingActionButton fabFriends = (FloatingActionButton) view.findViewById(R.id.fabFriends);
        final FloatingActionButton fabNotifications = (FloatingActionButton) view.findViewById(R.id.fabNotifications);
        final FloatingActionButton fabAddFriends = (FloatingActionButton) view.findViewById(R.id.fabAddFriends);

        RelativeLayout homeActivity = (RelativeLayout) view.findViewById(R.id.homeActivity);
        homeActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!clicked){
                    fabFriends.setVisibility(View.GONE);
                    fabNotifications.setVisibility(View.GONE);
                    fabAddFriends.setVisibility(View.GONE);
                    fabFriends.startAnimation(toBottom);
                    fabNotifications.setAnimation(toBottom);
                    fabAddFriends.setAnimation(toBottom);
                    fabExpander.setAnimation(rotateClose);
                    clicked = !clicked;
                }
            }
        });

        fabExpander.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clicked){
                    fabFriends.setVisibility(View.VISIBLE);
                    fabNotifications.setVisibility(View.VISIBLE);
                    fabAddFriends.setVisibility(View.VISIBLE);
                    fabFriends.startAnimation(fromBottom);
                    fabNotifications.setAnimation(fromBottom);
                    fabAddFriends.setAnimation(fromBottom);
                    fabExpander.setAnimation(rotateOpen);
                }else{
                    fabFriends.setVisibility(View.GONE);
                    fabNotifications.setVisibility(View.GONE);
                    fabAddFriends.setVisibility(View.GONE);
                    fabFriends.startAnimation(toBottom);
                    fabNotifications.setAnimation(toBottom);
                    fabAddFriends.setAnimation(toBottom);
                    fabExpander.setAnimation(rotateClose);
                }
                clicked = !clicked;
            }
        });

        fabFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(),FriendsListActivity.class);
                startActivity(i);
            }
        });

        fabAddFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(),findPeople.class);
                startActivity(i);
            }
        });

        fabNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(),NotificationsActivity.class);
                startActivity(i);
            }
        });

        return view;
    }

}

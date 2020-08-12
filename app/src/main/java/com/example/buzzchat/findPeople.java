package com.example.buzzchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class findPeople extends AppCompatActivity {

    private ImageButton backBtnforSearchET, clearTextBtn, backBtnNormal, searchOnBtn;
    private EditText searchText;
    private RecyclerView findPeopleList;
    private RelativeLayout searchOn, searchOff;
    private String str = "";
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        backBtnforSearchET = (ImageButton) findViewById(R.id.backBtnForSearchET);
        clearTextBtn = (ImageButton) findViewById(R.id.clearBtn);
        backBtnNormal = (ImageButton) findViewById(R.id.backBtnNormal);
        searchOnBtn = (ImageButton) findViewById(R.id.searchOnBtn);
        searchText = (EditText) findViewById(R.id.searchText);
        searchOn = (RelativeLayout) findViewById(R.id.searchOn);
        searchOff = (RelativeLayout) findViewById(R.id.searchOff);
        findPeopleList = (RecyclerView) findViewById(R.id.findPeopleList);
        findPeopleList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        backBtnNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        searchOnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchOn.setVisibility(View.VISIBLE);
                searchOff.setVisibility(View.GONE);
                searchText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        backBtnforSearchET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchOn.setVisibility(View.GONE);
                searchOff.setVisibility(View.VISIBLE);
                searchText.setText("");
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchText.getWindowToken(),0);
            }
        });

        clearTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchText.setText("");
            }
        });

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(searchText.getText().toString().equals("")){
                    str = "";
                }else{
                    str = charSequence.toString();
                }
                searchView();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        searchView();
    }

    public void searchView(){
        FirebaseRecyclerOptions<Friends> options = null;

        if(str.equals("")){
            options = new FirebaseRecyclerOptions.Builder<Friends>()
                    .setQuery(userRef, Friends.class)
                    .build();
        }else{
            options = new FirebaseRecyclerOptions.Builder<Friends>()
                    .setQuery(userRef.orderByChild("name").startAt(str).endAt(str + "\uf8ff"), Friends.class)
                    .build();
        }

        FirebaseRecyclerAdapter<Friends, findFriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, findFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull findFriendsViewHolder holder, final int position, @NonNull final Friends model) {
                holder.userNameText.setText(model.getName());
                Picasso.get().load(model.getImage()).into(holder.profileImageView);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String visit_user_id = getRef(position).getKey();

                        Intent i = new Intent(findPeople.this, userProfileActivity.class);
                        i.putExtra("visit_user_id",visit_user_id);
                        i.putExtra("profile_image",model.getImage());
                        i.putExtra("profile_name",model.getName());
                        startActivity(i);
                    }
                });
            }

            @NonNull
            @Override
            public findFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friend_design, parent, false);
                findFriendsViewHolder viewHolder = new findFriendsViewHolder(view);
                return viewHolder;
            }
        };
        findPeopleList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class findFriendsViewHolder extends RecyclerView.ViewHolder{

        TextView userNameText;
        Button acceptBtn, rejectBtn;
        ImageView profileImageView;
        RelativeLayout cardViewNotification;

        public findFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            userNameText = (TextView) itemView.findViewById(R.id.findFriendUsername);
            acceptBtn = (Button) itemView.findViewById(R.id.acceptBtn);
            rejectBtn = (Button) itemView.findViewById(R.id.rectangle);
            profileImageView = (ImageView) itemView.findViewById(R.id.findFriendImage);
            cardViewNotification = (RelativeLayout) itemView.findViewById(R.id.cardViewNotification);
        }
    }
}
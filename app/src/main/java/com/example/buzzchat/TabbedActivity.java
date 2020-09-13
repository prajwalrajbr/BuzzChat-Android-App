package com.example.buzzchat;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class TabbedActivity extends AppCompatActivity {




    private static final String TAG = "TabbedActivity";
    private SectionsPageAdapter sectionPageAdapter;
    private ViewPager viewPager;
    private FirebaseAuth mAuth;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        sectionPageAdapter = new SectionsPageAdapter((getSupportFragmentManager()));
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        setupViewPager(viewPager);


        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        ValidateUser();
    }

    private  void ValidateUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child("Users").child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    Intent ProfileIntent = new Intent(TabbedActivity.this, userProfileActivity.class);
                    startActivity(ProfileIntent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setupViewPager(ViewPager viewPager){
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new homeFragment(), "Home");
        adapter.addFragment(new igScreenFragment(), "Feed");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logOut:
                FirebaseAuth.getInstance().signOut();
                Intent logOutIntent = new Intent(this, LoginActivity.class);
                startActivity(logOutIntent);
                finish();
                return true;
            case R.id.Profile:
                Intent profile = new Intent(this, MyProfileActivity.class);
                startActivity(profile);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
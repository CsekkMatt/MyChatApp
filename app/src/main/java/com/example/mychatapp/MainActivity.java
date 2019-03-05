package com.example.mychatapp;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.widget.TableLayout;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {




    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    Button mLogout;
    Helper helper = new Helper();

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private DatabaseReference mUserRef;

    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chat");

        //Tabs
        mViewPager = (ViewPager)findViewById(R.id.main_Pager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        if (mAuth.getCurrentUser() != null) {


            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }

        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout)findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }


    @Override
    public void onStart(){
        super.onStart();
        //Check if logged in or not
        FirebaseUser getCurentUser = mAuth.getCurrentUser();
        if (getCurentUser == null) {
            sendToStart();
        }else{
            mUserRef.child("online").setValue("true");
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser getCurentUser = mAuth.getCurrentUser();
        if(getCurentUser != null) {
            //User status set, Offline.
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void sendToStart() {

            Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(startIntent);
            finish();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);

         if(item.getItemId() == R.id.logout_btn){
             helper.LogOut(mAuth);
             sendToStart();
         }

         if(item.getItemId() == R.id.main_settings_btn){
             Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
             startActivity(settingsIntent);
         }

         if(item.getItemId() == R.id.main_all){
             Intent allusersIntent = new Intent(MainActivity.this,UsersActivity.class);
             startActivity(allusersIntent);
         }

         return true;

    }
}

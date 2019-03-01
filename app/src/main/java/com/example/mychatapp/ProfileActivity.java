package com.example.mychatapp;

import android.app.ProgressDialog;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileDisplayName,mProfileStatus,mProfileFriendsCount;
    private Button mProfileSendReqBtn,mProfileDeclineBtn;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFirendDatabase;
    private DatabaseReference mNotificationDatabase;

    private DatabaseReference mRootRef;

    private FirebaseUser mCurrentUser;


    private ProgressDialog mProgressDialog;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //user_id. The other user ID. Not the actual user.
        final String user_id =  getIntent().getStringExtra("user_id");
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFirendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        //Initalize components
        mProfileDisplayName = (TextView)findViewById(R.id.profile_display_name);
        mProfileImage = (ImageView)findViewById(R.id.profile_image);
        mProfileStatus = (TextView)findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView)findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn = (Button)findViewById(R.id.profile_send_reg_btn);
        mProfileDeclineBtn = (Button)findViewById(R.id.profile_decline_btn);

        mCurrent_state = "not_friends";
        mProfileDeclineBtn.setVisibility(View.VISIBLE);
        mProfileDeclineBtn.setEnabled(false);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load the user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();


        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileDisplayName.setText(display_name);
                mProfileStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.defaultprof).into(mProfileImage);

                //**---------------------------FRIENDS LIST / REQUEST FEATURE ---------
                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       if(dataSnapshot.hasChild(user_id)){
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("received")){
                                mCurrent_state = "req_received";
                                mProfileSendReqBtn.setText("Accept Friend Request");

                                mProfileDeclineBtn.setVisibility(View.VISIBLE);
                                mProfileDeclineBtn.setEnabled(true);

                            }else if(req_type.equals("sent")){
                                mCurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);
                            }
                           mProgressDialog.dismiss();

                       }else{
                           mFirendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                               @Override
                               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                   if(dataSnapshot.hasChild(user_id)){
                                        mCurrent_state = "friends";
                                        mProfileSendReqBtn.setText("Unfriend this Person");

                                       mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                       mProfileDeclineBtn.setEnabled(false);
                                   }
                                   mProgressDialog.dismiss();

                               }

                               @Override
                               public void onCancelled(@NonNull DatabaseError databaseError) {
                                   mProgressDialog.dismiss();

                               }
                           });
                       }



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileSendReqBtn.setEnabled(false);
                //-------- NOT FRIENDS STATEMENT --------------------------------------------------------
                //UPDATED VERSION...
                if(mCurrent_state.equals("not_friends")){

                    DatabaseReference newNotificationRef = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationID = newNotificationRef.getKey();

                    HashMap<String,String> notificationData = new HashMap<>();
                    notificationData.put("from",mCurrentUser.getUid());
                    notificationData.put("type","request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/"+ mCurrentUser.getUid() + "/" + user_id + "/request_type","sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid() + "/request_type","received");
                    requestMap.put("notifications/" + user_id + "/" + newNotificationID,notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Toast.makeText(ProfileActivity.this,"Error in sending request",Toast.LENGTH_SHORT).show();
                            }
                            mProfileSendReqBtn.setEnabled(true);
                            mCurrent_state = "req_sent";
                            mProfileSendReqBtn.setText("Cancel Firend Request");
                        }
                    });
                }
                //--------Cancel Request state--------------------------------------------------------
                if(mCurrent_state.equals("req_sent")){

                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mProfileSendReqBtn.setText("Send Friend Request");

                                    mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                    mProfileDeclineBtn.setEnabled(false);
                                }
                            });
                        }
                    });

                }

                //-----------------REQUEST RECEIVED STATE------------------------

                if(mCurrent_state.equals("req_received")){
                    final String currentDate = DateFormat.getDateInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date",currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/date",currentDate);

                    friendsMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id,null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid(),null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError == null){
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = "friends";
                                mProfileSendReqBtn.setText("Unfriend this Person");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);
                            }else{
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                }

                ////////// ---------------- UNFRIEND ------------------
                if(mCurrent_state.equals("friends")){
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id,null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid(),null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError == null){
                                mCurrent_state = "not_friends";
                                mProfileSendReqBtn.setText("Send Friend Request");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);
                            }else{
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();

                            }
                            mProfileSendReqBtn.setEnabled(true);

                        }
                    });


                }

            }
        });


    }


}

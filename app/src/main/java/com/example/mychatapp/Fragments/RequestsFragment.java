package com.example.mychatapp.Fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.mychatapp.Model.Requests;
import com.example.mychatapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private TextView mDisplayName;
    private CircleImageView mProfileImage;
    private Button mAcceptBtn,mDeclineBtn;

    RecyclerView mRequestsList;

    private DatabaseReference mUserDatabase,mRequestDatabase,mFriendDatabase;
    private FirebaseAuth mAuth;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View requestView = inflater.inflate(R.layout.fragment_requests, container, false);



        mAcceptBtn = (Button)requestView.findViewById(R.id.single_request_accept_btn);
        mDeclineBtn = (Button)requestView.findViewById(R.id.profile_decline_btn);

        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();


        mRequestsList = (RecyclerView)requestView.findViewById(R.id.fragment_request_recyclerview);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(current_user_id);
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");






        mRequestsList.setHasFixedSize(true);
        mRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));





        /*

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


         */



        /*

        ACCEPT REQUEST.

            if(mCurrent_state.equals("req_received")){
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

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


         */


        return requestView;
    }

    @Override
    public void onStart() {
        super.onStart();
        startListening();
    }

    private void startListening() {


        Query query = mRequestDatabase
                        .limitToLast(10);

        FirebaseRecyclerOptions<Requests> options =
                new FirebaseRecyclerOptions.Builder<Requests>()
                        .setQuery(query,Requests.class)
                        .build();

        FirebaseRecyclerAdapter<Requests,RequestsViewHolder> requestsViewHolderFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Requests, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Requests model) {


                String list_user = getRef(position).getKey();
                Log.i("User",list_user); // MARIE key.


                mUserDatabase.child(list_user).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String user_name = dataSnapshot.child("name").getValue().toString();
                        final String user_thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                        holder.setName(user_name);
                        holder.setImage(user_thumb_image);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });







            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.friend_request_single,viewGroup,false);

                return new RequestsViewHolder(view);
            }
        };

        mRequestsList.setAdapter(requestsViewHolderFirebaseRecyclerAdapter);
        requestsViewHolderFirebaseRecyclerAdapter.startListening();


    }

    private class RequestsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }


        public void setName(String user_name) {
            mDisplayName = (TextView)mView.findViewById(R.id.single_request_name);
            mDisplayName.setText(user_name);
        }

        public void setImage(String user_thumb_image) {
            mProfileImage = (CircleImageView)mView.findViewById(R.id.single_request_image);
            Picasso.with(getContext()).load(user_thumb_image).networkPolicy(NetworkPolicy.OFFLINE).into(mProfileImage);
        }
    }
}

package com.example.mychatapp.Fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mychatapp.Activities.ChatActivity;
import com.example.mychatapp.Model.Friends;
import com.example.mychatapp.Activities.ProfileActivity;
import com.example.mychatapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Init view components.
        mMainView = inflater.inflate(R.layout.fragment_friends,container,false);

        mFriendsList = (RecyclerView)mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return mMainView;
    }


    //Get data from firebase db and load to Friends *card*
    @Override
    public void onStart() {
        super.onStart();
        startListening();

    }

    private void startListening() {

        Query query = mFriendsDatabase
                .limitToLast(50);

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(query,Friends.class)
                        .build();

        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {
                holder.setDate(model.date);

                final String list_user = getRef(position).getKey();


                mUsersDatabase.child(list_user).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String user_name = dataSnapshot.child("name").getValue().toString();

                        String user_thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                        if(dataSnapshot.hasChild("online")){
                            String user_online = dataSnapshot.child("online").getValue().toString();
                            holder.setUserOnline(user_online);

                        }
                        holder.setName(user_name);
                        holder.setImage(user_thumb_image,getContext());


                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CharSequence options[] = new CharSequence[]{"Open Profile","Send Message"};

                                AlertDialog.Builder builder  = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //CLick event for each item.
                                        switch (which){
                                            case 0:
                                                Intent profile_intent = new Intent(getContext(), ProfileActivity.class);
                                                profile_intent.putExtra("user_id",list_user);
                                                startActivity(profile_intent);
                                                break;
                                            case 1:
                                                Intent chat_intent = new Intent(getContext(), ChatActivity.class);
                                                chat_intent.putExtra("user_id",list_user);
                                                //We can put the user name, and user picture .. for saving loading data/time in ChatActivity.
                                                chat_intent.putExtra("user_name",user_name);
                                                //chat_intent.putExtra("user_picture",list_user_picture);
                                                startActivity(chat_intent);


                                        }
                                    }
                                });
                                builder.show();
                            }
                        });


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single,parent,false);

                return new FriendsViewHolder(view);
            }
        };
        mFriendsList.setAdapter(friendsRecyclerViewAdapter);
        friendsRecyclerViewAdapter.startListening();
    }


    public class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setDate(String date){
            TextView userNameView = (TextView) mView.findViewById(R.id.users_single_status);
            userNameView.setText(date);
        }

        public void setName(String name){
            TextView userNameView = (TextView)mView.findViewById(R.id.users_singe_name);
            userNameView.setText(name);
        }
        public void setImage(final String thumb_image,final Context ctx){
            final CircleImageView friends_image  = (CircleImageView)mView.findViewById(R.id.users_single_profile_image);
            Picasso.with(ctx).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.defaultprof).into(friends_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.defaultprof).into(friends_image);
                }
            });


        }
        public void setUserOnline(String online_status){
            ImageView userOnlineView = (ImageView)mView.findViewById(R.id.users_single_online_icon);
            if(online_status.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            }else{
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }
    }
}

package com.example.mychatapp.Fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.Image;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mychatapp.Activities.ChatActivity;
import com.example.mychatapp.Model.Conversation;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {


    private View mMainView;

    private RecyclerView mConversationList;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;


    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;




    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView =  inflater.inflate(R.layout.fragment_chats, container, false);

        mConversationList = (RecyclerView)mMainView.findViewById(R.id.fragment_chat_recyclerview);

        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);

        mConvDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);



        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConversationList.setHasFixedSize(true);
        mConversationList.setLayoutManager(linearLayoutManager);



        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        startListening();

    }



    private void startListening() {

            Query conversationQuery = mConvDatabase.orderByChild("timestamp");

            FirebaseRecyclerOptions<Conversation> options =
                    new FirebaseRecyclerOptions.Builder<Conversation>()
                            .setQuery(conversationQuery,Conversation.class).build();


            final FirebaseRecyclerAdapter<Conversation,ConversationViewHolder> conversationViewHolderRecyclerAdapter = new FirebaseRecyclerAdapter<Conversation, ConversationViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ConversationViewHolder holder, int position, @NonNull final Conversation model) {


                final String list_user_id = getRef(position).getKey();
                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);
                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        String data = dataSnapshot.child("message").getValue().toString();
                        holder.setMessage(data,model.isSeen());

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mConvDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            final String userName = dataSnapshot.child("name").getValue().toString();
                            String userThumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                            if (dataSnapshot.hasChild("online")) {
                                String userOnline = dataSnapshot.child("online").getValue().toString();
                                holder.setUserOnline(userOnline);
                            }

                            holder.setName(userName);
                            holder.setUserImage(userThumbImage, getContext());

                            holder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("user_id", list_user_id);
                                    chatIntent.putExtra("user_name", userName);
                                    startActivity(chatIntent);

                                }
                            });

                        }catch(Exception e){
                            Log.w("ALMA","Error");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });




            }

            @NonNull
            @Override
            public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single,parent,false);
                return new ConversationViewHolder(view);
            }
        };
        mConversationList.setAdapter(conversationViewHolderRecyclerAdapter);
        conversationViewHolderRecyclerAdapter.startListening();


    }


     public static class ConversationViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
         public void setMessage(String message,boolean isSeen){

            TextView userStatusView = (TextView)mView.findViewById(R.id.users_single_status);
             //Check if the message is a picture or no.
            if(message.toLowerCase().contains("firebasestorage.googleapis.com")){
                 userStatusView.setText("Photo");
             }else{
                 userStatusView.setText(message);
             }
             //check if seen or not.. and change text to bold or normal.
             if(!isSeen){
                 userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
                 userStatusView.setTextSize(15);
             }else{
                 userStatusView.setTypeface(userStatusView.getTypeface(),Typeface.NORMAL);
                 userStatusView.setTextSize(15);

             }
         }

         public void setUserOnline(String online_status){
            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.users_single_online_icon);
            if(online_status.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            }else{
                userOnlineView.setVisibility(View.INVISIBLE);
            }
         }

         public void setName(String name){
            TextView display_name = (TextView)mView.findViewById(R.id.users_singe_name);
            display_name.setText(name);
         }

         public void setUserImage(String thumb_image, Context ctx){
            ImageView profile_image = (ImageView)mView.findViewById(R.id.users_single_profile_image);
             Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.defaultprof).networkPolicy(NetworkPolicy.OFFLINE).into(profile_image);
         }

     }
}


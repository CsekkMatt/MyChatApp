package com.example.mychatapp;

;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
    private List<Messages> messagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;




    public MessageAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_single_layout,viewGroup,false);

        return new MessageViewHolder(v);
    }


    @Override
    public int getItemCount() {
        return messagesList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText,displayNameText;
        public CircleImageView mprofileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = (TextView)itemView.findViewById(R.id.single_message_text_layout);
            displayNameText = (TextView)itemView.findViewById(R.id.single_message_display_name);
            mprofileImage = (CircleImageView)itemView.findViewById(R.id.single_message_image_layout);

        }
    }


    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();
        final Messages c = messagesList.get(i);
        final String from_user = c.getFrom();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mUserDatabase.keepSynced(true);


        //Check the message sender.And change layout design.
        if(from_user.equals(current_user_id)){
            messageViewHolder.messageText.setBackgroundResource(R.drawable.sened_message_text_background);
            messageViewHolder.messageText.setTextColor(Color.WHITE);
            messageViewHolder.displayNameText.setTextColor(Color.BLACK);
            messageViewHolder.mprofileImage.setVisibility(View.VISIBLE);
        }else{

            messageViewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
            messageViewHolder.messageText.setTextColor(Color.WHITE);
            messageViewHolder.displayNameText.setTextColor(Color.BLACK);
            messageViewHolder.mprofileImage.setVisibility(View.VISIBLE);
        }


        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String from_user_image = dataSnapshot.child("image").getValue().toString();
                String from_user_name = dataSnapshot.child("name").getValue().toString();
                messageViewHolder.messageText.setText(c.getMessage());
                messageViewHolder.displayNameText.setText(from_user_name);
                Picasso.with(messageViewHolder.itemView.getContext()).load(from_user_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.defaultprof).into(messageViewHolder.mprofileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}

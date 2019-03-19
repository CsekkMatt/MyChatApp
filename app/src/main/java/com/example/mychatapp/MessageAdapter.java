package com.example.mychatapp;
import android.graphics.Color;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mychatapp.Helper.GetTimeAgo;
import com.example.mychatapp.Model.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

    public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
        private List<Messages> messagesList;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();

        private static final int VIEW_TYPE_MESSAGE_SENT = 1;
        private static final int VIEW_TYPE_MESSAGE_RECEIVED = 0;

        boolean isMyMessage;
        int messagePos;


    public MessageAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;

    }

        @Override
        public int getItemViewType(int position) {
            Messages chat = this.messagesList.get(position);
            String from_user = chat.getFrom();
            if(from_user.equals(current_user_id)){
                return VIEW_TYPE_MESSAGE_SENT;
            }else{
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
        }

        @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.my_message,viewGroup,false);
            switch (i) {
                case 1:
                isMyMessage = true;
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.my_message, viewGroup, false);
                break;
                case 0:
                isMyMessage = false;
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.message_single_layout, viewGroup, false);
            }


        return new MessageViewHolder(v);
    }


    @Override
    public int getItemCount() {
        return messagesList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView myMessageText,messageText,displayNameText,messageTime;
        public CircleImageView mprofileImage;
        public ImageView messageImage,myMessageImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            myMessageText = (TextView)itemView.findViewById(R.id.my_message_body);
            messageText = (TextView)itemView.findViewById(R.id.single_message_text_layout);
            messageTime = (TextView)itemView.findViewById(R.id.single_message_time);
            displayNameText = (TextView)itemView.findViewById(R.id.single_message_display_name);
            mprofileImage = (CircleImageView)itemView.findViewById(R.id.single_message_image_layout);
            myMessageImage = (ImageView)itemView.findViewById(R.id.my_message_text_image_layout);
            messageImage = (ImageView)itemView.findViewById(R.id.single_message_text_image_layout);

        }
    }


    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int i) {
        String current_user_id = mAuth.getCurrentUser().getUid();
        messagePos = i;
        final Messages c = messagesList.get(messagePos);
        final String from_user = c.getFrom();
        final String message_type = c.getType();
        final long time = c.getTime();

        String message_time = GetTimeAgo.getMessageTime(time);
        DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        //mUserDatabase.keepSynced(true);

        //check what viewtype need to be used
        switch (messageViewHolder.getItemViewType()){
            case VIEW_TYPE_MESSAGE_SENT:
                messageViewHolder.myMessageText.setBackgroundResource(R.drawable.my_message);
                messageViewHolder.myMessageText.setTextColor(Color.WHITE);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                messageViewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
                messageViewHolder.messageText.setTextColor(Color.WHITE);
                messageViewHolder.displayNameText.setTextColor(Color.BLACK);
                messageViewHolder.mprofileImage.setVisibility(View.VISIBLE);

                mUserDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String from_user_image = dataSnapshot.child("thumb_image").getValue().toString();
                        String from_user_name = dataSnapshot.child("name").getValue().toString();

                        messageViewHolder.displayNameText.setText(from_user_name);

                        Picasso.with(messageViewHolder.mprofileImage.getContext()).load(from_user_image)
                                .placeholder(R.drawable.defaultprof)
                                .into(messageViewHolder.mprofileImage);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        }


        //Set message or image.
        if(messageViewHolder.getItemViewType() == VIEW_TYPE_MESSAGE_SENT){
            if(message_type.equals("text")) {

                messageViewHolder.myMessageText.setText(c.getMessage());


            } if(message_type.equals("image")){

                messageViewHolder.myMessageText.setVisibility(View.INVISIBLE);
                Picasso.with(messageViewHolder.myMessageText.getContext()).load(c.getMessage())
                        .resize(800,800)
                        .placeholder(R.drawable.defaultprof).networkPolicy(NetworkPolicy.OFFLINE).into(messageViewHolder.myMessageImage);

            }
        }else {

            if (message_type.equals("text")) {

                messageViewHolder.messageText.setText(c.getMessage());
                messageViewHolder.messageImage.setVisibility(View.INVISIBLE);
                messageViewHolder.messageTime.setText(message_time);


            }
            if (message_type.equals("image")) {

                messageViewHolder.messageText.setVisibility(View.INVISIBLE);
                messageViewHolder.messageTime.setText(message_time);
                Picasso.with(messageViewHolder.mprofileImage.getContext()).load(c.getMessage())
                        .resize(800, 800)
                        .placeholder(R.drawable.defaultprof).into(messageViewHolder.messageImage);

            }
        }
    }



}

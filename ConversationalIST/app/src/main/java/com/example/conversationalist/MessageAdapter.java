package com.example.conversationalist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {

    private ArrayList<Message> messages;
    // sender and receiver only will work on 2 man chatroom might need to be a list of sender images
    // USER is senderImg
    private Context context;

    public MessageAdapter(ArrayList<Message> messages, Context context) {
        this.messages = messages;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_holder, parent,false);
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
        holder.txtMessage.setText(messages.get(position).getContent());
        holder.txtUsername.setText(messages.get(position).getUsername());
        Glide.with(context).load(messages.get(position).getImageContent()).placeholder(R.drawable.account_image).error(R.drawable.account_image).into(holder.imgSend);

        ConstraintLayout constraintLayout = holder.ccll;

        // if the message is ours, put on the right from the left
        if(messages.get(position).getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            // USER
            Glide.with(context).load(messages.get(position).getSenderImg()).error(R.drawable.account_image).placeholder(R.drawable.account_image).into(holder.profImage);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);

            constraintSet.clear(R.id.profile_cardView, ConstraintSet.LEFT);
            constraintSet.clear(R.id.txt_message_content, ConstraintSet.LEFT);
            constraintSet.clear(R.id.txt_message_content, ConstraintSet.LEFT);
            constraintSet.clear(R.id.img_send, ConstraintSet.LEFT);

            constraintSet.connect(R.id.profile_cardView, ConstraintSet.RIGHT, R.id.ccLayout, ConstraintSet.RIGHT, 0);
            constraintSet.connect(R.id.txt_message_content, ConstraintSet.RIGHT, R.id.ccLayout, ConstraintSet.RIGHT, 0);
            constraintSet.connect(R.id.img_send, ConstraintSet.RIGHT, R.id.ccLayout, ConstraintSet.RIGHT, 0);
            constraintSet.connect(R.id.txt_message_content, ConstraintSet.TOP, R.id.profile_cardView, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.img_send, ConstraintSet.TOP, R.id.txt_message_content, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.txt_username_content, ConstraintSet.RIGHT, R.id.profile_cardView, ConstraintSet.LEFT, 0);

            constraintSet.applyTo(constraintLayout);

        } else {
            // Others
            Glide.with(context).load(messages.get(position).getSenderImg()).error(R.drawable.account_image).placeholder(R.drawable.account_image).into(holder.profImage);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);

            constraintSet.clear(R.id.profile_cardView, ConstraintSet.RIGHT);
            constraintSet.clear(R.id.txt_message_content, ConstraintSet.RIGHT);
            constraintSet.clear(R.id.img_send, ConstraintSet.RIGHT);
            constraintSet.clear(R.id.txt_username_content, ConstraintSet.RIGHT);

            constraintSet.connect(R.id.profile_cardView, ConstraintSet.LEFT, R.id.ccLayout, ConstraintSet.LEFT, 0);
            constraintSet.connect(R.id.txt_message_content, ConstraintSet.LEFT, R.id.ccLayout, ConstraintSet.LEFT, 0);
            constraintSet.connect(R.id.txt_message_content, ConstraintSet.TOP, R.id.profile_cardView, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.img_send, ConstraintSet.LEFT, R.id.ccLayout, ConstraintSet.LEFT, 0);
            constraintSet.connect(R.id.img_send, ConstraintSet.TOP, R.id.txt_message_content, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.txt_username_content, ConstraintSet.LEFT, R.id.profile_cardView, ConstraintSet.RIGHT, 0);
            constraintSet.applyTo(constraintLayout);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MessageHolder extends RecyclerView.ViewHolder {
        ConstraintLayout ccll;
        TextView txtMessage;
        TextView txtUsername;
        ImageView profImage;
        ImageView imgSend;
        public MessageHolder(@NonNull View itemView) {
            super(itemView);

            ccll = itemView.findViewById(R.id.ccLayout);
            txtMessage = itemView.findViewById(R.id.txt_message_content);
            txtUsername = itemView.findViewById(R.id.txt_username_content);
            profImage = itemView.findViewById(R.id.small_profile_img);
            imgSend = itemView.findViewById(R.id.img_send);
        }
    }
}

package com.example.conversationalist;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
        String nameDate = messages.get(position).getUsername() + " - " + messages.get(position).getDate();
        holder.txtUsername.setText(nameDate);

        if (!messages.get(position).getImageContent().equals("")) {
            holder.imgSend.setVisibility(View.VISIBLE);
            if (isConnected()) {
                Glide.with(context).load(messages.get(position).getImageContent()).placeholder(R.drawable.account_image).error(R.drawable.account_image).into(holder.imgSend);
            } else {
                Glide.with(context).load(messages.get(position).getImageContent()).placeholder(R.drawable.account_image).error(R.drawable.account_image).into(holder.imgSend);
            }
        }
        else {
            holder.imgSend.setVisibility(View.GONE);
        }

        if (!messages.get(position).getFile().equals("")) {
            holder.fileImg.setVisibility(View.VISIBLE);
        }
        else {
            holder.fileImg.setVisibility(View.GONE);
        }

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

            constraintSet.clear(R.id.small_file_img, ConstraintSet.LEFT);

            constraintSet.connect(R.id.profile_cardView, ConstraintSet.RIGHT, R.id.ccLayout, ConstraintSet.RIGHT, 0);
            constraintSet.connect(R.id.txt_message_content, ConstraintSet.RIGHT, R.id.ccLayout, ConstraintSet.RIGHT, 0);
            constraintSet.connect(R.id.img_send, ConstraintSet.RIGHT, R.id.ccLayout, ConstraintSet.RIGHT, 0);
            constraintSet.connect(R.id.txt_message_content, ConstraintSet.TOP, R.id.profile_cardView, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.img_send, ConstraintSet.TOP, R.id.txt_message_content, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.txt_username_content, ConstraintSet.RIGHT, R.id.profile_cardView, ConstraintSet.LEFT, 0);

            if (!messages.get(position).getImageContent().equals("")) {
                constraintSet.connect(R.id.small_file_img, ConstraintSet.RIGHT, R.id.ccLayout, ConstraintSet.RIGHT, 0);
                constraintSet.connect(R.id.small_file_img, ConstraintSet.TOP, R.id.img_send, ConstraintSet.BOTTOM, 0);
            }
            else {
                constraintSet.connect(R.id.small_file_img, ConstraintSet.RIGHT, R.id.ccLayout, ConstraintSet.RIGHT, 0);
                constraintSet.connect(R.id.small_file_img, ConstraintSet.TOP, R.id.img_send, ConstraintSet.BOTTOM, 0);
            }

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

            constraintSet.clear(R.id.small_file_img, ConstraintSet.RIGHT);

            constraintSet.connect(R.id.profile_cardView, ConstraintSet.LEFT, R.id.ccLayout, ConstraintSet.LEFT, 0);
            constraintSet.connect(R.id.txt_message_content, ConstraintSet.LEFT, R.id.ccLayout, ConstraintSet.LEFT, 0);
            constraintSet.connect(R.id.txt_message_content, ConstraintSet.TOP, R.id.profile_cardView, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.img_send, ConstraintSet.LEFT, R.id.ccLayout, ConstraintSet.LEFT, 0);
            constraintSet.connect(R.id.img_send, ConstraintSet.TOP, R.id.txt_message_content, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.txt_username_content, ConstraintSet.LEFT, R.id.profile_cardView, ConstraintSet.RIGHT, 0);

            if (!messages.get(position).getImageContent().equals("")) {
                constraintSet.connect(R.id.small_file_img, ConstraintSet.LEFT, R.id.ccLayout, ConstraintSet.LEFT, 0);
                constraintSet.connect(R.id.small_file_img, ConstraintSet.TOP, R.id.img_send, ConstraintSet.BOTTOM, 0);
            }
            else {
                constraintSet.connect(R.id.small_file_img, ConstraintSet.LEFT, R.id.ccLayout, ConstraintSet.LEFT, 0);
                constraintSet.connect(R.id.small_file_img, ConstraintSet.TOP, R.id.img_send, ConstraintSet.BOTTOM, 0);
            }

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
        TextView fileName;
        ImageView fileImg;
        public MessageHolder(@NonNull View itemView) {
            super(itemView);

            ccll = itemView.findViewById(R.id.ccLayout);
            txtMessage = itemView.findViewById(R.id.txt_message_content);
            txtUsername = itemView.findViewById(R.id.txt_username_content);
            profImage = itemView.findViewById(R.id.small_profile_img);
            imgSend = itemView.findViewById(R.id.img_send);
            fileImg = itemView.findViewById(R.id.small_file_img);


            fileImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = fileName.getText().toString();

                    downloadFile(context, fileName.getText().toString(), "", "" , url);
                }
            });


        }
    }
    public long downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url) {


        DownloadManager downloadmanager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension);

        return downloadmanager.enqueue(request);
    }

    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }


}

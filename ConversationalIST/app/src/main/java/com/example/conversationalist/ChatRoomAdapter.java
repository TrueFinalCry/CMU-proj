package com.example.conversationalist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomHolder> {

    private ArrayList<ChatRoom> chatRooms;
    private Context context;
    private ChatRoomAdapter.OnChatRoomClickListener onChatRoomClickListener;

    public ChatRoomAdapter(ArrayList<ChatRoom> chatRooms, Context context, ChatRoomAdapter.OnChatRoomClickListener onChatRoomClickListener) {
        this.chatRooms = chatRooms;
        this.context = context;
        this.onChatRoomClickListener = onChatRoomClickListener;
    }

    interface OnChatRoomClickListener {
        void onChatRoomClicked(int position);
    }

    @NonNull
    @Override
    public ChatRoomAdapter.ChatRoomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_holder, parent, false);

        return new ChatRoomAdapter.ChatRoomHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomAdapter.ChatRoomHolder holder, int position) {
        holder.txtUsername.setText(chatRooms.get(position).getChatRoomId());
        if (chatRooms.get(position).getUnread().equals("1")) {
            holder.notificationImg.setVisibility(View.VISIBLE);
        }
        Glide.with(context).load(chatRooms.get(position).getChatImage()).error(R.drawable.account_image).placeholder(R.drawable.account_image).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    class ChatRoomHolder extends RecyclerView.ViewHolder {
        TextView txtUsername;
        ImageView imageView,notificationImg;

        public ChatRoomHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onChatRoomClickListener.onChatRoomClicked(getAdapterPosition());
                }
            });

            notificationImg = itemView.findViewById(R.id.notification);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            imageView = itemView.findViewById(R.id.img_pro);
        }
    }
}

package com.example.conversationalist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatRoomActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText edtMessageInput;
    private TextView txtChattingWith;
    private ProgressBar progressBar;
    private ImageView chatIcon,imgSend;

    private ArrayList<Message> messages;

    private MessageAdapter messageAdapter;

    String usernameOfTheRoommate, emailOfRoommate;

    String chatRoomId, myImage,myUsername, chatRoomImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        chatRoomId = getIntent().getStringExtra("room_id");
        chatRoomImage = getIntent().getStringExtra("room_image");
        myImage = getIntent().getStringExtra("my_image");
        myUsername = getIntent().getStringExtra("my_username");

        recyclerView = findViewById(R.id.recyclerMessages);
        imgSend = findViewById(R.id.imgSendMessage);
        edtMessageInput = findViewById(R.id.edtText);
        progressBar = findViewById(R.id.progressBar);
        txtChattingWith = findViewById(R.id.txtChattingWith);
        chatIcon = findViewById(R.id.small_chatroom_img);

        txtChattingWith.setText(chatRoomId);
        messages = new ArrayList<>();

        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("chatRoom").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //cycles through all chatroom
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String roomId = dataSnapshot.child("chatRoomId").getValue(String.class);
                            if (roomId.equals(chatRoomId)) {
                                Message myMessage = new Message(FirebaseAuth.getInstance().getCurrentUser().getUid(), myImage, edtMessageInput.getText().toString(), "", chatRoomId, myUsername);
                                dataSnapshot.child("messages").getRef().push().setValue(myMessage);
                                //edtMessageInput.setText("");
                                break;
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });


        messageAdapter = new MessageAdapter(messages, ChatRoomActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        /*
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1))
                    onScrolledToBottom();
            }
        });
        */
        recyclerView.setAdapter(messageAdapter);
        // maybe need toolbar
        Glide.with(ChatRoomActivity.this).load(chatRoomImage).placeholder(R.drawable.account_image).error(R.drawable.account_image).into(chatIcon);

        attachMessageListener(chatRoomId);
        //setUpChatRoom();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatroom_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settings_menu_item) {
            startActivity(new Intent(ChatRoomActivity.this,ChatRoomEditorActivity.class)
                    .putExtra("room_id", chatRoomId)
                    .putExtra("room_image", chatRoomImage)
            );
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        FirebaseDatabase.getInstance().getReference("chatRoom").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //cycles through all chatroom
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String roomId = dataSnapshot.child("chatRoomId").getValue(String.class);
                    if (chatRoomId.equals(roomId)) {
                        chatRoomImage = dataSnapshot.child("chatImage").getValue(String.class);
                        Glide.with(ChatRoomActivity.this).load(chatRoomImage).placeholder(R.drawable.account_image).error(R.drawable.account_image).into(chatIcon);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void attachMessageListener(String chatRoomId) {
        FirebaseDatabase.getInstance().getReference("chatRoom").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //cycles through all chatroom
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String roomId = dataSnapshot.child("chatRoomId").getValue(String.class);
                    if (chatRoomId.equals(roomId)) {
                        dataSnapshot.child("messages").getRef().addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                messages.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    messages.add(dataSnapshot.getValue(Message.class));
                                }
                                messageAdapter.notifyDataSetChanged();
                                recyclerView.scrollToPosition(messages.size()-1);
                                recyclerView.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    /*
    private void onScrolledToBottom() {
        if (songMainList.size() < songAllList.size()) {
            int x, y;
            if ((songAllList.size() - songMainList.size()) >= 50) {
                x = songMainList.size();
                y = x + 50;
            } else {
                x = songMainList.size();
                y = x + songAllList.size() - songMainList.size();
            }
            for (int i = x; i < y; i++) {
                songMainList.add(songAllList.get(i));
            }
            songsAdapter.notifyDataSetChanged();
        }

    }
    */
}
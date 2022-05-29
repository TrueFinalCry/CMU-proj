package com.example.conversationalist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
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
    private ImageView imgToolbar,imgSend;

    private ArrayList<Message> messages;

    private MessageAdapter messageAdapter;

    String usernameOfTheRoommate, emailOfRoommate, chatRoomId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        usernameOfTheRoommate = getIntent().getStringExtra("username_of_roommate");
        emailOfRoommate = getIntent().getStringExtra("email_of_roommate");


        recyclerView = findViewById(R.id.recyclerMessages);
        imgSend = findViewById(R.id.imgSendMessage);
        edtMessageInput = findViewById(R.id.edtText);
        progressBar = findViewById(R.id.progressBar);
        txtChattingWith = findViewById(R.id.txtChattingWith);

        txtChattingWith.setText(usernameOfTheRoommate);
        messages = new ArrayList<>();

        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("messages/"+chatRoomId).push().setValue(new Message(FirebaseAuth.getInstance().getCurrentUser().getEmail(),emailOfRoommate, edtMessageInput.getText().toString()));
                edtMessageInput.setText("");
            }
        });

        messageAdapter = new MessageAdapter(messages, getIntent().getStringExtra("my_image"), getIntent().getStringExtra("img_of_roommate"), ChatRoomActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
        // maybe need toolbar
        //Glide.with(ChatRoomActivity.this).load(getIntent().getStringExtra("img_of_roommate")).placeholder(R.drawable.account_image).error(R.drawable.account_image).into(ImageToolbar)
        setUpChatRoom();
    }

    private void setUpChatRoom() {
        FirebaseDatabase.getInstance().getReference("user/"+ FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String myUsername = snapshot.getValue(User.class).getUsername();
                //generate room ID (need to change for our implementation
                if(usernameOfTheRoommate.compareTo(myUsername) >= 0) {
                    chatRoomId = myUsername + usernameOfTheRoommate;
                } else {
                    chatRoomId =  usernameOfTheRoommate + myUsername;
                }

                attachMessageListener(chatRoomId);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void attachMessageListener(String chatRoomId) {
        FirebaseDatabase.getInstance().getReference("messages/" + chatRoomId).addValueEventListener(new ValueEventListener() {
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
package com.example.conversationalist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private User myUser;
    private ArrayList<String> users;
    private ArrayList<ChatRoom> chatRooms;
    private ChatRoomAdapter chatRoomAdapter;
    ChatRoomAdapter.OnChatRoomClickListener onChatRoomClickListener;

    private ImageView addChatRoom;
    private EditText txtChatRoom;
    private ProgressBar progressBar;
    private UsersAdapter usersAdapter;
    UsersAdapter.OnUserClickListener onUserClickListener;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        progressBar = findViewById(R.id.progressBar);
        users = new ArrayList<>();

        chatRooms = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler);
        swipeRefreshLayout = findViewById(R.id.swipeLayout);
        addChatRoom = findViewById(R.id.imgAddRoom);
        txtChatRoom = findViewById(R.id.edtText);


        FirebaseDatabase.getInstance().getReference("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //cycles through all users (maybe we need chatrooms instead)
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    users.add(dataSnapshot.getKey());
                    if (dataSnapshot.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        myUser = new User(dataSnapshot.child("username").getValue(String.class), dataSnapshot.child("email").getValue(String.class), dataSnapshot.child("profilePicture").getValue(String.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //getUsers();
                getChatRooms();
                swipeRefreshLayout.setRefreshing(false);
            }


        });

        onChatRoomClickListener = new ChatRoomAdapter.OnChatRoomClickListener() {
            @Override
            public void onChatRoomClicked(int position) {
                startActivity(new Intent(FriendsActivity.this, ChatRoomActivity.class)
                        // arguments of chatroom (1 user so far)
                        .putExtra("room_id", chatRooms.get(position).getChatRoomId())
                        .putExtra("room_image", chatRooms.get(position).getChatImage())
                        .putExtra("my_image", myUser.getProfilePicture())
                        .putExtra("my_username", myUser.getUsername())

                );
                Toast.makeText(FriendsActivity.this, "Selected chatroom "+ chatRooms.get(position).getChatRoomId(), Toast.LENGTH_SHORT).show();
            }
        };




        addChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FriendsActivity.this,ChatRoomCreatorActivity.class)
                        .putExtra("my_image", myUser.getProfilePicture())
                        .putExtra("my_username", myUser.getUsername())
                );
                //txtChatRoom.setText("");
            }
        });

        getChatRooms();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_profile) {
            startActivity(new Intent(FriendsActivity.this,Profile.class)
                    .putExtra("my_image", myUser.getProfilePicture())
                    .putExtra("my_username", myUser.getUsername())
            );
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        FirebaseDatabase.getInstance().getReference("user/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //cycles through all chatroom
                myUser.setProfilePicture(snapshot.child("profilePicture").getValue().toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        getChatRooms();
    }

    private void getChatRooms() {
        chatRooms.clear();
        FirebaseDatabase.getInstance().getReference("chatRoom").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //cycles through all users (maybe we need chatrooms instead)
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {



                    ArrayList<String> userList = new ArrayList<>();
                    ArrayList<Message> messageList = new ArrayList<>();

                    boolean in = false;

                    for (DataSnapshot ds : dataSnapshot.child("users").getChildren()) {
                        if (ds.getValue(String.class).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            in = true;
                        }
                        userList.add(ds.getValue(String.class));
                    }
                    if (!in) {
                        continue;
                    }
                    for (DataSnapshot ds : dataSnapshot.child("messages").getChildren()) {
                        messageList.add(ds.getValue(Message.class));
                    }

                    ChatRoom chatRoom = new ChatRoom(
                            dataSnapshot.child("type").getValue(String.class),
                            dataSnapshot.child("chatRoomId").getValue(String.class),
                            userList,
                            messageList,
                            dataSnapshot.child("chatImage").getValue(String.class)
                    );

                    chatRooms.add(chatRoom);

                }
                chatRoomAdapter = new ChatRoomAdapter(chatRooms, FriendsActivity.this, onChatRoomClickListener);
                recyclerView.setLayoutManager(new LinearLayoutManager(FriendsActivity.this));
                recyclerView.setAdapter(chatRoomAdapter);
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                addChatRoom.setVisibility(View.VISIBLE);
                txtChatRoom.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
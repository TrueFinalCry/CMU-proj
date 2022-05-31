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
    private ArrayList<User> users;
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
                    users.add(dataSnapshot.getValue(User.class));
                }

                for(User user : users) {
                    if(user.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                        myUser = new User(user.getUsername(), user.getEmail(), user.getProfilePicture());
                        // maybe remove myself from list
                        return;
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
                FirebaseDatabase.getInstance().getReference("chatRoom").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //cycles through all users (maybe we need chatrooms instead)
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String roomId = dataSnapshot.child("chatRoomId").getValue(String.class);
                            if (txtChatRoom.getText().toString().equals(roomId)) {
                                for(DataSnapshot ds : dataSnapshot.child("users").getChildren()) {
                                    String email = ds.child("email").getValue(String.class);
                                    if(myUser.getEmail().equals(email)) {
                                        Toast.makeText(FriendsActivity.this, "Already Registered in this ChatRoom", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                Toast.makeText(FriendsActivity.this, "Successfully Registered in this ChatRoom", Toast.LENGTH_SHORT).show();
                                dataSnapshot.child("users").getRef().push().setValue(myUser);
                                return;
                            }
                        }

                        ChatRoom chatRoom = new ChatRoom(
                                "public",
                                txtChatRoom.getText().toString(),
                                new ArrayList<User>(),
                                new ArrayList<Message>(),
                                ""
                        );
                        FirebaseDatabase.getInstance().getReference("chatRoom").push().setValue(chatRoom);

                        FirebaseDatabase.getInstance().getReference("chatRoom").addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                           public void onDataChange(@NonNull DataSnapshot snapshot) {
                               //cycles through all users (maybe we need chatrooms instead)
                               for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                   String roomId = dataSnapshot.child("chatRoomId").getValue(String.class);
                                   if (txtChatRoom.getText().toString().equals(roomId)) {
                                       dataSnapshot.child("users").getRef().push().setValue(myUser);
                                       //FirebaseDatabase.getInstance().getReference("chatRoom/"+txtChatRoom.getText().toString()+"/users").push().setValue(myUser);
                                   }
                               }
                           }
                           @Override
                           public void onCancelled (@NonNull DatabaseError error){

                           }
                        });
                        Toast.makeText(FriendsActivity.this, "Successfully created ChatRoom", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
                    ArrayList<User> userList = new ArrayList<>();
                    ArrayList<Message> messageList = new ArrayList<>();

                    for (DataSnapshot ds : dataSnapshot.child("users").getChildren()) {
                        userList.add(ds.getValue(User.class));
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

                    for (User user : chatRoom.getUsers()) {
                        if(user.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                            chatRooms.add(chatRoom);
                            break;
                        }
                    }
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
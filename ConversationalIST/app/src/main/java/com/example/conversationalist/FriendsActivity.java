package com.example.conversationalist;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

    private String appLinkAction;
    private Uri appLinkData;

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
                if (isConnected())
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
                        .putExtra("chat_room_uid", chatRooms.get(position).getUid())

                );
                Toast.makeText(FriendsActivity.this, "Selected chatroom " + chatRooms.get(position).getChatRoomId(), Toast.LENGTH_SHORT).show();
            }
        };


        addChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FriendsActivity.this, ChatRoomCreatorActivity.class)
                        .putExtra("my_image", myUser.getProfilePicture())
                        .putExtra("my_username", myUser.getUsername())
                );
                //txtChatRoom.setText("");
            }
        });

        handleIntent(getIntent());
        if (isConnected())
            getChatRooms();
        // ATTENTION: This was auto-generated to handle app links.

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
        if (isConnected()) {
            FirebaseDatabase.getInstance().getReference("user/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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
    }

    private void getChatRooms() {
        chatRooms.clear();
        FirebaseDatabase.getInstance().getReference("user/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/chatrooms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //cycles through all users (maybe we need chatrooms instead)
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ArrayList<String> userList = new ArrayList<>();
                    ArrayList<Message> messageList = new ArrayList<>();



                    ChatRoom chatRoom = new ChatRoom(
                            dataSnapshot.child("type").getValue(String.class),
                            dataSnapshot.child("chatRoomId").getValue(String.class),
                            userList,
                            messageList,
                            dataSnapshot.child("chatImage").getValue(String.class),
                            dataSnapshot.child("uid").getValue(String.class)
                    );

                    chatRooms.add(chatRoom);

                }
                chatRoomAdapter = new ChatRoomAdapter(chatRooms, FriendsActivity.this, onChatRoomClickListener);
                recyclerView.setLayoutManager(new LinearLayoutManager(FriendsActivity.this));
                recyclerView.setAdapter(chatRoomAdapter);
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                addChatRoom.setVisibility(View.VISIBLE);
                txtChatRoom.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null){
            String chatRoomUid = appLinkData.getLastPathSegment();
            FirebaseDatabase.getInstance().getReference("chatRoom/" +chatRoomUid ).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //cycles through all users (maybe we need chatrooms instead
                    ChatRoom chatRoom = new ChatRoom(
                            snapshot.child("type").getValue(String.class),
                            snapshot.child("chatRoomId").getValue(String.class),
                            new ArrayList<String>(),
                            new ArrayList<Message>(),
                            "",
                            chatRoomUid);
                    for (DataSnapshot ds : snapshot.child("users").getChildren()) {
                        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(ds.getValue(String.class))) {
                            Toast.makeText(FriendsActivity.this, "Already Registered in this ChatRoom", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    Toast.makeText(FriendsActivity.this, "Successfully Registered in this ChatRoom", Toast.LENGTH_SHORT).show();
                    FirebaseDatabase.getInstance().getReference("user/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chatrooms").push().setValue(chatRoom);
                    snapshot.child("users").getRef().push().setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }
}
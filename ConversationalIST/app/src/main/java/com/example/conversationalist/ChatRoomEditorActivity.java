package com.example.conversationalist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;


public class ChatRoomEditorActivity extends AppCompatActivity {

    private TextView chatRoomName, viewUsername;
    private ImageView chatImage;
    private Uri imagePath;
    private Button btnLeaveChatRoom, btnAdd, btnRemove;
    private EditText txtUsername;
    private String userUid;
    private DatabaseReference userChatRoomRef;

    private String chatRoomId, chatRoomImage, chatRoomUid,chatRoomType,chatRoomRad, chatRoomLong,chatRoomlat;
    private Reference chatRoomReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_chat_editor);



        chatRoomName = findViewById(R.id.chatRoomName);
        btnLeaveChatRoom = findViewById(R.id.btnLeaveChatRoom);
        btnAdd = findViewById(R.id.btnAdd);
        btnRemove = findViewById(R.id.btnRemove);
        txtUsername = findViewById(R.id.txtUsername);

        chatRoomId = getIntent().getStringExtra("room_id");
        chatRoomImage = getIntent().getStringExtra("room_image");
        chatRoomUid = getIntent().getStringExtra("room_uid");
        chatRoomType = getIntent().getStringExtra("type");
        chatRoomRad = getIntent().getStringExtra("rad");
        chatRoomLong = getIntent().getStringExtra("long");
        chatRoomlat = getIntent().getStringExtra("lat");
        chatRoomName.setText(chatRoomId);

        if (!chatRoomType.equals("private")) {
            btnRemove.setVisibility(View.GONE);
            btnAdd.setVisibility(View.GONE);
            txtUsername.setVisibility(View.GONE);
            viewUsername.setVisibility(View.GONE);
        }

        btnLeaveChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("chatRoom/" + chatRoomUid + "/users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if(dataSnapshot.getValue(String.class).equals(FirebaseAuth.getInstance().getUid())) {
                                dataSnapshot.getRef().removeValue();
                                break;
                            }
                        }
                        FirebaseDatabase.getInstance().getReference("user/" + FirebaseAuth.getInstance().getUid() + "/chatrooms").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    if(dataSnapshot.child("uid").getValue(String.class).equals(chatRoomUid)) {
                                        dataSnapshot.getRef().removeValue();
                                        Toast.makeText(ChatRoomEditorActivity.this, "Successfully left ChatRoom", Toast.LENGTH_SHORT).show();
                                        Intent returnBtn = new Intent(getApplicationContext(), FriendsActivity.class);
                                        returnBtn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(returnBtn);
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                //txtChatRoom.setText("");
            }
        });

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("user").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if(Objects.equals(dataSnapshot.child("username").getValue(String.class), txtUsername.getText().toString())) {
                                userUid = dataSnapshot.getKey();
                                if(userUid.equals(FirebaseAuth.getInstance().getUid())) {
                                    Toast.makeText(ChatRoomEditorActivity.this, String.format(ChatRoomEditorActivity.this.getResources().getString(R.string.Use_Leave_Button)), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                FirebaseDatabase.getInstance().getReference("chatRoom/" + chatRoomUid + "/users").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            if(dataSnapshot.getValue(String.class).equals(userUid)) {
                                                dataSnapshot.getRef().removeValue();
                                                break;
                                            }
                                        }
                                        FirebaseDatabase.getInstance().getReference("user/" + userUid + "/chatrooms").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                    if(dataSnapshot.child("uid").getValue(String.class).equals(chatRoomUid)) {
                                                        dataSnapshot.getRef().removeValue();
                                                        Toast.makeText(ChatRoomEditorActivity.this, String.format(ChatRoomEditorActivity.this.getResources().getString(R.string.Successfully_removed_user)), Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
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
                //txtChatRoom.setText("");
            }
        });


        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("user").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if(Objects.equals(dataSnapshot.child("username").getValue(String.class), txtUsername.getText().toString())) {
                                userUid = dataSnapshot.getKey();
                                for (DataSnapshot ds : dataSnapshot.child("chatrooms").getChildren()) {
                                    if (chatRoomUid.equals(ds.child("uid").getValue(String.class))) {
                                        Toast.makeText(ChatRoomEditorActivity.this, String.format(ChatRoomEditorActivity.this.getResources().getString(R.string.User_Already_Registered)), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                ChatRoom chatRoom = new ChatRoom(
                                        chatRoomType,
                                        chatRoomId,
                                        new ArrayList<String>(),
                                        new ArrayList<Message>(),
                                        "",
                                        chatRoomUid,
                                        chatRoomlat,
                                        chatRoomLong,
                                        chatRoomRad,
                                        "0");
                                FirebaseDatabase.getInstance().getReference("user/" + userUid + "/chatrooms/" + chatRoom.getUid()).setValue(chatRoom);
                                FirebaseDatabase.getInstance().getReference("chatRoom/" + chatRoomUid + "/users").push().setValue(userUid);
                                Toast.makeText(ChatRoomEditorActivity.this, String.format(ChatRoomEditorActivity.this.getResources().getString(R.string.Successfully_added_user)), Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                //txtChatRoom.setText("");
            }
        });
    }

}

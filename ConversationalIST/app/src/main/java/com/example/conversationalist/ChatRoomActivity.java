package com.example.conversationalist;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class ChatRoomActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText edtMessageInput;
    private TextView txtChattingWith;
    private ProgressBar progressBar;
    private ImageView chatIcon,imgSend,imgSendPhotos;
    private Uri imagePath;
    private String ImageToSend;

    private ArrayList<Message> messages;

    private MessageAdapter messageAdapter;

    String usernameOfTheRoommate, emailOfRoommate;

    String chatRoomId, myImage,myUsername, chatRoomImage, chatRoomUid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        chatRoomId = getIntent().getStringExtra("room_id");
        chatRoomImage = getIntent().getStringExtra("room_image");
        myImage = getIntent().getStringExtra("my_image");
        myUsername = getIntent().getStringExtra("my_username");
        chatRoomUid = getIntent().getStringExtra("chat_room_uid");

        recyclerView = findViewById(R.id.recyclerMessages);
        imgSend = findViewById(R.id.imgSendMessage);
        edtMessageInput = findViewById(R.id.edtText);
        progressBar = findViewById(R.id.progressBar);
        txtChattingWith = findViewById(R.id.txtChattingWith);
        chatIcon = findViewById(R.id.small_chatroom_img);
        imgSendPhotos = findViewById(R.id.imgSendPhotos);

        txtChattingWith.setText(chatRoomId);
        messages = new ArrayList<>();
        ImageToSend = "";


        imgSendPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent photoIntent = new Intent(Intent.ACTION_PICK);
                //photoIntent.setAction(Intent.ACTION_GET_CONTENT);
                photoIntent.setType("image/*");
                LaunchImageChooserActivity.launch(photoIntent);
            }

            ActivityResultLauncher<Intent> LaunchImageChooserActivity = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                imagePath = data.getData();
                                getImageInImageView();
                            }
                        }
                    });

            private void getImageInImageView() {
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imagePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("chatRoom/" + chatRoomUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //cycles through all chatroom
                        if (!imagePath.equals(""))
                            uploadImage();
                        Message myMessage = new Message(FirebaseAuth.getInstance().getCurrentUser().getUid(), myImage, edtMessageInput.getText().toString(), ImageToSend, chatRoomId, myUsername);
                        snapshot.child("messages").getRef().push().setValue(myMessage);
                        //edtMessageInput.setText("");
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

    private void uploadImage() {

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        if (imagePath != null) {
            FirebaseStorage.getInstance().getReference("images/"+ UUID.randomUUID().toString()).putFile(imagePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if(task.isSuccessful()) {
                                    ImageToSend = task.getResult().toString();
                                }
                            }
                        });
                        Toast.makeText(ChatRoomActivity.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChatRoomActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = 100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount();
                    progressDialog.setMessage(" Uploaded "+(int) progress + "%");
                }
            });
        } else {
            progressDialog.dismiss();
        }
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
package com.example.conversationalist;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import java.util.Calendar;
import java.util.UUID;

public class ChatRoomActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText edtMessageInput;
    private TextView txtChattingWith;
    private ProgressBar progressBar;
    private ImageView chatIcon,imgSend,imgSendPhotos, shareRoomLink,shareFile;
    private Uri imagePath;
    private Uri filePath;
    private String ImageToSend, fileToSend;

    private ArrayList<Message> messages;

    private MessageAdapter messageAdapter;

    String usernameOfTheRoommate, emailOfRoommate;

    String chatRoomId, myImage,myUsername, chatRoomImage, chatRoomUid, chatRoomType,chatRoomRad, chatRoomLong,chatRoomlat;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        chatRoomId = getIntent().getStringExtra("room_id");
        chatRoomImage = getIntent().getStringExtra("room_image");
        myImage = getIntent().getStringExtra("my_image");
        myUsername = getIntent().getStringExtra("my_username");
        chatRoomUid = getIntent().getStringExtra("chat_room_uid");

        chatRoomType = getIntent().getStringExtra("type");
        chatRoomRad = getIntent().getStringExtra("rad");
        chatRoomLong = getIntent().getStringExtra("long");
        chatRoomlat = getIntent().getStringExtra("lat");

        shareFile = findViewById(R.id.share_file);
        shareRoomLink = findViewById(R.id.share_link);
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

        shareRoomLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String linkInfo = "http://conversationalist-3003c-default-rtdb.firebaseio.com/chatRoom/" + chatRoomUid;
                String toSend = edtMessageInput.getText().toString() + linkInfo;
                edtMessageInput.setText(toSend);
            }
        });

        shareFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");

                FileActivityResultLauncher.launch(intent);
            }

            ActivityResultLauncher<Intent> FileActivityResultLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                // Here, no request code
                                Intent data = result.getData();
                                if (data != null && data.getData() != null) {
                                    filePath = data.getData();
                                }
                            }
                        }
                    });
        });

        imgSendPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent photoIntent = new Intent(Intent.ACTION_PICK);
                photoIntent.setAction(Intent.ACTION_GET_CONTENT);
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
                        sendMessage(snapshot);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });


        messageAdapter = new MessageAdapter(messages, ChatRoomActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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
                    .putExtra("room_uid", chatRoomUid)
                    .putExtra("type", chatRoomType)
                    .putExtra("rad",  chatRoomRad)
                    .putExtra("long",  chatRoomLong)
                    .putExtra("lat",  chatRoomlat)
            );
        }
        return super.onOptionsItemSelected(item);
    }

    private void attachMessageListener(String chatRoomId) {
        if (isConnected()) {
            FirebaseDatabase.getInstance().getReference("chatRoom/" + chatRoomUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //cycles through all chatroom
                    snapshot.child("messages").getRef().addValueEventListener(new ValueEventListener() {
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

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }


    private void sendMessage(DataSnapshot snapshot) {

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
                                    if (filePath != null) {
                                        FirebaseStorage.getInstance().getReference("files/" + UUID.randomUUID().toString()).putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Uri> task) {
                                                            if (task.isSuccessful()) {
                                                                fileToSend = task.getResult().toString();
                                                                Message myMessage = new Message(FirebaseAuth.getInstance().getCurrentUser().getUid(), myImage, edtMessageInput.getText().toString(), ImageToSend, chatRoomId, myUsername, fileToSend, Calendar.getInstance().getTime().toString());
                                                                snapshot.child("messages").getRef().push().setValue(myMessage);
                                                                filePath = null;
                                                                imagePath = null;
                                                                edtMessageInput.setText("");
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
                                                progressDialog.setMessage(" Uploaded " + (int) progress + "%");
                                            }
                                        });
                                    }
                                    else {
                                        Message myMessage = new Message(FirebaseAuth.getInstance().getCurrentUser().getUid(), myImage, edtMessageInput.getText().toString(), ImageToSend, chatRoomId, myUsername, "", Calendar.getInstance().getTime().toString());
                                        snapshot.child("messages").getRef().push().setValue(myMessage);
                                        imagePath = null;
                                        edtMessageInput.setText("");
                                    }
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
            if (filePath != null) {
                FirebaseStorage.getInstance().getReference("files/" + UUID.randomUUID().toString()).putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        String fileToSend = task.getResult().toString();
                                        Message myMessage = new Message(FirebaseAuth.getInstance().getCurrentUser().getUid(), myImage, edtMessageInput.getText().toString(), "", chatRoomId, myUsername, fileToSend, Calendar.getInstance().getTime().toString());
                                        snapshot.child("messages").getRef().push().setValue(myMessage);
                                        filePath = null;
                                        edtMessageInput.setText("");
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
                        progressDialog.setMessage(" Uploaded " + (int) progress + "%");
                    }
                });
            }
            else {
                progressDialog.dismiss();
                Message myMessage = new Message(FirebaseAuth.getInstance().getCurrentUser().getUid(), myImage, edtMessageInput.getText().toString(), "", chatRoomId, myUsername, "", Calendar.getInstance().getTime().toString());
                snapshot.child("messages").getRef().push().setValue(myMessage);
                edtMessageInput.setText("");
            }
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
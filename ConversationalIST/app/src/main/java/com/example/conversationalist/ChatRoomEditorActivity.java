package com.example.conversationalist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.UUID;


public class ChatRoomEditorActivity extends AppCompatActivity {

    private TextView chatRoomName;
    private Button updateAllInfo;
    private EditText editTextUsername;
    private ImageView chatImage;
    private Uri imagePath;

    private String chatRoomId, chatRoomImage;
    private Reference chatRoomReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_editor);

        chatRoomName = findViewById(R.id.chatRoomName);
        updateAllInfo = findViewById(R.id.updateAllInfo);
        editTextUsername = findViewById(R.id.editTextUsername);
        chatImage = findViewById(R.id.chatImage);

        chatRoomId = getIntent().getStringExtra("room_id");
        chatRoomImage = getIntent().getStringExtra("room_image");

        chatRoomName.setText(chatRoomId);


        Glide.with(ChatRoomEditorActivity.this).load(chatRoomImage).placeholder(R.drawable.account_image).error(R.drawable.account_image).into(chatImage);

        chatImage.setOnClickListener(new View.OnClickListener() {
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
                Glide.with(ChatRoomEditorActivity.this).load(imagePath).placeholder(R.drawable.account_image).error(R.drawable.account_image).into(chatImage);
            }
        });

        updateAllInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });
    }

    private void upload() {
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
                                    update(task.getResult().toString(), editTextUsername.getText().toString());
                                }
                            }
                        });
                        Toast.makeText(ChatRoomEditorActivity.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChatRoomEditorActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
            update(chatRoomImage, editTextUsername.getText().toString());
            progressDialog.dismiss();
        }
    }

    private void update(String url, String username) {
        chatRoomImage = url;
        FirebaseDatabase.getInstance().getReference("chatRoom").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //cycles through all users (maybe we need chatrooms instead)
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String roomId = dataSnapshot.child("chatRoomId").getValue(String.class);
                    if (chatRoomId.equals(roomId)) {
                        dataSnapshot.child("chatImage").getRef().setValue(url);
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
}

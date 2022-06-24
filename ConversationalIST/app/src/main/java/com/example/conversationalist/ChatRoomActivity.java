package com.example.conversationalist;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.location.Location;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.xml.transform.Result;

public class ChatRoomActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText edtMessageInput;
    private TextView txtChattingWith;
    private ProgressBar progressBar;
    private ImageView chatIcon,imgSend,imgSendPhotos, shareRoomLink,shareFile, shareLocation;
    private Uri imagePath;
    private Uri filePath;
    private Uri imageUri;
    private String ImageToSend, fileToSend;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String latitude, longitude;

    private ArrayList<Message> messages;
    private FusedLocationProviderClient fusedLocationClient;

    private MessageAdapter messageAdapter;

    String usernameOfTheRoommate, emailOfRoommate;

    String chatRoomId, myImage,myUsername, chatRoomImage, chatRoomUid, chatRoomType,chatRoomRad, chatRoomLong,chatRoomLat;



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
        chatRoomLat = getIntent().getStringExtra("lat");

        shareFile = findViewById(R.id.share_file);
        shareRoomLink = findViewById(R.id.share_link);
        recyclerView = findViewById(R.id.recyclerMessages);
        imgSend = findViewById(R.id.imgSendMessage);
        edtMessageInput = findViewById(R.id.edtText);
        progressBar = findViewById(R.id.progressBar);
        txtChattingWith = findViewById(R.id.txtChattingWith);
        chatIcon = findViewById(R.id.small_chatroom_img);
        imgSendPhotos = findViewById(R.id.imgSendPhotos);
        shareLocation = findViewById(R.id.shareLocation);

        txtChattingWith.setText(chatRoomId);
        messages = new ArrayList<>();
        ImageToSend = "";
        latitude = "";
        longitude = "";

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        shareRoomLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String linkInfo = "http://conversationalist-3003c-default-rtdb.firebaseio.com/chatRoom/" + chatRoomUid;
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, linkInfo);
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
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


                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile(ChatRoomActivity.this);

                    } catch (IOException ex) {
                    }

                    if (photoFile != null) {
                        imagePath = FileProvider.getUriForFile(ChatRoomActivity.this, getApplicationContext().getPackageName() + ".provider",
                                photoFile);
                        deleteFile(photoFile.getName());
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imagePath);
                        LaunchImageChooserActivity.launch(takePictureIntent);
                        photoFile.deleteOnExit();
                    }
                }

                /*
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);

                try {
                    LaunchChooserActivity.launch(gallery);
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                }

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


                try {
                    LaunchImageChooserActivity.launch(takePictureIntent);
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                }
                */


            }

            ActivityResultLauncher<Intent> LaunchImageChooserActivity = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                imagePath = data.getData();
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
                        if (chatRoomType.equals("geo-fenced")) {
                            if (!(ActivityCompat.checkSelfPermission(ChatRoomActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ChatRoomActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                                fusedLocationClient.getLastLocation()
                                        .addOnSuccessListener(ChatRoomActivity.this, new OnSuccessListener<Location>() {
                                            @Override
                                            public void onSuccess(Location location) {
                                                // Got last known location. In some rare situations this can be null.
                                                if (location != null) {
                                                    double longitude = location.getLongitude();
                                                    double latitude = location.getLatitude();
                                                    if (Double.parseDouble(chatRoomRad) > Math.sqrt(((Double.parseDouble(chatRoomLong)) - longitude) * ((Double.parseDouble(chatRoomLong) - longitude) + ((Double.parseDouble(chatRoomLat) - latitude) * ((Double.parseDouble(chatRoomLat) - latitude)))))) {
                                                        sendMessage(snapshot);
                                                    } else {
                                                        Toast.makeText(ChatRoomActivity.this, "Locations permission denied", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(ChatRoomActivity.this, "Locations permission denied", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            sendMessage(snapshot);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

        shareLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(ActivityCompat.checkSelfPermission(ChatRoomActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ChatRoomActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(ChatRoomActivity.this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        longitude = ""+location.getLongitude();
                                        latitude =  ""+location.getLatitude();
                                        Message myMessage = new Message(FirebaseAuth.getInstance().getCurrentUser().getUid(), myImage, "https://www.google.com/maps/@" + latitude + "," + longitude + ",15z", "", chatRoomId, myUsername, "", Calendar.getInstance().getTime().toString().substring(0,Calendar.getInstance().getTime().toString().length()-4), latitude, longitude);
                                        FirebaseDatabase.getInstance().getReference("chatRoom/" + chatRoomUid).child("messages").push().setValue(myMessage);
                                        sendNotification();
                                        longitude = "";
                                        latitude =  "";
                                    }
                                }
                            });
                }
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
                    .putExtra("lat",  chatRoomLat)
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
                            FirebaseDatabase.getInstance().getReference("user/" + FirebaseAuth.getInstance().getUid() + "/chatrooms/" + chatRoomUid + "/unread").setValue("0");

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
                                                                Message myMessage = new Message(FirebaseAuth.getInstance().getCurrentUser().getUid(), myImage, edtMessageInput.getText().toString(), ImageToSend, chatRoomId, myUsername, fileToSend, Calendar.getInstance().getTime().toString().substring(0,Calendar.getInstance().getTime().toString().length()-4),latitude, longitude);
                                                                snapshot.child("messages").getRef().push().setValue(myMessage);
                                                                sendNotification();
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
                                        Message myMessage = new Message(FirebaseAuth.getInstance().getCurrentUser().getUid(), myImage, edtMessageInput.getText().toString(), ImageToSend, chatRoomId, myUsername, "", Calendar.getInstance().getTime().toString().substring(0,Calendar.getInstance().getTime().toString().length()-4), latitude, longitude);
                                        snapshot.child("messages").getRef().push().setValue(myMessage);
                                        sendNotification();
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
                                        Message myMessage = new Message(FirebaseAuth.getInstance().getCurrentUser().getUid(), myImage, edtMessageInput.getText().toString(), "", chatRoomId, myUsername, fileToSend, Calendar.getInstance().getTime().toString().substring(0,Calendar.getInstance().getTime().toString().length()-4), latitude, longitude);
                                        snapshot.child("messages").getRef().push().setValue(myMessage);
                                        sendNotification();
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
                Message myMessage = new Message(FirebaseAuth.getInstance().getCurrentUser().getUid(), myImage, edtMessageInput.getText().toString(), "", chatRoomId, myUsername, "", Calendar.getInstance().getTime().toString().substring(0,Calendar.getInstance().getTime().toString().length()-4), latitude, longitude);
                snapshot.child("messages").getRef().push().setValue(myMessage);
                sendNotification();
                edtMessageInput.setText("");
            }
        }
    }

    private void sendNotification() {
        FirebaseDatabase.getInstance().getReference("chatRoom/" + chatRoomUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //cycles through all users

                for (DataSnapshot ds : snapshot.child("users").getChildren()) {
                    String userUid = ds.getValue(String.class);
                    if (userUid.equals(FirebaseAuth.getInstance().getUid())) {
                        continue;
                    }
                    FirebaseDatabase.getInstance().getReference("user/" + userUid + "/chatrooms/" + chatRoomUid + "/unread").setValue("1");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String imageFileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ;
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir     /* directory */
        );
        return image;
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
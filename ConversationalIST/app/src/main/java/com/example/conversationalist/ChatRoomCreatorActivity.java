package com.example.conversationalist;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatRoomCreatorActivity extends AppCompatActivity {
    private Button btnPublic, btnPrivate, btnGeo, btnCreate, btnJoin;
    private EditText txtChatRoomName, txtLocationLat,txtLocationLong, txtRadius;
    private String location;
    private String type;
    private LocationRequest locationRequest;
    private double longitude, latitude, rad;
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat_creator);

        txtChatRoomName = findViewById(R.id.chatRoomName);
        txtLocationLat = findViewById(R.id.txtLocationLat);
        txtLocationLong = findViewById(R.id.txtLocationLong);

        txtRadius = findViewById(R.id.txtRadius);
        btnPublic = findViewById(R.id.publicButton);
        btnPrivate = findViewById(R.id.privateButton);
        btnGeo = findViewById(R.id.geoButton);
        btnCreate = findViewById(R.id.createButton);
        btnJoin = findViewById(R.id.joinButton);

        txtLocationLong.setVisibility(View.GONE);
        txtLocationLat.setVisibility(View.GONE);
        txtRadius.setVisibility(View.GONE);

        location = "";

        type = "public";


        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("chatRoom").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //cycles through all users (maybe we need chatrooms instead)
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String roomId = dataSnapshot.child("chatRoomId").getValue(String.class);
                            if (txtChatRoomName.getText().toString().equals(roomId)) {
                                ChatRoom chatRoom = new ChatRoom(
                                        type,
                                        txtChatRoomName.getText().toString(),
                                        new ArrayList<String>(),
                                        new ArrayList<Message>(),
                                        "",
                                        dataSnapshot.getKey(),
                                        dataSnapshot.child("latitude").getValue(String.class),
                                        dataSnapshot.child("longitude").getValue(String.class),
                                        dataSnapshot.child("rad").getValue(String.class));

                                if (dataSnapshot.child("type").getValue(String.class).equals("public")) {
                                    for (DataSnapshot ds : dataSnapshot.child("users").getChildren()) {
                                        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(ds.getValue(String.class))) {
                                            Toast.makeText(ChatRoomCreatorActivity.this, "Already Registered in this ChatRoom", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }
                                    Toast.makeText(ChatRoomCreatorActivity.this, "Successfully Registered in this ChatRoom", Toast.LENGTH_SHORT).show();
                                    FirebaseDatabase.getInstance().getReference("user/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chatrooms").push().setValue(chatRoom);
                                    dataSnapshot.child("users").getRef().push().setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    return;
                                } else if (dataSnapshot.child("type").getValue(String.class).equals("private")) {
                                    Toast.makeText(ChatRoomCreatorActivity.this, "This ChatRoom is private", Toast.LENGTH_SHORT).show();
                                    return;
                                } else if (dataSnapshot.child("type").getValue(String.class).equals("geo-fenced")) {
                                    // check localization

                                    // Toast.makeText(ChatRoomCreatorActivity.this, "ChatRoom to far away", Toast.LENGTH_SHORT).show();
                                    for (DataSnapshot ds : dataSnapshot.child("users").getChildren()) {
                                        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(ds.getValue(String.class))) {
                                            Toast.makeText(ChatRoomCreatorActivity.this, "Already Registered in this ChatRoom", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }
                                    Toast.makeText(ChatRoomCreatorActivity.this, "Successfully Registered in this ChatRoom", Toast.LENGTH_SHORT).show();
                                    FirebaseDatabase.getInstance().getReference("user/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chatrooms").push().setValue(chatRoom);
                                    dataSnapshot.child("users").getRef().push().setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    return;
                                }
                                return;
                            }

                        }
                        Toast.makeText(ChatRoomCreatorActivity.this, "ChatRoom doesn't exist", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("chatRoom").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String key = "";
                        //cycles through all users (maybe we need chatrooms instead)
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String roomId = dataSnapshot.child("chatRoomId").getValue(String.class);
                            if (txtChatRoomName.getText().toString().equals(roomId)) {
                                Toast.makeText(ChatRoomCreatorActivity.this, "ChatRoom already exists", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        ChatRoom chatRoom = new ChatRoom(
                                type,
                                txtChatRoomName.getText().toString(),
                                new ArrayList<String>(),
                                new ArrayList<Message>(),
                                "",
                                "",
                                txtLocationLat.getText().toString(),
                                txtLocationLong.getText().toString(),
                                txtRadius.getText().toString());
                        FirebaseDatabase.getInstance().getReference("chatRoom").push().setValue(chatRoom);

                        FirebaseDatabase.getInstance().getReference("chatRoom").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                //cycles through all users (maybe we need chatrooms instead)
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    String roomId = dataSnapshot.child("chatRoomId").getValue(String.class);
                                    if (txtChatRoomName.getText().toString().equals(roomId)) {
                                        dataSnapshot.child("uid").getRef().setValue(dataSnapshot.getRef().getKey());
                                        dataSnapshot.child("users").getRef().push().setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        //Log.d("TAGDAGWDGWDA","AA");
                                        ChatRoom chatRoom = new ChatRoom(
                                                type,
                                                txtChatRoomName.getText().toString(),
                                                new ArrayList<String>(),
                                                new ArrayList<Message>(),
                                                "",
                                                dataSnapshot.getRef().getKey(),
                                                txtLocationLat.getText().toString(),
                                                txtLocationLong.getText().toString(),
                                                txtRadius.getText().toString());
                                        FirebaseDatabase.getInstance().getReference("chatRoom/" + dataSnapshot.getRef().getKey() + "/uid").setValue(dataSnapshot.getRef().getKey());
                                        FirebaseDatabase.getInstance().getReference("user/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chatrooms").push().setValue(chatRoom);
                                        //FirebaseDatabase.getInstance().getReference("chatRoom/"+txtChatRoom.getText().toString()+"/users").push().setValue(myUser);
                                        return;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        Toast.makeText(ChatRoomCreatorActivity.this, "Successfully created ChatRoom", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                //txtChatRoom.setText("");
            }
        });

        btnPrivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "private";

                txtLocationLat.setVisibility(View.GONE);
                txtLocationLong.setVisibility(View.GONE);
                txtRadius.setVisibility(View.GONE);
            }
        });
        btnPublic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "public";

                txtLocationLat.setVisibility(View.GONE);
                txtLocationLong.setVisibility(View.GONE);
                txtRadius.setVisibility(View.GONE);

            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnGeo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "geo-fenced";

                txtLocationLat.setVisibility(View.VISIBLE);
                txtLocationLong.setVisibility(View.VISIBLE);
                txtRadius.setVisibility(View.VISIBLE);

                if (!(ActivityCompat.checkSelfPermission(ChatRoomCreatorActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ChatRoomCreatorActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(ChatRoomCreatorActivity.this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        longitude = location.getLongitude();
                                        latitude =  location.getLatitude();

                                        String locationStringLat = ""+latitude;
                                        String locationStringLong = ""+longitude;
                                        txtLocationLat.setText(locationStringLat);
                                        txtLocationLong.setText(locationStringLong);

                                    }
                                }
                            });
                }


                //getCurrentLocation();


            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

                if (isGPSEnabled()) {

                    getCurrentLocation();

                }else {

                    turnOnGPS();
                }
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {

                getCurrentLocation();
            }
        }
    }

    private void getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(ChatRoomCreatorActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    Log.d("TAG1", "A");
                    LocationServices.getFusedLocationProviderClient(ChatRoomCreatorActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(ChatRoomCreatorActivity.this)
                                            .removeLocationUpdates(this);
                                    Log.d("TAG1", "A");
                                    if (locationResult != null && locationResult.getLocations().size() >0){

                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();

                                        location = "Latitude: "+ latitude + "\n" + "Longitude: "+ longitude;
                                        Log.d("TAG1", location);
                                    }
                                }
                            }, Looper.getMainLooper());

                } else {
                    turnOnGPS();
                }

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(ChatRoomCreatorActivity.this, "GPS is already turned on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(ChatRoomCreatorActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }
}

package com.example.conversationalist;

import static com.bumptech.glide.Glide.with;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {

    private ArrayList<Message> messages;
    // sender and receiver only will work on 2 man chatroom might need to be a list of sender images
    // USER is senderImg
    private Context context;


    public MessageAdapter(ArrayList<Message> messages, Context context) {
        this.messages = messages;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_holder, parent, false);
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
        holder.txtMessage.setText(messages.get(position).getContent());
        String nameDate = messages.get(position).getUsername() + " - " + messages.get(position).getDate();
        holder.fileName.setText(messages.get(position).getFile());
        holder.txtUsername.setText(nameDate);

        if (!messages.get(position).getImageContent().equals("")) {
            holder.imgSend.setVisibility(View.VISIBLE);
            if (isConnected()) {
                with(context).load(messages.get(position).getImageContent()).placeholder(R.drawable.account_image).error(R.drawable.account_image).into(holder.imgSend);
            } else {
                with(context).load(messages.get(position).getImageContent()).placeholder(R.drawable.account_image).error(R.drawable.account_image).into(holder.imgSend);
            }
        } else {
            holder.imgSend.setVisibility(View.GONE);
        }

        if (!messages.get(position).getFile().equals("")) {
            holder.fileImg.setVisibility(View.VISIBLE);
        } else {
            holder.fileImg.setVisibility(View.GONE);
        }

        ConstraintLayout constraintLayout = holder.ccll;

        // if the message is ours, put on the right from the left
        if (messages.get(position).getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            // USER
            with(context).load(messages.get(position).getSenderImg()).error(R.drawable.account_image).placeholder(R.drawable.account_image).into(holder.profImage);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);

            constraintSet.clear(R.id.profile_cardView, ConstraintSet.LEFT);
            constraintSet.clear(R.id.txt_message_content, ConstraintSet.LEFT);
            constraintSet.clear(R.id.txt_message_content, ConstraintSet.LEFT);
            constraintSet.clear(R.id.img_send, ConstraintSet.LEFT);

            constraintSet.clear(R.id.small_file_img, ConstraintSet.LEFT);

            constraintSet.connect(R.id.profile_cardView, ConstraintSet.RIGHT, R.id.ccLayout, ConstraintSet.RIGHT, 0);
            constraintSet.connect(R.id.txt_message_content, ConstraintSet.RIGHT, R.id.ccLayout, ConstraintSet.RIGHT, 0);
            constraintSet.connect(R.id.img_send, ConstraintSet.RIGHT, R.id.ccLayout, ConstraintSet.RIGHT, 0);
            constraintSet.connect(R.id.txt_message_content, ConstraintSet.TOP, R.id.profile_cardView, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.img_send, ConstraintSet.TOP, R.id.txt_message_content, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.txt_username_content, ConstraintSet.RIGHT, R.id.profile_cardView, ConstraintSet.LEFT, 0);

            if (!messages.get(position).getImageContent().equals("")) {
                constraintSet.connect(R.id.small_file_img, ConstraintSet.RIGHT, R.id.ccLayout, ConstraintSet.RIGHT, 0);
                constraintSet.connect(R.id.small_file_img, ConstraintSet.TOP, R.id.img_send, ConstraintSet.BOTTOM, 0);
            } else {
                constraintSet.connect(R.id.small_file_img, ConstraintSet.RIGHT, R.id.ccLayout, ConstraintSet.RIGHT, 0);
                constraintSet.connect(R.id.small_file_img, ConstraintSet.TOP, R.id.img_send, ConstraintSet.BOTTOM, 0);
            }

            constraintSet.applyTo(constraintLayout);

        } else {
            // Others
            with(context).load(messages.get(position).getSenderImg()).error(R.drawable.account_image).placeholder(R.drawable.account_image).into(holder.profImage);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);

            constraintSet.clear(R.id.profile_cardView, ConstraintSet.RIGHT);
            constraintSet.clear(R.id.txt_message_content, ConstraintSet.RIGHT);
            constraintSet.clear(R.id.img_send, ConstraintSet.RIGHT);
            constraintSet.clear(R.id.txt_username_content, ConstraintSet.RIGHT);

            constraintSet.clear(R.id.small_file_img, ConstraintSet.RIGHT);

            constraintSet.connect(R.id.profile_cardView, ConstraintSet.LEFT, R.id.ccLayout, ConstraintSet.LEFT, 0);
            constraintSet.connect(R.id.txt_message_content, ConstraintSet.LEFT, R.id.ccLayout, ConstraintSet.LEFT, 0);
            constraintSet.connect(R.id.txt_message_content, ConstraintSet.TOP, R.id.profile_cardView, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.img_send, ConstraintSet.LEFT, R.id.ccLayout, ConstraintSet.LEFT, 0);
            constraintSet.connect(R.id.img_send, ConstraintSet.TOP, R.id.txt_message_content, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.txt_username_content, ConstraintSet.LEFT, R.id.profile_cardView, ConstraintSet.RIGHT, 0);

            if (!messages.get(position).getImageContent().equals("")) {
                constraintSet.connect(R.id.small_file_img, ConstraintSet.LEFT, R.id.ccLayout, ConstraintSet.LEFT, 0);
                constraintSet.connect(R.id.small_file_img, ConstraintSet.TOP, R.id.img_send, ConstraintSet.BOTTOM, 0);
            } else {
                constraintSet.connect(R.id.small_file_img, ConstraintSet.LEFT, R.id.ccLayout, ConstraintSet.LEFT, 0);
                constraintSet.connect(R.id.small_file_img, ConstraintSet.TOP, R.id.img_send, ConstraintSet.BOTTOM, 0);
            }

            constraintSet.applyTo(constraintLayout);
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MessageHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {
        ConstraintLayout ccll;
        TextView txtMessage;
        TextView txtUsername;
        ImageView profImage;
        ImageView imgSend;
        TextView fileName;
        ImageView fileImg;
        MapView mapView;

        public MessageHolder(@NonNull View itemView) {
            super(itemView);

            ccll = itemView.findViewById(R.id.ccLayout);
            txtMessage = itemView.findViewById(R.id.txt_message_content);
            txtUsername = itemView.findViewById(R.id.txt_username_content);
            profImage = itemView.findViewById(R.id.small_profile_img);
            imgSend = itemView.findViewById(R.id.img_send);
            fileImg = itemView.findViewById(R.id.small_file_img);
            fileName = itemView.findViewById(R.id.txt_filename);
            mapView = itemView.findViewById(R.id.mapView);


            //mapView.onCreate(null);
            //mapView.getMapAsync(this);

            fileImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    String url = fileName.getText().toString();

                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    String title = URLUtil.guessFileName(url, null, null);
                    request.setTitle(title);
                    String cookie = CookieManager.getInstance().getCookie(url);
                    request.addRequestHeader("cookie", cookie);
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);


                    DownloadManager downloadmanager = (DownloadManager) context.
                            getSystemService(Context.DOWNLOAD_SERVICE);

                    long req = downloadmanager.enqueue(request);
                    /*
                    try {
                        downloadmanager.wait();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "A/A");
                    } catch (Exception e) {

                    }

                    fileName.setText("");
                    */

                    //
                }
            });

            txtMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, txtMessage.getText().toString());
                    sendIntent.setType("text/plain");

                    Intent shareIntent = Intent.createChooser(sendIntent, null);
                    context.startActivity(shareIntent);

                    return false;
                }
            });

            txtMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (txtMessage.getText().toString().split("@")[0].equals("https://www.google.com/maps/")) {
                        String lat = txtMessage.getText().toString().split("@")[1].split(",")[0];
                        String longitude = txtMessage.getText().toString().split("@")[1].split(",")[1];
                        Uri gmmIntentUri = Uri.parse("geo:" +lat +  "," + longitude);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        context.startActivity(mapIntent);
                    }

                }
            });


        }

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            MapsInitializer.initialize(context.getApplicationContext());
            LatLng ll = new LatLng(0, 0);
            googleMap.addMarker(new MarkerOptions().position(ll).title("Marker"));
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 11f));
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            googleMap.setMyLocationEnabled(true);
            mapView.onResume();
        }
    }

    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }


}

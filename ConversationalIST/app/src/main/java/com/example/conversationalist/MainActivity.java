package com.example.conversationalist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword, edtEmail;
    private Button btnSubmit, btnGuest, btnLogin, btnSimpleLogIn, btnSimpleSignUp;
    private TextView txtLoginInfo,txtSingUpInfo ;
    private Intent appLinkIntent;
    private String appLinkAction, currPassword;
    private Uri appLinkData;
    private PasswordGenerator passwordGenerator;

    private boolean isSigningUp = true;

    public static class PasswordGenerator {
        public static String generateRandomPassword(int len) {
            String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghi"
                    +"jklmnopqrstuvwxyz!@#$%&";
            Random rnd = new Random();
            StringBuilder sb = new StringBuilder(len);
            for (int i = 0; i < len; i++)
                sb.append(chars.charAt(rnd.nextInt(chars.length())));
            return sb.toString();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtUsername = findViewById(R.id.edtUsername);
        btnGuest = findViewById(R.id.btnGuest);

        btnLogin = findViewById(R.id.btnLogin);
        btnSimpleLogIn = findViewById(R.id.btnSimpleLogIn);
        btnSimpleSignUp = findViewById(R.id.btnSimpleSignUp);
        txtSingUpInfo = findViewById(R.id.txtSignUpInfo);

        btnSubmit = findViewById(R.id.btnSubmit);

        txtLoginInfo = findViewById(R.id.txtLoginInfo);

        appLinkIntent = getIntent();
        appLinkAction = appLinkIntent.getAction();
        appLinkData = appLinkIntent.getData();

        PasswordGenerator passwordGenerator = new PasswordGenerator();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, FriendsActivity.class)
                    .putExtra("password", currPassword)
            );
            finish();
        }
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (edtEmail.getText().toString().isEmpty() || edtPassword.getText().toString().isEmpty() || edtUsername.getText().toString().isEmpty()) {
                    if (isSigningUp && edtUsername.getText().toString().isEmpty()) {
                        Toast.makeText(MainActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(MainActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
                    return;

                }

                handleSignUp();

            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtEmail.getText().toString().isEmpty() || edtPassword.getText().toString().isEmpty() || edtUsername.getText().toString().isEmpty()) {
                    if (isSigningUp && edtUsername.getText().toString().isEmpty()) {
                        Toast.makeText(MainActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(MainActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
                    return;
                }
                handleLogin();
            }
        });

        btnSimpleLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtPassword.getText().toString().isEmpty() || edtUsername.getText().toString().isEmpty()) {
                    if (isSigningUp && edtUsername.getText().toString().isEmpty()) {
                        Toast.makeText(MainActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(MainActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseAuth.getInstance().signInWithEmailAndPassword(edtUsername.getText().toString() + "@conversationalist-3003c.com", edtPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            startActivity(new Intent(MainActivity.this, FriendsActivity.class)
                            );
                            Toast.makeText(MainActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        btnSimpleSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edtUsername.equals("")) {
                    FirebaseDatabase.getInstance().getReference("user").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //cycles through all users (maybe we need chatrooms instead)
                            boolean i = true;
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String username = dataSnapshot.child("username").getValue(String.class);
                                if (edtUsername.getText().toString().equals(username)) {
                                    i = false;
                                    Toast.makeText(MainActivity.this, "Username already in use", Toast.LENGTH_SHORT).show();
                                }
                            }
                            if (i) {
                                FirebaseAuth.getInstance().createUserWithEmailAndPassword(edtUsername.getText().toString() + "@conversationalist-3003c.com", edtPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseDatabase.getInstance().getReference("user/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new User(edtUsername.getText().toString(), edtEmail.getText().toString(), ""));
                                            startActivity(new Intent(MainActivity.this, FriendsActivity.class)
                                            );
                                            Toast.makeText(MainActivity.this, "Signed up successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MainActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                        @Override
                        public void onCancelled (@NonNull DatabaseError error){

                        }
                    });
                }
            }
        });



        btnGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtUsername.getText().toString().isEmpty()) {
                    if (isSigningUp && edtUsername.getText().toString().isEmpty()) {
                        Toast.makeText(MainActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(MainActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseDatabase.getInstance().getReference("user").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //cycles through all users (maybe we need chatrooms instead)
                        boolean i = true;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String username = dataSnapshot.child("username").getValue(String.class);
                            if (edtUsername.getText().toString().equals(username)) {
                                i = false;
                                Toast.makeText(MainActivity.this, "Username already in use", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (i) {
                            currPassword = PasswordGenerator.generateRandomPassword(32);
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(edtUsername.getText().toString() + "@conversationalist-3003c.com", currPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseDatabase.getInstance().getReference("user/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new User(edtUsername.getText().toString(), edtEmail.getText().toString(), ""));
                                        startActivity(new Intent(MainActivity.this, FriendsActivity.class)
                                                .putExtra("password", currPassword)
                                        );
                                        Toast.makeText(MainActivity.this, "Signed up successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled (@NonNull DatabaseError error){

                    }
                });
            }
        });

        txtLoginInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSimpleLogIn.setVisibility(View.VISIBLE);
                txtSingUpInfo.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.GONE);
                btnSimpleSignUp.setVisibility(View.GONE);
                txtLoginInfo.setVisibility(View.GONE);
                btnSubmit.setVisibility(View.GONE);
                btnGuest.setVisibility(View.VISIBLE);
            }
        });

        txtSingUpInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSimpleLogIn.setVisibility(View.GONE);
                txtSingUpInfo.setVisibility(View.GONE);
                btnLogin.setVisibility(View.GONE);
                btnSimpleSignUp.setVisibility(View.VISIBLE);
                txtLoginInfo.setVisibility(View.VISIBLE);
                btnSubmit.setVisibility(View.GONE);
                btnGuest.setVisibility(View.VISIBLE);
            }
        });
    }

    private void handleSignUp() {
        FirebaseDatabase.getInstance().getReference("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //cycles through all users (maybe we need chatrooms instead)
                boolean i = true;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String username = dataSnapshot.child("username").getValue(String.class);
                    if (edtUsername.getText().toString().equals(username)) {
                        i = false;
                        Toast.makeText(MainActivity.this, "Username already in use", Toast.LENGTH_SHORT).show();
                    }
                }
                if (i) {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseDatabase.getInstance().getReference("user/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new User(edtUsername.getText().toString(), edtEmail.getText().toString(), ""));
                                startActivity(new Intent(MainActivity.this, FriendsActivity.class)
                                );
                                Toast.makeText(MainActivity.this, "Signed up successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled (@NonNull DatabaseError error){

            }
        });
    }

    private void handleLogin() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    startActivity(new Intent(MainActivity.this, FriendsActivity.class)
                    );
                    Toast.makeText(MainActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }




}
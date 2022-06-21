package com.example.conversationalist;

import java.util.ArrayList;

public class ChatRoom {
    private String type;
    private String chatRoomId;
    private ArrayList<String> users;
    private ArrayList<Message> messages;
    private String chatImage;
    private String uid;
    private String latitude,longitude,rad;


    public ChatRoom() {

    }

    public ChatRoom(String type, String chatRoomId, ArrayList<String> usersId, ArrayList<Message> messages, String chatImage, String uid, String latitude, String longitude, String rad) {
        this.type = type;
        this.chatRoomId = chatRoomId;
        this.users = usersId;
        this.messages = messages;
        this.chatImage = chatImage;
        this.uid = uid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rad = rad;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getRad() {
        return rad;
    }

    public void setRad(String rad) {
        this.rad = rad;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getChatImage() {
        return chatImage;
    }

    public void setChatImage(String chatImage) {
        this.chatImage = chatImage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> usersId) {
        this.users = usersId;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }
}

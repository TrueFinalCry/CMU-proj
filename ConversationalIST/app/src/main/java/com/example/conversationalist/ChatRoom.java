package com.example.conversationalist;

import java.util.ArrayList;

public class ChatRoom {
    private String type;
    private String chatRoomId;
    private ArrayList<User> users;
    private ArrayList<Message> messages;
    private String chatImage;


    public ChatRoom() {

    }

    public ChatRoom(String type, String chatRoomId, ArrayList<User> usersId, ArrayList<Message> messages, String chatImage) {
        this.type = type;
        this.chatRoomId = chatRoomId;
        this.users = usersId;
        this.messages = messages;
        this.chatImage = chatImage;
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

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> usersId) {
        this.users = usersId;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }
}
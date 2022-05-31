package com.example.conversationalist;

public class Message {
    private String sender,content;
    private String chatRoomId, senderImg;
    private String username;
    // private string reactions

    public Message() {
    }

    public Message(String sender,String senderImg, String content, String chatRoomId, String username) {
        this.sender = sender;
        this.content = content;
        this.chatRoomId = chatRoomId;
        this.senderImg = senderImg;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getSenderImg() {
        return senderImg;
    }

    public void setSenderImg(String senderImg) {
        this.senderImg = senderImg;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

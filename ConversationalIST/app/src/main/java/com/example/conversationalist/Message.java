package com.example.conversationalist;

public class Message {
    private String sender,receiver,content;
    private String chatRoomId, senderImg;
    // private string reactions

    public Message() {
    }

    public Message(String sender, String content, String chatRoomId, String senderImg) {
        this.sender = sender;
        this.content = content;
        this.chatRoomId = chatRoomId;
        this.senderImg = senderImg;
    }

    public Message(String sender, String receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
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

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

package com.example.conversationalist;

public class Message {
    private String sender,content;
    private String imageContent;
    private String chatRoomId, senderImg;
    private String username;
    private String file;
    private String date;
    private String lat;
    private String longitude;

    // private string reactions

    public Message() {
    }


    public Message(String sender, String senderImg, String content, String imageContent, String chatRoomId, String username, String file, String date, String lat, String longitude) {
        this.sender = sender;
        this.content = content;
        this.imageContent = imageContent;
        this.file = file;
        this.chatRoomId = chatRoomId;
        this.senderImg = senderImg;
        this.username = username;
        this.date = date;
        this.lat = lat;
        this.longitude = longitude;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getImageContent() {
        return imageContent;
    }

    public void setImageContent(String imageContent) {
        this.imageContent = imageContent;
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

package tech.greedylabs.crowspot.model;

public class Hotspot {
    private String username;
    private String latitude;
    private String longitude;
    private String threatType;
    private String address;
    private String time;
    private int upVote;
    private int downVote;

    public Hotspot() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getThreatType() {
        return threatType;
    }

    public void setThreatType(String threatType) {
        this.threatType = threatType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getUpVote() {
        return upVote;
    }

    public void setUpVote(int upVote) {
        this.upVote = upVote;
    }

    public int getDownVote() {
        return downVote;
    }

    public void setDownVote(int downVote) {
        this.downVote = downVote;
    }

    public Hotspot(String username, String latitude, String longitude, String threatType, String address, String time, int upVote, int downVote) {
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
        this.threatType = threatType;
        this.address = address;
        this.time = time;
        this.upVote = upVote;
        this.downVote = downVote;
    }
}

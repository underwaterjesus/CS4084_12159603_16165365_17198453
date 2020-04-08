package ie.ul.discoverlimerick;

import com.google.firebase.Timestamp;

public class Upload {
    private Timestamp timestamp;
    private String username;
    private String fileName;
    private String userID;

    public Upload(Timestamp timestamp, String username, String fileName, String userID){
        this.timestamp = timestamp;
        this.username = username;
        this.fileName = fileName;
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID){
        this.userID = userID;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getfileName() {
        return fileName;
    }

    public void setfileName(String fileName) {
        this.fileName = fileName;
    }
}

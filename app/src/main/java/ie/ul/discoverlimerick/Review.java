package ie.ul.discoverlimerick;

import com.google.firebase.Timestamp;

public class Review {
    private String id;
    private String username;
    private String review;
    private Timestamp stamp;
    private MyLocation location;

    public Review(String id, String username, String review, Timestamp stamp, MyLocation location) {
        this.id = id;
        this.username = username;
        this.review = review;
        this.stamp = stamp;
        this.location = location;
    }

    public MyLocation getLocation() {
        return location;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getReview() {
        return review;
    }

    public Timestamp getStamp() {
        return stamp;
    }
}

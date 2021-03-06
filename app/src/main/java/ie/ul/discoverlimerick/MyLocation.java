package ie.ul.discoverlimerick;

import android.location.Location;
import android.util.Log;

import java.io.Serializable;

public class MyLocation implements Serializable {
    private String id;
    private String name;
    private String address;
    private String desc;
    private double lat;
    private double lng;

    public MyLocation(String id, String name, String address, String desc, double lat, double lng){
        this.id = id;
        this.name = name;
        this.address = address;
        this.desc = desc;
        this.lat = lat;
        this.lng = lng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getDistance(Location start)
    {
        double x1 = Math.toRadians(lat);
        double x2 = Math.toRadians(start.getLatitude());

        double deltaX = Math.toRadians( (start.getLatitude()) - (lat) );
        double deltaY = Math.toRadians( (start.getLongitude()) - (lng) );


        double a = (
                (Math.sin(deltaX / 2) *  Math.sin(deltaX / 2)) +
                 Math.cos(x1) * Math.cos(x2) *
                 (Math.sin(deltaY / 2) * Math.sin(deltaY / 2)));
        double c = 2 * Math.asin(Math.sqrt(a));

        return c * 6371.0;
    }
}

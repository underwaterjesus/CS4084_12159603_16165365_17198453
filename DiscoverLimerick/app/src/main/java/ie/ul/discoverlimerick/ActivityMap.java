package ie.ul.discoverlimerick;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ActivityMap extends AppCompatActivity implements OnMapReadyCallback {
    private MyLocation location;
    private SupportMapFragment map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        location = (MyLocation) intent.getSerializableExtra("MyLocation");

        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMyLocationEnabled(MainActivity.locationPermission);
        LatLng latLng = new LatLng(location.getLat(), location.getLng());
        Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(location.getName()));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
        marker.showInfoWindow();
    }
}

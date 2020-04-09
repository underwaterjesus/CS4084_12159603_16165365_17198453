package ie.ul.discoverlimerick;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static FirebaseAuth mAuth;

    private DrawerLayout drawer;

    public static CategoryItem[] CATEGORIES = {new CategoryItem(R.drawable.ic_bowling, "Amusements"),
            new CategoryItem(R.drawable.ic_bank, "Arts & Culture"),
            new CategoryItem(R.drawable.ic_local_bar_24px, "Bars"),
            new CategoryItem(R.drawable.ic_family, "Family"),
            new CategoryItem(R.drawable.ic_disco_ball, "Nightlife"),
            new CategoryItem(R.drawable.ic_explore_24px, "Outdoors"),
            new CategoryItem(R.drawable.ic_sports_football_24px, "Sports")};

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static String selected_category;
    public static String selected_location;
    public static boolean locationPermission;
    public static boolean sendHome = false;
    private static final int RC_SIGN_IN = 123;
    private static Location mLastKnownLocation;
    private static LocationRequest mLocationRequest;
    public static FusedLocationProviderClient locationProvider;


    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().clear();
        navigationView.setNavigationItemSelectedListener(this);

        setMenu();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        getPermission();
        locationProvider = LocationServices.getFusedLocationProviderClient(this);
        monitorLocation();

        goHome();
        //navigationView.bringToFront();
    }

    private void monitorLocation() {
        Log.d("Monitor", Boolean.toString(locationPermission));
        if (locationPermission) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();

            SettingsClient settingsClient = LocationServices.getSettingsClient(this);
            settingsClient.checkLocationSettings(locationSettingsRequest);

            locationProvider.requestLocationUpdates(mLocationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    setLastKnownLocation(locationResult.getLastLocation());
                    Log.d("Coordinates", mLastKnownLocation.toString());
                }
            }, Looper.myLooper());
        }
    }

    private void setLastKnownLocation(Location location) {
        mLastKnownLocation = location;
    }

    private void getPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermission = true;
        } else {
            //if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    locationPermission = true;
                else
                    locationPermission = false;
        }
    }

    private void goHome() {
        sendHome = false;
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (sendHome) {
                sendHome = false;
                goHome();
                return;
            }
            super.onBackPressed();
            sendHome = false;
        }
    }

    public void onClickLogin() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build());
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(),
                RC_SIGN_IN);
    }

    public void onClickLogOut() {
        mAuth.signOut();
        setMenu();
        if (sendHome)
            goHome();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                setMenu();
            } else {
                showToast(MainActivity.this, "Error Signing in!");
            }
        }
    }

    public static void showToast(Context c, String s) {
        Toast.makeText(c, s, Toast.LENGTH_LONG).show();
    }

    private void setMenu() {
        navigationView.getMenu().clear();
        if (mAuth.getCurrentUser() == null) {
            navigationView.inflateMenu(R.menu.drawer_menu);
        } else {
            navigationView.inflateMenu(R.menu.drawer_menu_signed_in);
            MenuItem menuItem = navigationView.getMenu().findItem(R.id.nav_username);
            menuItem.setTitle("Logged in as: " + mAuth.getCurrentUser().getDisplayName());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                if (drawer.isDrawerOpen(GravityCompat.START))
                    drawer.closeDrawer(GravityCompat.START);
                goHome();
                break;
            case R.id.nav_log_in:
                if (drawer.isDrawerOpen(GravityCompat.START))
                    drawer.closeDrawer(GravityCompat.START);
                onClickLogin();
                break;
            case R.id.nav_log_out:
                if (drawer.isDrawerOpen(GravityCompat.START))
                    drawer.closeDrawer(GravityCompat.START);
                onClickLogOut();
                break;
            case R.id.nav_my_reviews:
                if (drawer.isDrawerOpen(GravityCompat.START))
                    drawer.closeDrawer(GravityCompat.START);
                sendHome = true;
                onMyReviewsClicked();
                break;
            case R.id.nav_my_images:
                if (drawer.isDrawerOpen(GravityCompat.START))
                    drawer.closeDrawer(GravityCompat.START);
                sendHome = true;
                onMyImagesClicked();
                break;
        }

        return true;
    }

    private void onMyReviewsClicked() {
        if (mAuth.getCurrentUser() != null) {
            FragmentTransaction tranny = getSupportFragmentManager().beginTransaction();
            tranny.replace(R.id.fragment_container, new MyReviewsFragment());
            tranny.addToBackStack(null);
            tranny.commit();
        } else {
            showToast(this, "Please login to complete this action");
            if (drawer.isDrawerOpen(GravityCompat.START))
                drawer.closeDrawer(GravityCompat.START);
            goHome();
        }
    }

    private void onMyImagesClicked() {
        if (mAuth.getCurrentUser() != null) {
            FragmentTransaction tranny = getSupportFragmentManager().beginTransaction();
            tranny.replace(R.id.fragment_container, new MyUploadsFragment());
            tranny.addToBackStack(null);
            tranny.commit();
        } else {
            showToast(this, "Please login to complete this action");
            if (drawer.isDrawerOpen(GravityCompat.START))
                drawer.closeDrawer(GravityCompat.START);
            goHome();
        }
    }

    public static Location getLastLocation() {
        return mLastKnownLocation;
    }
}
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
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
import java.util.Locale;

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
    public static Button activitySpeechButton;

    public static TextToSpeech mTTS;
    public static boolean canSpeak = false;
    public static boolean canShow = false;

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);

        closeKeyboard();

        if (!isConnected())
            startActivity(new Intent(this, ConnectionActivity.class));

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
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                closeKeyboard();
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                closeKeyboard();
                super.onDrawerClosed(drawerView);
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        activitySpeechButton = findViewById(R.id.activity_speech_button);
        activitySpeechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activitySpeechButtonClicked();
            }
        });

        getPermission();
        locationProvider = LocationServices.getFusedLocationProviderClient(this);
        monitorLocation();

        if (savedInstanceState == null) {
            goHome();
        }
        //navigationView.bringToFront();
        if (MainActivity.mTTS == null) {
            MainActivity.mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int result = MainActivity.mTTS.setLanguage(Locale.ENGLISH);

                        if (!(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)) {
                            MainActivity.canSpeak = true;
                            if (LocationFragment.speech != null)
                                LocationFragment.speech.setBackgroundResource(R.drawable.speech_filled);
                        }

                        MainActivity.mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String utteranceId) {
                                Log.d("TTS", "onStart");
                                LocationFragment.speech.setBackgroundResource(R.drawable.speech_filled_stop);
                                thread2.run();
                            }

                            @Override
                            public void onDone(String utteranceId) {
                                Log.d("TTS", "onDone");
                                LocationFragment.speech.setBackgroundResource(R.drawable.speech_filled);
                                thread.run();
                            }

                            @Override
                            public void onError(String utteranceId) {
                                Log.d("TTS", "onError");
                                LocationFragment.speech.setBackgroundResource(R.drawable.speech_filled);
                                thread.run();
                            }

                            @Override
                            public void onStop(String utteranceId, boolean interrupted) {
                                Log.d("TTS", "onStop");
                                LocationFragment.speech.setBackgroundResource(R.drawable.speech_filled);
                                thread.run();

                                super.onStop(utteranceId, interrupted);
                            }
                        });
                    }
                }
            });
        }
    }

    private void activitySpeechButtonClicked() {
        if (mTTS != null)
            if (mTTS.isSpeaking())
                mTTS.stop();
            else
                activitySpeechButton.setVisibility(View.GONE);
        else
            activitySpeechButton.setVisibility(View.GONE);
    }

    Thread thread = new Thread() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideButton();
                }
            });
        }
    };

    Thread thread2 = new Thread() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showButton();
                }
            });
        }
    };

    public void showButton() {
        try {
            if (mTTS != null) {
                if (mTTS.isSpeaking() && canShow) {
                    Log.d("showButton", "inside if");
                    activitySpeechButton.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            String s = e.getMessage() == null ? "null" : e.getMessage();
            Log.d("showButton", s);
        }
    }

    public void hideButton() {
        try {
            if (activitySpeechButton.getVisibility() == View.VISIBLE)
                activitySpeechButton.setVisibility(View.GONE);
        } catch (Exception e) {
            String s = e.getMessage() == null ? "null" : e.getMessage();
            Log.d("hideButton", s);
        }
    }

    public static void unsetTTS() {
        mTTS = null;
        canSpeak = false;
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
        closeKeyboard();
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (sendHome) {
                sendHome = false;
                goHome();
                return;
            }
            sendHome = false;
            super.onBackPressed();
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //getSupportFragmentManager().putFragment("activeFragment", outState, );
    }

    private boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiConn = false;
        boolean isMobileConn = false;

        try {

            for (Network network : connMgr.getAllNetworks()) {
                NetworkInfo networkInfo = connMgr.getNetworkInfo(network);

                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    isWifiConn |= networkInfo.isConnected();
                }

                if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    isMobileConn |= networkInfo.isConnected();
                }

                return isWifiConn || isMobileConn;

            }
        } catch (Exception e) {

            String s = e.getMessage() == null ? "unable to give more details" : e.getMessage();
            Log.d("isConnected", s);
        }

        return false;
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();

        if (view != null && view.getWindowToken() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    @Override
    protected void onStop() {
        Log.d("MainActivity", "onStop");
        try {
            if (mTTS != null && !isChangingConfigurations()) {
                mTTS.stop();
                mTTS.shutdown();
                unsetTTS();
            }
        } catch (Exception e) {
            String s = e.getMessage() == null ? "null" : e.getMessage();
            Log.d("onStop", s);
        }

        super.onStop();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Log.d("MainActivity", "onRestoreInstanceState");
        thread2.run();
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestart() {
        Log.d("MainActivity", "onRestart");
        if (MainActivity.mTTS == null) {
            MainActivity.mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int result = MainActivity.mTTS.setLanguage(Locale.ENGLISH);

                        if (!(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)) {
                            MainActivity.canSpeak = true;
                            if (LocationFragment.speech != null)
                                LocationFragment.speech.setBackgroundResource(R.drawable.speech_filled);
                        }

                        MainActivity.mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String utteranceId) {
                                Log.d("TTS", "onStart");
                                LocationFragment.speech.setBackgroundResource(R.drawable.speech_filled_stop);
                                thread2.run();
                            }

                            @Override
                            public void onDone(String utteranceId) {
                                Log.d("TTS", "onDone");
                                thread.run();
                                LocationFragment.speech.setBackgroundResource(R.drawable.speech_filled);
                            }

                            @Override
                            public void onError(String utteranceId) {
                                Log.d("TTS", "onError");
                                LocationFragment.speech.setBackgroundResource(R.drawable.speech_filled);
                                thread.run();
                            }

                            @Override
                            public void onStop(String utteranceId, boolean interrupted) {
                                Log.d("TTS", "onStop");
                                LocationFragment.speech.setBackgroundResource(R.drawable.speech_filled);
                                thread.run();

                                super.onStop(utteranceId, interrupted);
                            }
                        });
                    }
                }
            });
        }

        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //thread.run();

        super.onDestroy();
    }
}
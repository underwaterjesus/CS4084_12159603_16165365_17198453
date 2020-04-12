package ie.ul.discoverlimerick;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class LocationFragment extends Fragment {
    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    private ImageView img;

    private Button leaveReview;
    private Button readReviews;
    private Button leaveImage;
    private Button viewImages;
    private Button viewMap;

    private CollectionReference db;
    private FirebaseStorage storage;
    private StorageReference mStorageReference;
    private MyLocation location;

    private final static String PROJECT_URL = "gs://discover-limerick.appspot.com";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        if(!isConnected())
            startActivity(new Intent(getContext(), ConnectionActivity.class));

        location = (MyLocation) getArguments().getSerializable("Location");

        storage = FirebaseStorage.getInstance();
        mStorageReference = storage.getReference();

        String referenceString = "img/" + location.getId() + ".jpg";
        StorageReference imgRef = mStorageReference.child(referenceString);

        img = (ImageView) view.findViewById(R.id.img);
        Glide.with(this).load(imgRef).into(img);

        tv1 = (TextView) view.findViewById(R.id.tv1);
        tv2 = (TextView) view.findViewById(R.id.tv2);
        tv3 = (TextView) view.findViewById(R.id.tv3);

        tv1.setText(location.getName());
        tv2.setText(location.getAddress());
        tv3.setText(location.getDesc());

        leaveReview = (Button) view.findViewById(R.id.leave_review);
        readReviews = (Button) view.findViewById(R.id.read_reviews);
        leaveImage = (Button) view.findViewById(R.id.leave_image);
        viewImages = (Button) view.findViewById(R.id.view_images);
        viewMap = (Button) view.findViewById(R.id.mapBtn);

        leaveReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLeaveReviewClicked(v);
            }
        });

        readReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReadReviewsClicked(v);
            }
        });

        leaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLeaveImageClicked();
            }
        });

        viewImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewImagesClicked();
            }
        });

        viewMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMapClicked(v);
            }
        });

        return view;
    }

    private void onLeaveImageClicked() {
        if(MainActivity.mAuth.getCurrentUser() != null) {
            Fragment frag = new UploadFragment();

            FragmentTransaction tranny = getActivity().getSupportFragmentManager().beginTransaction();
            tranny.replace(R.id.fragment_container, frag);
            tranny.addToBackStack(null);
            tranny.commit();
        }else {
            MainActivity.showToast(getContext(), "Please login to upload an image");
        }
    }

    private void onViewImagesClicked(){
        Fragment frag = new ViewUploadsFragment();

        FragmentTransaction tranny = getActivity().getSupportFragmentManager().beginTransaction();
        tranny.replace(R.id.fragment_container, frag);
        tranny.addToBackStack(null);
        tranny.commit();
    }

    private void onReadReviewsClicked(View v) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("Location", location);

        Fragment frag = new ReadReviewsFragment();
        frag.setArguments(bundle);

        FragmentTransaction tranny = getActivity().getSupportFragmentManager().beginTransaction();
        tranny.replace(R.id.fragment_container, frag);
        tranny.addToBackStack(null);
        tranny.commit();
    }

    private void onLeaveReviewClicked(View v) {
        if (MainActivity.mAuth.getCurrentUser() != null) {

            Bundle bundle = new Bundle();
            bundle.putSerializable("Location", location);

            Fragment frag = new LeaveReviewFragment();
            frag.setArguments(bundle);

            FragmentTransaction tranny = getActivity().getSupportFragmentManager().beginTransaction();
            tranny.replace(R.id.fragment_container, frag);
            tranny.addToBackStack(null);
            tranny.commit();

        } else {

            MainActivity.showToast(getContext(), "Please log in to leave a review");

        }
    }

    private void onMapClicked(View v) {

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());

        if (available == ConnectionResult.SUCCESS) {
            sendToMap();
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, 9001);
            dialog.show();
        } else {
            MainActivity.showToast(getContext(), "Google Play Services issue. Maps unavailable.");
        }

    }

    private void sendToMap() {
        Intent intent = new Intent(getActivity(), ActivityMap.class);

        intent.putExtra("MyLocation", location);

        startActivity(intent);
    }

    private boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
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
}

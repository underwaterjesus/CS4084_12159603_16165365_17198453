package ie.ul.discoverlimerick;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.firestore.CollectionReference;

public class LocationFragment extends Fragment {
    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    private ImageView img;

    private Button leaveReview;
    private Button readReviews;
    private Button viewMap;

    private CollectionReference db;
    private MyLocation location;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        location = (MyLocation) getArguments().getSerializable("Location");

        tv1 = (TextView) view.findViewById(R.id.tv1);
        tv2 = (TextView) view.findViewById(R.id.tv2);
        tv3 = (TextView) view.findViewById(R.id.tv3);

        tv1.setText(location.getName());
        tv2.setText(location.getAddress());
        tv3.setText(location.getDesc());

        leaveReview = (Button) view.findViewById(R.id.leave_review);
        readReviews = (Button) view.findViewById(R.id.read_reviews);
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

        viewMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMapClicked(v);
            }
        });

        return view;
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
}

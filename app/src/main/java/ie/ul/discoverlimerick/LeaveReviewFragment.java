package ie.ul.discoverlimerick;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LeaveReviewFragment extends Fragment {
    private FirebaseFirestore db;
    private MyLocation location;

    private static int count = 250;

    private TextView tv1;
    private TextView tv2;
    private EditText et;
    private Button btn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leave_review, container, false);

        if(!isConnected())
            startActivity(new Intent(getContext(), ConnectionActivity.class));

        db = FirebaseFirestore.getInstance();
        location = (MyLocation) getArguments().getSerializable("Location");

        tv1 = view.findViewById(R.id.review_header);
        tv2 = view.findViewById(R.id.counter);
        et = view.findViewById(R.id.review_body);
        btn = view.findViewById(R.id.leave_review_btn);

        tv1.setText("Leave Review for\n" + location.getName());

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = et.length();
                String counter_message = "Characters: " + length + "/" + LeaveReviewFragment.count;
                tv2.setText(counter_message);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLeaveReviewClicked(v);
            }
        });

        return view;
    }

    private void onLeaveReviewClicked(View v) {
        if (MainActivity.mAuth.getCurrentUser() != null) {

            String review = et.getText().toString().trim();
            if (review.length() == 0) {

                MainActivity.showToast(getContext(), "Please enter a review");

            } else {

                Map<String, Object> entry = new HashMap<>();
                entry.put("review", review);
                entry.put("time", Timestamp.now());
                entry.put("username", MainActivity.mAuth.getCurrentUser().getDisplayName());
                entry.put("userID", MainActivity.mAuth.getCurrentUser().getUid());
                entry.put("location_name", location.getName());

                db.collection(MainActivity.selected_category).document(MainActivity.selected_location).collection("Reviews")
                        .add(entry).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        MainActivity.showToast(getContext(), "Review successfully posted");
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        MainActivity.showToast(getContext(), "Error posting review");
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });

            }

        } else {

            MainActivity.showToast(getContext(), "Error: please log in again");
            getActivity().getSupportFragmentManager().popBackStack();

        }
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

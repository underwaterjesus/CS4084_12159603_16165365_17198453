package ie.ul.discoverlimerick;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ReadReviewsFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ReviewAdapter adapter;
    private MyLocation location;

    private RelativeLayout relativeLayout;

    private static CollectionReference db;
    private static ArrayList<Review> reviews;

    private Parcelable position;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        location = (MyLocation) getArguments().getSerializable("Location");
        getReviews();
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read_reviews, container, false);

        relativeLayout = view.findViewById(R.id.review_relative_layout);

        setCycler(view);
        return view;
    }

    private void getReviews() {
        reviews = new ArrayList<Review>();

        db = FirebaseFirestore.getInstance().collection(MainActivity.selected_category).document(MainActivity.selected_location).collection("Reviews");
        db.orderBy("time", Query.Direction.DESCENDING);

        db.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        try {
                            Review temp = new Review(document.getId(), document.getString("username"), document.getString("review"), document.getTimestamp("time"), location);
                            reviews.add(temp);
                        } catch (Exception e) {
                            String s = e.getMessage() != null ? e.getMessage() : "unable to give more details";
                            Log.d("Exception Caught: ", s);
                        }
                    }
                } else {
                    MainActivity.showToast(getContext(), "Error: cannot access database");
                }
                try {
                    adapter.notifyDataSetChanged();

                    if (reviews.isEmpty()) {
                        TextView textView = new TextView(getContext());
                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
                        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                        textView.setLayoutParams(lp);
                        textView.setText("Nothing to see here yet!");
                        textView.setTextSize(20);
                        textView.setTypeface(null, Typeface.BOLD);
                        textView.setTextColor(0xFF000000);
                        relativeLayout.addView(textView);
                    }

                    if (position != null) {
                        layoutManager.onRestoreInstanceState(position);
                    }
                } catch (Exception e) {
                    String s = e.getMessage() != null ? e.getMessage() : "unable to give more details";
                    Log.i("ReadReviewsFragment: ", s);
                }
            }
        });
    }

    private void setCycler(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.review_recycler);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ReviewAdapter(reviews);

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();

        position = recyclerView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {//Log.i("ReadReviewsFragment", "onViewStateRestored");
            position = savedInstanceState.getParcelable("position");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("position", position);
    }
}

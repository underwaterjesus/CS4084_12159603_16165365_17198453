package ie.ul.discoverlimerick;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    private static CollectionReference db;
    private static ArrayList<Review> reviews;

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
                adapter.notifyDataSetChanged();
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
}

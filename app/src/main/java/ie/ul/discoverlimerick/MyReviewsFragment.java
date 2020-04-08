package ie.ul.discoverlimerick;

import android.location.Location;
import android.os.Bundle;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MyReviewsFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ReviewAdapter adapter;

    private static CollectionReference db;
    private static ArrayList<Review> reviews;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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

    private void setCycler(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.review_recycler);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ReviewAdapter(reviews);

        recyclerView.setAdapter(adapter);
    }

    private void getReviews() {
        if (MainActivity.mAuth.getCurrentUser() == null) {
            MainActivity.showToast(getContext(), "Error: please log in again");
            getActivity().getSupportFragmentManager().popBackStack();
        } else {
            reviews = new ArrayList<Review>();

            Query query = FirebaseFirestore.getInstance().collectionGroup("Reviews").whereEqualTo("userID", MainActivity.mAuth.getCurrentUser().getUid()).orderBy("time", Query.Direction.DESCENDING);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MyLocation location = new MyLocation("id", document.getString("location_name"), "address",
                                                        "desc", 0.0, 0.0);
                            Review toAdd = new Review(document.getId(), document.getString("username"), document.getString("review"),
                                                        document.getTimestamp("time"), location);

                            reviews.add(toAdd);
                        }
                    } else {
                        MainActivity.showToast(getContext(), "Error: cannot access database");
                    }

                    adapter.notifyDataSetChanged();
                }
            });
        }

    }
}

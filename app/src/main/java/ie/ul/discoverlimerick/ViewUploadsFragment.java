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
import java.util.List;

public class ViewUploadsFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private UploadAdapter adapter;

    private static CollectionReference db;
    private static ArrayList<Upload> uploads;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getUploads();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_uploads, container, false);

        setRecycler(view);

        return view;
    }

    private void getUploads() {
        uploads = new ArrayList<Upload>();

        db = FirebaseFirestore.getInstance().collection(MainActivity.selected_category).document(MainActivity.selected_location).collection("Images");
        db.orderBy("time", Query.Direction.DESCENDING);

        db.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        try {
                            Upload toAdd = new Upload(document.getTimestamp("time"), document.getString("username"), document.getString("fileName"), document.getString("userID"));
                            uploads.add(toAdd);
                        } catch (Exception e) {
                            String s = e.getMessage() != null ? e.getMessage() : "unable to give more details";
                            Log.d("Exception Caught: ", s);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setRecycler(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.uploads_recycler);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new UploadAdapter(uploads, getContext());

        recyclerView.setAdapter(adapter);
    }
}

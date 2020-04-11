package ie.ul.discoverlimerick;

import android.os.Bundle;
import android.os.Parcelable;
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

public class MyUploadsFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private UploadAdapter adapter;

    private static ArrayList<Upload> uploads;

    private Parcelable position;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMyUploads();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_uploads, container, false);

        setRecycler(view);

        return view;
    }

    private void getMyUploads() {
        if (MainActivity.mAuth.getCurrentUser() == null) {
            MainActivity.showToast(getContext(), "Error: please log in again");
            MainActivity.sendHome = false;
            getActivity().getSupportFragmentManager().popBackStack();
        } else {
            uploads = new ArrayList<Upload>();

            Query query = FirebaseFirestore.getInstance().collectionGroup("Images").whereEqualTo("userID", MainActivity.mAuth.getCurrentUser().getUid())
                    .orderBy("time", Query.Direction.DESCENDING);

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {

                                Upload toAdd = new Upload(document.getTimestamp("time"), document.getString("username"), document.getString("fileName"), document.getString("userID"));
                                uploads.add(toAdd);

                            } catch (Exception e) {

                                String s = e.getMessage() != null ? e.getMessage() : "unable to give more details";
                                Log.d("MyUploadsFragment: ", s);

                            }
                        }
                    }
                    try {
                        adapter.notifyDataSetChanged();
                        if (position != null) {
                            layoutManager.onRestoreInstanceState(position);
                        }
                    } catch (Exception e) {
                        String s = e.getMessage() != null ? e.getMessage() : "unable to give more details";
                        Log.i("MyUploadsFragment: ", s);
                    }
                }
            });
        }
    }

    private void setRecycler(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.uploads_recycler);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new UploadAdapter(uploads, getContext());

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

        if (savedInstanceState != null) {Log.i("onViewStateRestored", "savedInstanceState != null");
            position = savedInstanceState.getParcelable("position");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("position", position);
    }
}

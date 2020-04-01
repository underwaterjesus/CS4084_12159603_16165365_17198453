package ie.ul.discoverlimerick;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
import java.util.concurrent.Semaphore;

public class CategoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private CycleLocationAdapter mAdapter;

    private static CollectionReference db;
    private static ArrayList<MyLocation> locations;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        getLocations();
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        setCycler(view);
        return view;
    }

    private void setCycler(View v){
        recyclerView = (RecyclerView) v.findViewById(R.id.category_cycler);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new CycleLocationAdapter(locations);
        mAdapter.setOnItemClickListener(new CycleLocationAdapter.OnItemClickListner() {
            @Override
            public void onItemClick(int position) {
                MainActivity.showToast(getContext(), locations.get(position).getName());

                Bundle bundle = new Bundle();
                bundle.putSerializable("Location", locations.get(position));

                MainActivity.selected_location = locations.get(position).getId();

                Fragment frag = new LocationFragment();
                frag.setArguments(bundle);

                FragmentTransaction tranny = getActivity().getSupportFragmentManager().beginTransaction();
                tranny.replace(R.id.fragment_container, frag);
                tranny.addToBackStack(null);
                tranny.commit();
            }
        });
        recyclerView.setAdapter(mAdapter);
    }

    private void getLocations() {
        locations = new ArrayList<MyLocation>();
        db = FirebaseFirestore.getInstance().collection(MainActivity.selected_category);
        db.orderBy("name", Query.Direction.ASCENDING);

        db.get().addOnCompleteListener((new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for(QueryDocumentSnapshot document : task.getResult()){
                        try{
                            MyLocation temp = new MyLocation(document.getId(), document.getString("name"), document.getString("address"), document.getString("desc"), document.getDouble("lat"), document.getDouble("lng"));
                            locations.add(temp); Log.d("Testing locations: ", Double.toString(temp.getLat()) + " - " + Double.toString(temp.getLng()));
                        }catch (Exception e){
                            String s = e.getMessage() != null ? e.getMessage() : "unable to give more details";
                            Log.d("Exception Caught: ", s);
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        }));
    }
}
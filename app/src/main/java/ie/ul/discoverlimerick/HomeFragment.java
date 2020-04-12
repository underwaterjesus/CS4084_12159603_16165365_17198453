package ie.ul.discoverlimerick;

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
import android.widget.ImageView;
import android.widget.ViewFlipper;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private CycleAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ViewFlipper viewFlipper;

    private ArrayList<String> imageNames;
    private CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Image Names");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        if(!isConnected())
            startActivity(new Intent(getContext(), ConnectionActivity.class));

        viewFlipper = view.findViewById(R.id.flipper);

        if(viewFlipper != null)
            populateFlipper();

        recyclerView = (RecyclerView) view.findViewById(R.id.category_recycler);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new CycleAdapter(MainActivity.CATEGORIES);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new CycleAdapter.OnItemClickListner() {
            @Override
            public void onItemClick(int position) {
                MainActivity.selected_category = MainActivity.CATEGORIES[position].getCategoryName();
                //MainActivity.showToast(getContext(), MainActivity.CATEGORIES[position].getCategoryName());
                FragmentTransaction tranny = getActivity().getSupportFragmentManager().beginTransaction();
                tranny.replace(R.id.fragment_container, new CategoryFragment());
                tranny.addToBackStack(null);
                tranny.commit();
            }
        });

        return view;
    }

    private void populateFlipper() {
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    imageNames = new ArrayList<>();
                    try {

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            imageNames.add(document.getString("name"));
                        }
                        Collections.shuffle(imageNames);
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference("img");
                        StorageReference fileReference;
                        //boolean[] used = new boolean[imageNames.size()];
                        for (int i = 0; i < 8 && i < imageNames.size(); i++) {
                                fileReference = storageReference.child(imageNames.get(i));
                                ImageView img = new ImageView(getActivity());
                                GlideApp.with(getActivity()).load(fileReference).into(img);
                                viewFlipper.addView(img);
                        }
                    } catch (Exception e) {
                        String s = e.getMessage() == null ? "unable to give more details" : e.getMessage();
                        Log.d("populateFlipper", s);
                    }
                }
                Log.d("HomeFragment", "populateFlipper - " + Integer.toString(imageNames.size()));
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("HomeFragment", "onSaveInstanceState");

        //outState.putStringArrayList("imageNames", imageNames);
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
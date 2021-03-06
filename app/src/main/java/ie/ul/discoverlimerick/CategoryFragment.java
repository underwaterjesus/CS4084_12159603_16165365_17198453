package ie.ul.discoverlimerick;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
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
    private RelativeLayout relativeLayout;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private CycleLocationAdapter mAdapter;

    private Parcelable position;

    private SearchView searchView;

    private static CollectionReference db;
    private static ArrayList<MyLocation> locations;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        if (!isConnected())
            startActivity(new Intent(getContext(), ConnectionActivity.class));

        getLocations(view);

        relativeLayout = view.findViewById(R.id.selected_category_linear_layout);
        searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    mAdapter.getFilter().filter(newText);
                } catch (Exception e) {
                    String s = e.getMessage() == null ? "unable to give more details" : e.getMessage();
                    Log.d("onQueryTextChange", s);
                }

                return false;
            }
        });

        return view;
    }

    private void getLocations(final View v) {
        locations = new ArrayList<MyLocation>();
        db = FirebaseFirestore.getInstance().collection(MainActivity.selected_category);
        //db.orderBy("name", Query.Direction.ASCENDING);

        db.orderBy("name", Query.Direction.ASCENDING).get().addOnCompleteListener((new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                recyclerView = (RecyclerView) v.findViewById(R.id.category_cycler);
                layoutManager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(layoutManager);
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        try {
                            MyLocation temp = new MyLocation(document.getId(), document.getString("name"), document.getString("address"), document.getString("desc"), document.getDouble("lat"), document.getDouble("lng"));
                            locations.add(temp);
                            Log.d("CategoryFragment: ", Double.toString(temp.getLat()) + " - " + Double.toString(temp.getLng()));
                        } catch (Exception e) {
                            String s = e.getMessage() != null ? e.getMessage() : "unable to give more details";
                            Log.d("Exception Caught: ", s);
                        }
                    }
                }
                mAdapter = new CycleLocationAdapter(locations);
                recyclerView.setAdapter(mAdapter);
                mAdapter.setOnItemClickListener(new CycleLocationAdapter.OnItemClickListner() {
                    @Override
                    public void onItemClick(int position) {
                        //MainActivity.showToast(getContext(), locations.get(position).getName());

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Location", locations.get(position));

                        MainActivity.selected_location = locations.get(position).getId();

                        closeKeyboard();

                        Fragment frag = new LocationFragment();
                        frag.setArguments(bundle);

                        FragmentTransaction tranny = getActivity().getSupportFragmentManager().beginTransaction();
                        tranny.replace(R.id.fragment_container, frag);
                        tranny.addToBackStack(null);
                        tranny.commit();
                    }
                });
                try {
                    mAdapter.notifyDataSetChanged();
                    if (position != null) {
                        layoutManager.onRestoreInstanceState(position);
                    }
                    mAdapter.getFilter().filter(searchView.getQuery());
                    if (locations.isEmpty()) {
                        TextView textView = new TextView(getContext());
                        textView.setGravity(Gravity.CENTER);
                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
                        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                        textView.setLayoutParams(lp);
                        textView.setText("Nothing to see here yet!");
                        textView.setTextSize(30);
                        textView.setTypeface(null, Typeface.BOLD);
                        textView.setTextColor(0xffffffff);
                        relativeLayout.addView(textView);
                    }
                } catch (Exception e) {
                    String s = e.getMessage() == null ? "unable to give more details" : e.getMessage();
                    Log.i("setOnItemClickListener", s);
                }
            }
        }));
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            position = recyclerView.getLayoutManager().onSaveInstanceState();
        } catch (Exception e) {
            String s = e.getMessage() == null ? "unable to give more details" : e.getMessage();
            Log.d("onPause", s);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            Log.i("onViewStateRestored", "savedInstanceState != null");
            position = savedInstanceState.getParcelable("position");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("position", position);
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

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();

        if (view != null && view.getWindowToken() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
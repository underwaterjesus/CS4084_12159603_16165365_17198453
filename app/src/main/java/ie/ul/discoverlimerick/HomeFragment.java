package ie.ul.discoverlimerick;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private CycleAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.category_recycler);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new CycleAdapter(MainActivity.CATEGORIES);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new CycleAdapter.OnItemClickListner() {
            @Override
            public void onItemClick(int position) {
                MainActivity.selected_category = MainActivity.CATEGORIES[position].getCategoryName();
                MainActivity.showToast(getContext(), MainActivity.CATEGORIES[position].getCategoryName());
                FragmentTransaction tranny =getActivity().getSupportFragmentManager().beginTransaction();
                tranny.replace(R.id.fragment_container, new CategoryFragment());
                tranny.addToBackStack(null);
                tranny.commit();
            }
        });

        return view;
    }
}
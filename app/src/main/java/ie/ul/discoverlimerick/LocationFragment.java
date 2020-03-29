package ie.ul.discoverlimerick;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;

public class LocationFragment extends Fragment {
    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    private ImageView img;

    private Button leaveReview;
    private Button readReviews;
    private Button viewMap;

    private CollectionReference db;
    private MyLocation location;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        location = (MyLocation)getArguments().getSerializable("Location");

        tv1 = (TextView)view.findViewById(R.id.tv1);
        tv2 = (TextView)view.findViewById(R.id.tv2);
        tv3 = (TextView)view.findViewById(R.id.tv3);

        tv1.setText(location.getName());
        tv2.setText(location.getAddress());
        tv3.setText(location.getDesc());

        return view;
    }
}

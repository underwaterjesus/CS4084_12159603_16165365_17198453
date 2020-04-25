package ie.ul.discoverlimerick;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Locale;

public class LocationFragment extends Fragment {
    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    private ImageView img;

    private Button leaveReview;
    private Button readReviews;
    private Button leaveImage;
    private Button viewImages;
    private Button viewMap;
    public static Button speech;

    private CollectionReference db;
    private FirebaseStorage storage;
    private StorageReference mStorageReference;
    private MyLocation location;

    private final static String PROJECT_URL = "gs://discover-limerick.appspot.com";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        if (!isConnected())
            startActivity(new Intent(getContext(), ConnectionActivity.class));

        location = (MyLocation) getArguments().getSerializable("Location");

        storage = FirebaseStorage.getInstance();
        mStorageReference = storage.getReference();

        String referenceString = "img/" + location.getId() + ".jpg";
        StorageReference imgRef = mStorageReference.child(referenceString);

        img = (ImageView) view.findViewById(R.id.img);
        Glide.with(this).load(imgRef).into(img);

        tv1 = (TextView) view.findViewById(R.id.tv1);
        tv2 = (TextView) view.findViewById(R.id.tv2);
        tv3 = (TextView) view.findViewById(R.id.tv3);

        tv1.setText(location.getName());
        tv2.setText(location.getAddress());
        tv3.setText(location.getDesc());

        leaveReview = (Button) view.findViewById(R.id.leave_review);
        readReviews = (Button) view.findViewById(R.id.read_reviews);
        leaveImage = (Button) view.findViewById(R.id.leave_image);
        viewImages = (Button) view.findViewById(R.id.view_images);
        viewMap = (Button) view.findViewById(R.id.mapBtn);
        speech = (Button) view.findViewById(R.id.speech_button);

        if (!(MainActivity.mTTS == null))
            if (MainActivity.mTTS.isSpeaking())
                speech.setBackgroundResource(R.drawable.speech_filled_stop);
            else
                speech.setBackgroundResource(R.drawable.speech_filled);

        leaveReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLeaveReviewClicked(v);
            }
        });

        readReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReadReviewsClicked(v);
            }
        });

        leaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLeaveImageClicked();
            }
        });

        viewImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewImagesClicked();
            }
        });

        viewMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMapClicked(v);
            }
        });

        speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });

        return view;
    }


    private void onLeaveImageClicked() {
        if (MainActivity.mAuth.getCurrentUser() != null) {
            Fragment frag = new UploadFragment();

            FragmentTransaction tranny = getActivity().getSupportFragmentManager().beginTransaction();
            tranny.replace(R.id.fragment_container, frag);
            tranny.addToBackStack(null);
            tranny.commit();
        } else {
            MainActivity.showToast(getContext(), "Please login to upload an image");
        }
    }

    private void onViewImagesClicked() {
        Fragment frag = new ViewUploadsFragment();

        FragmentTransaction tranny = getActivity().getSupportFragmentManager().beginTransaction();
        tranny.replace(R.id.fragment_container, frag);
        tranny.addToBackStack(null);
        tranny.commit();
    }

    private void onReadReviewsClicked(View v) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("Location", location);

        Fragment frag = new ReadReviewsFragment();
        frag.setArguments(bundle);

        FragmentTransaction tranny = getActivity().getSupportFragmentManager().beginTransaction();
        tranny.replace(R.id.fragment_container, frag);
        tranny.addToBackStack(null);
        tranny.commit();
    }

    private void onLeaveReviewClicked(View v) {
        if (MainActivity.mAuth.getCurrentUser() != null) {

            Bundle bundle = new Bundle();
            bundle.putSerializable("Location", location);

            Fragment frag = new LeaveReviewFragment();
            frag.setArguments(bundle);

            FragmentTransaction tranny = getActivity().getSupportFragmentManager().beginTransaction();
            tranny.replace(R.id.fragment_container, frag);
            tranny.addToBackStack(null);
            tranny.commit();

        } else {

            MainActivity.showToast(getContext(), "Please log in to leave a review");

        }
    }

    private void onMapClicked(View v) {

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());

        if (available == ConnectionResult.SUCCESS) {
            sendToMap();
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, 9001);
            dialog.show();
        } else {
            MainActivity.showToast(getContext(), "Google Play Services issue. Maps unavailable.");
        }

    }

    private void sendToMap() {
        Intent intent = new Intent(getActivity(), ActivityMap.class);

        intent.putExtra("MyLocation", location);

        startActivity(intent);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        Log.d("LocationFragment", "onPause");
        MainActivity.canShow = true;
        if (MainActivity.mTTS.isSpeaking())
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (MainActivity.mTTS.isSpeaking())
                        MainActivity.activitySpeechButton.setVisibility(View.VISIBLE);
                }
            });
        super.onPause();
    }

    private void speak() {
        if (MainActivity.canSpeak) {
            if (MainActivity.mTTS.isSpeaking()) {
                MainActivity.mTTS.stop();
            } else {
                String sayThis = tv3.getText().toString();
                HashMap<String, String> hash = new HashMap<>(1);
                hash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Location Description");
                MainActivity.mTTS.speak(sayThis, TextToSpeech.QUEUE_FLUSH, hash);
            }
        } else {
            MainActivity.showToast(getContext(), "Error: Text to Speech Unavailable");
        }
    }

    @Override
    public void onResume() {
        Log.d("LocationFragment", "onResume");
        MainActivity.canShow = false;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("onResume", "run");
                if (MainActivity.activitySpeechButton.getVisibility() == View.VISIBLE)
                    MainActivity.activitySpeechButton.setVisibility(View.GONE);
            }
        });
        /*MainActivity.hideButton();
        if (MainActivity.mTTS == null) {
            MainActivity.mTTS = new TextToSpeech(getActivity().getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int result = MainActivity.mTTS.setLanguage(Locale.ENGLISH);

                        if (!(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)) {
                            MainActivity.canSpeak = true;
                            speech.setBackgroundResource(R.drawable.speech_filled);
                        }

                        MainActivity.mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String utteranceId) {
                                Log.d("TTS", "onStart");
                                speech.setBackgroundResource(R.drawable.speech_filled_stop);
                            }

                            @Override
                            public void onDone(String utteranceId) {
                                Log.d("TTS", "onDone");
                                MainActivity.hideButton();
                                speech.setBackgroundResource(R.drawable.speech_filled);
                            }

                            @Override
                            public void onError(String utteranceId) {
                                Log.d("TTS", "onError");
                                speech.setBackgroundResource(R.drawable.speech_filled);
                                MainActivity.hideButton();
                            }

                            @Override
                            public void onStop(String utteranceId, boolean interrupted) {
                                Log.d("TTS", "onStop");
                                speech.setBackgroundResource(R.drawable.speech_filled);
                                MainActivity.hideButton();

                                super.onStop(utteranceId, interrupted);
                            }
                        });
                    }
                }
            });
        }*/

        super.onResume();
    }
}

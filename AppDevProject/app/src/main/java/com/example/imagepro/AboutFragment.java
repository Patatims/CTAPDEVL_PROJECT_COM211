package com.example.imagepro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AboutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AboutFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public AboutFragment() {
        // Required empty public constructor
    }

    public static AboutFragment newInstance(String param1, String param2) {
        AboutFragment fragment = new AboutFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        // Initialize views and set text
        TextView happinessDescription = view.findViewById(R.id.happiness_description);
        happinessDescription.setText(getString(R.string.happiness_description));

        TextView happinessTips = view.findViewById(R.id.happiness_tips);
        happinessTips.setText(getString(R.string.happiness_tips));

        TextView sadnessDescription = view.findViewById(R.id.sadness_description);
        sadnessDescription.setText(getString(R.string.sadness_description));

        TextView sadnessTips = view.findViewById(R.id.sadness_tips);
        sadnessTips.setText(getString(R.string.sadness_tips));

        TextView angerDescription = view.findViewById(R.id.anger_description);
        angerDescription.setText(getString(R.string.anger_description));

        TextView angerTips = view.findViewById(R.id.anger_tips);
        angerTips.setText(getString(R.string.anger_tips));

        TextView fearDescription = view.findViewById(R.id.fear_description);
        fearDescription.setText(getString(R.string.fear_description));

        TextView fearTips = view.findViewById(R.id.fear_tips);
        fearTips.setText(getString(R.string.fear_tips));

        TextView surpriseDescription = view.findViewById(R.id.surprise_description);
        surpriseDescription.setText(getString(R.string.surprise_description));

        TextView surpriseTips = view.findViewById(R.id.surprise_tips);
        surpriseTips.setText(getString(R.string.surprise_tips));

        return view;
    }
}

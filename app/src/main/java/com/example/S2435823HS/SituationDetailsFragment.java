package com.example.S2435823HS;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SituationDetailsFragment extends Fragment {

    public SituationDetailsFragment() {
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     *
     */
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_situation_details,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        TextView tvSource = view.findViewById(R.id.tvSource);
        TextView tvType = view.findViewById(R.id.tvType);
        TextView tvComment = view.findViewById(R.id.tvComment);
        TextView tvRoadName = view.findViewById(R.id.tvRoadName);
        TextView tvLocation = view.findViewById(R.id.tvLocation);

        Button btnBack = view.findViewById(R.id.btnBack);

        Bundle args = getArguments();
//catch any unknown/ missing values and replace with default values
        if (args != null) {

            tvSource.setText(
                    args.getString("source", "Unknown source")
            );

            tvType.setText(
                    args.getString("type", "No type provided")
            );

            tvComment.setText(
                    args.getString("comment", "No comment provided")
            );

            tvRoadName.setText(
                    args.getString("roadName", "No road name provided")
            );


            String location = "To:\n" +
                    args.getDouble("toLatitude") + ", " +
                    args.getDouble("toLongitude") +
                    "\n\nFrom:\n" +
                    args.getDouble("fromLatitude") + ", " +
                    args.getDouble("fromLongitude");

            tvLocation.setText(location);
        }

        btnBack.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });
    }
}

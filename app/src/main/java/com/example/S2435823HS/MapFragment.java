package com.example.S2435823HS;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * A Fragment class that implements the main functionality for Google Maps
 */

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private Button btnMapBack;
    private LinearLayout mapLoadingOverlay;

    public MapFragment() {
        // Required empty public constructor
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
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    /**
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        mapLoadingOverlay = view.findViewById(R.id.mapLoadingOverlay);

        btnMapBack = view.findViewById(R.id.btnMapBack);

        btnMapBack.setOnClickListener(v -> {
            androidx.fragment.app.FragmentManager fragmentManager =
                    requireActivity().getSupportFragmentManager();

            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            }

        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();

            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_container, mapFragment)
                    .commit();
        }

        mapFragment.getMapAsync(this);
    }

    /**
     * Called when the map is ready to be used.
     *
     * @param map this function creates the map markers and draws polylines between them
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        googleMap.setOnMarkerClickListener(marker -> {
            PlannedEvents selectedEvent = (PlannedEvents) marker.getTag();

            if (selectedEvent == null) {
                return false;
            }
            Bundle bundle = new Bundle();
            bundle.putString("source", selectedEvent.getSource());
            bundle.putString("type", selectedEvent.getType());
            bundle.putString("comment", selectedEvent.getComment());
            bundle.putString("roadName", selectedEvent.getRoadName());
            bundle.putDouble("toLatitude", selectedEvent.getToLatitude());
            bundle.putDouble("toLongitude", selectedEvent.getToLongitude());
            bundle.putDouble("fromLatitude", selectedEvent.getFromLatitude());
            bundle.putDouble("fromLongitude", selectedEvent.getFromLongitude());

            SituationDetailsFragment detailsFragment = new SituationDetailsFragment();

            detailsFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailsFragment)
                    .addToBackStack(null)
                    .commit();

            return true;
        });

        LatLng scotland = new LatLng(56.4907, -4.2026);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(scotland, 6.5f));

        int markerCount = 0;

        android.util.Log.d("MapFragment", "events Available: " + TrafficData.events.size());

        for (PlannedEvents event : TrafficData.events) {
            double lattitude = event.getToLatitude();
            double longitude = event.getToLongitude();

            if (lattitude == 0 || longitude == 0
                    || lattitude < 49 || lattitude > 61
                    || longitude < -9 || longitude > 3) {
                continue;
            }
            LatLng location = new LatLng(lattitude, longitude);
            float markerColour;

            if ("Unplanned Events".equals(event.getSource())) {
                markerColour = BitmapDescriptorFactory.HUE_RED;
            } else {
                markerColour = BitmapDescriptorFactory.HUE_AZURE;
            }

            if ("Current Roadworks".equals(event.getSource())) {
                double fromLat = event.getFromLatitude();
                double fromLong = event.getFromLongitude();

                double toLat = event.getToLatitude();
                double toLong = event.getToLongitude();

                if (fromLat != 0 && fromLong != 0
                        && toLat != 0 && toLong != 0
                        && fromLat >= 49 && fromLat <= 61
                        && fromLong >= -9 && fromLong <= 3
                        && toLong >= -9 && toLong <= 3 && (fromLat != toLat || fromLong != toLong)) {

                    LatLng fromLocation = new LatLng(fromLat, fromLong);
                    LatLng toLocation = new LatLng(toLat, toLong);

                    googleMap.addPolyline(
                            new PolylineOptions()
                                    .add(fromLocation, toLocation)
                                    .width(7)
                                    .color(android.graphics.Color.BLUE)
                    );
                }
            }


            String roadName = event.getRoadName();

            if (roadName == null || roadName.trim().isEmpty()) {
                roadName = "Unknown road";
            }
            String comment = event.getComment();

            if (comment == null || comment.trim().isEmpty()) {
                comment = "No comment provided";
            }
            com.google.android.gms.maps.model.Marker marker =
                    googleMap.addMarker(
                            new MarkerOptions()
                                    .position(location)
                                    .title(roadName + "-" + event.getSource())
                                    .snippet(comment)
                                    .icon(BitmapDescriptorFactory.defaultMarker(markerColour))
                    );
            if (marker != null) {
                marker.setTag(event);
            }
            markerCount++;
        }
        android.util.Log.d("MapFragment", "Added " + markerCount + " markers");

        if (mapLoadingOverlay != null) {
            mapLoadingOverlay.setVisibility(View.GONE);
        }
    }

    /**
     * Called when the fragment is no longer in use
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        btnMapBack = null;
        mapLoadingOverlay = null;
    }
}
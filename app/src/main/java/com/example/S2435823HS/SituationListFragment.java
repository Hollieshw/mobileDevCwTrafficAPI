package com.example.S2435823HS;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class SituationListFragment extends Fragment {

    // Complete list of downloaded events
    private final List<PlannedEvents> events = new ArrayList<>();

    // List currently displayed after filtering
    private final List<PlannedEvents> filteredEvents = new ArrayList<>();

    private TrafficViewModel trafficViewModel;
    private ListView listView;
    private SearchView searchView;
    private Button btnViewMap;
    private TrafficAdapter adapter;
    private Button btnFindNearby;
    private FusedLocationProviderClient fusedLocationClient;
    private int selectedRadiusKm = 0;
    private boolean nearbyFilterActive = false;
    private double userLatitude;
    private double userLongitude;
    private View loadingContainer;
    private TextView tvNoResults;
    private TextView tvLastUpdated;
    /**
     * ActivityResultLauncher for requesting location permissions.
     */
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        boolean fineGranted = Boolean.TRUE.equals(
                                permissions.get(Manifest.permission.ACCESS_FINE_LOCATION));
                        boolean coarseGranted = Boolean.TRUE.equals(
                                permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                        if (fineGranted || coarseGranted) {
                            getCurrentDeviceLocation();
                        } else {
                            Toast.makeText(requireContext(), "Location permission required for nearby search", Toast.LENGTH_LONG).show();
                        }
                    }
            );

    public SituationListFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_situation_list,
                container,
                false
        );
    }

    /**
     * Called when the fragment's activity has been created
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        listView = view.findViewById(R.id.listView);
        searchView = view.findViewById(R.id.searchView);
        btnViewMap = view.findViewById(R.id.btnViewMap);
        btnFindNearby = view.findViewById(R.id.btnFindNearby);
        loadingContainer = view.findViewById(R.id.loadingContainer);
        tvNoResults = view.findViewById(R.id.tvNoResults);
        tvLastUpdated = view.findViewById(R.id.tvLastUpdated);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
// Handle view map button click
        btnViewMap.setOnClickListener(v -> {

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new MapFragment())
                    .addToBackStack(null)
                    .commit();

        });
// Handle nearby search button click
        btnFindNearby.setOnClickListener(v -> {
            if (nearbyFilterActive) {
                nearbyFilterActive = false;
                btnFindNearby.setText("Find Nearby");

                filterEvents(searchView.getQuery().toString());

                Toast.makeText(requireContext(), "Showing all situations", Toast.LENGTH_SHORT).show();

            } else {
                showRadiusOPtions();
            }
        });

        // Handle search queries
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        filterEvents(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        filterEvents(newText);
                        return true;
                    }
                }
        );

        // Handle clicks on individual situations
        listView.setOnItemClickListener(
                (parent, itemView, position, id) -> {

                    // Get the selected event from the filtered adapter
                    PlannedEvents selectedEvent = adapter.getItem(position);

                    if (selectedEvent == null) {
                        return;
                    }

                    Bundle bundle = new Bundle();

                    bundle.putString("source", selectedEvent.getSource());
                    bundle.putString("type", selectedEvent.getType());
                    bundle.putString("comment", selectedEvent.getComment());
                    bundle.putString("roadName", selectedEvent.getRoadName());

                    bundle.putDouble(
                            "toLatitude", selectedEvent.getToLatitude());
                    bundle.putDouble(
                            "toLongitude", selectedEvent.getToLongitude());

                    bundle.putDouble(
                            "fromLatitude", selectedEvent.getFromLatitude());
                    bundle.putDouble(
                            "fromLongitude", selectedEvent.getFromLongitude());

                    SituationDetailsFragment detailsFragment =
                            new SituationDetailsFragment();

                    detailsFragment.setArguments(bundle);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(
                                    R.id.fragment_container,
                                    detailsFragment
                            )
                            .addToBackStack(null)
                            .commit();
                }
        );

        // Connect to the ViewModel
        trafficViewModel = new ViewModelProvider(this)
                .get(TrafficViewModel.class);

// Observe downloaded traffic situations
        trafficViewModel.getEvents().observe(
                getViewLifecycleOwner(),
                loadedEvents -> {

                    if (loadedEvents == null || listView == null) {
                        return;
                    }

                    // Update the complete event list
                    events.clear();
                    events.addAll(loadedEvents);

                    // Share data with the map
                    TrafficData.events.clear();
                    TrafficData.events.addAll(loadedEvents);

                    // Initially display all situations
                    filteredEvents.clear();
                    filteredEvents.addAll(events);

                    // Create and connect the adapter
                    adapter = new TrafficAdapter(
                            requireContext(),
                            filteredEvents
                    );

                    listView.setAdapter(adapter);

                    // Apply any existing search query
                    filterEvents(searchView.getQuery().toString());

                    if (events.isEmpty()) {
                        Toast.makeText(
                                requireContext(),
                                "No current events found",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

// Observe loading errors
        trafficViewModel.getError().observe(
                getViewLifecycleOwner(),
                errorMessage -> {

                    if (errorMessage != null && isAdded()) {
                        Toast.makeText(
                                requireContext(),
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );

        trafficViewModel.getLoading().observe(
                getViewLifecycleOwner(),
                isLoading -> {

                    if (loadingContainer != null) {
                        loadingContainer.setVisibility(
                                Boolean.TRUE.equals(isLoading)
                                        ? View.VISIBLE
                                        : View.GONE
                        );
                    }
                }
        );


        trafficViewModel.getLastUpdated().observe(
                getViewLifecycleOwner(),
                timestamp -> {
                    if (timestamp != null && tvLastUpdated != null) {

                        SimpleDateFormat sdf = new SimpleDateFormat(
                                "HH:mm:ss",
                                Locale.getDefault()
                        );

                        String formattedTime = sdf.format(new Date(timestamp));

                        tvLastUpdated.setText(
                                "Last updated: " + formattedTime
                        );
                    }
                }
        );
    }

    /**
     * Show a dialog to select the search radius
     */
    private void showRadiusOPtions() {
        String[] radiusOptions = {
                "5 KM",
                "10 KM",
                "25 KM",
                "50 KM"
        };
        int[] radiusValues = {5, 10, 25, 50};

        new AlertDialog.Builder(requireContext())
                .setTitle("Select search Radius")
                .setItems(radiusOptions, (dialog, which) -> {
                    selectedRadiusKm = radiusValues[which];
                    requestLocationPermission();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Request location permissions if not already granted
     */
    private void requestLocationPermission() {

        boolean fineGranted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        boolean coarseGranted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        if (fineGranted || coarseGranted) {

            getCurrentDeviceLocation();

        } else {

            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    /**
     * check if the user has granted location permissions
     * Get the current device location and update userLatitude and userLongitude
     * has failure fallback if unable to retrive data
     */
    private void getCurrentDeviceLocation() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        CancellationTokenSource cancellationTokenSource =
                new CancellationTokenSource();

        fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.getToken()
        ).addOnSuccessListener(location -> {

            if (!isAdded()) {
                return;
            }

            if (location == null) {

                Toast.makeText(
                        requireContext(),
                        "Unable to retrieve your location. Please try again.",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            userLatitude = location.getLatitude();
            userLongitude = location.getLongitude();

            nearbyFilterActive = true;
            btnFindNearby.setText("Show All Situations");

            filterEvents(searchView.getQuery().toString());

            Toast.makeText(
                    requireContext(),
                    "Showing situations within " + selectedRadiusKm + " km",
                    Toast.LENGTH_SHORT
            ).show();

        }).addOnFailureListener(error -> {

            if (isAdded()) {
                Toast.makeText(
                        requireContext(),
                        "Unable to retrieve your location",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    //Check if the event is within the specified radius

    private boolean isWithinRadius(PlannedEvents event) {

        float[] distances = new float[1];
        double toLat = event.getToLatitude();
        double toLong = event.getToLongitude();
        double fromLat = event.getFromLatitude();
        double fromLong = event.getFromLongitude();
        float minimumDistance = Float.MAX_VALUE;

        if (isValidCoordinate(toLat, toLong)) {
            Location.distanceBetween(
                    userLatitude,
                    userLongitude,
                    toLat,
                    toLong,
                    distances
            );
            minimumDistance = Math.min(
                    minimumDistance,
                    distances[0]
            );
        }
        if (isValidCoordinate(fromLat, fromLong)) {
            Location.distanceBetween(
                    userLatitude,
                    userLongitude,
                    fromLat,
                    fromLong,
                    distances
            );
            minimumDistance = Math.min(
                    minimumDistance,
                    distances[0]
            );
        }
        return minimumDistance <= selectedRadiusKm * 1000;
    }

    //check if the coordinates are valid
    private boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180
                && !(latitude == 0 && longitude == 0);
    }


    // Filter events by road name

    private void filterEvents(String query) {

        if (adapter == null) {
            return;
        }

        filteredEvents.clear();

        String searchText = query.trim().toLowerCase();

        for (PlannedEvents event : events) {

            String roadName = event.getRoadName();

            boolean matchesSearch = searchText.isEmpty()
                    || (roadName != null
                    && roadName.toLowerCase().contains(searchText));

            boolean matchesDistance = !nearbyFilterActive
                    || isWithinRadius(event);

            if (matchesSearch && matchesDistance) {
                filteredEvents.add(event);
            }
        }

        adapter.notifyDataSetChanged();
        if (filteredEvents.isEmpty() && !events.isEmpty()) {

            listView.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.VISIBLE);

            if (nearbyFilterActive && !searchText.isEmpty()) {

                tvNoResults.setText(
                        "No situations found matching your search within "
                                + selectedRadiusKm + " km."
                );

            } else if (nearbyFilterActive) {

                tvNoResults.setText(
                        "No incidents or roadworks found within "
                                + selectedRadiusKm + " km."
                );

            } else {

                tvNoResults.setText("No matching situations found.");

            }

        } else {

            listView.setVisibility(View.VISIBLE);
            tvNoResults.setVisibility(View.GONE);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        listView = null;
        searchView = null;
        adapter = null;
        btnViewMap = null;
        btnFindNearby = null;
        fusedLocationClient = null;
        tvNoResults = null;
        tvLastUpdated = null;
        loadingContainer = null;
    }
}
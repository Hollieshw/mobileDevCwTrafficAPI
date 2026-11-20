package com.example.S2435823HS;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Looper;


public class TrafficViewModel extends AndroidViewModel {

    private final TrafficRepository repository;
    private final ExecutorService executor;

    private final MutableLiveData<List<PlannedEvents>> events =
            new MutableLiveData<>();

    private final MutableLiveData<Boolean> loading =
            new MutableLiveData<>(false);

    private final MutableLiveData<String> error =
            new MutableLiveData<>();

    public final MutableLiveData<Long> lastUpdated =
            new MutableLiveData<>();

    private final Handler refresHandler =
            new Handler(Looper.getMainLooper());

    private static final long REFRESH_INTERVAL =
            5 * 60 * 100;
    //refreshes situations after 5 minutes, change refresh interval to change frequency
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadTrafficData();
            refresHandler.postDelayed(this, REFRESH_INTERVAL);
        }
    };

    /**
     * Constructor for the ViewModel
     *
     * @param application
     */
    public TrafficViewModel(@NonNull Application application) {
        super(application);

        repository = new TrafficRepository();
        executor = Executors.newSingleThreadExecutor();

        loadTrafficData();
        refresHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
    }

    // Provides traffic situations to the Fragment
    public LiveData<List<PlannedEvents>> getEvents() {
        return events;
    }

    // Provides the loading state
    public LiveData<Boolean> getLoading() {
        return loading;
    }

    // Provides any loading error
    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Long> getLastUpdated() {
        return lastUpdated;
    }

    // Downloads and parses traffic data in the background
    public void loadTrafficData() {

        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }

        loading.setValue(true);
        error.setValue(null);

        executor.execute(() -> {

            try {
                List<PlannedEvents> result =
                        repository.loadTrafficData();

                events.postValue(result);
                lastUpdated.postValue(System.currentTimeMillis());

            } catch (Exception e) {

                error.postValue("Unable to load traffic data");

            } finally {

                loading.postValue(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        refresHandler.removeCallbacks(refreshRunnable);
        executor.shutdown();
    }
}

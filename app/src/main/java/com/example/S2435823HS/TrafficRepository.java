package com.example.S2435823HS;

import java.util.ArrayList;
import java.util.List;

public class TrafficRepository {
    /**
     * Load traffic data from the API
     * gets credentials and key from config file
     * parses both current and unplanned events using the parser
     * combines events into one Array list
     *
     * @return
     * @throws Exception
     */
    public List<PlannedEvents> loadTrafficData() throws Exception {

        TrafficAPI api = new TrafficAPI();
        PullParser parser = new PullParser();

        // Download both traffic feeds
        String unplannedXml = api.getTrafficData(
                "UnplannedEvents",
                BuildConfig.TRAFFIC_CLIENT_ID,
                BuildConfig.TRAFFIC_CLIENT_SECRET
        );

        String roadworksXml = api.getTrafficData(
                "CurrentRoadworks",
                BuildConfig.TRAFFIC_CLIENT_ID,
                BuildConfig.TRAFFIC_CLIENT_SECRET
        );

        // Parse both XML responses
        List<PlannedEvents> unplannedEvents =
                parser.parse(unplannedXml, "Unplanned Events");

        List<PlannedEvents> roadworksEvents =
                parser.parse(roadworksXml, "Current Roadworks");

        // Combine the results
        List<PlannedEvents> combinedEvents = new ArrayList<>();

        combinedEvents.addAll(unplannedEvents);
        combinedEvents.addAll(roadworksEvents);

        return combinedEvents;
    }
}

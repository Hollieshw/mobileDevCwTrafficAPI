package com.example.S2435823HS;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TrafficAPI {

    /**
     * builds HTTP request to get traffic data from API
     *
     * @param publication
     * @param clientId
     * @param clientKey
     * @return
     * @throws Exception
     */
    public String getTrafficData(String publication, String clientId, String clientKey)
            throws Exception {

        HttpURLConnection connection = null;

        try {
            String apiUrl = "https://datex2.trafficscotland.org/rest/2.3/publications/"
                    + publication + "/Content.xml";
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            String credentials = clientId + ":" + clientKey;

            String encodedCredentials = Base64.getEncoder()
                    .encodeToString(
                            credentials.getBytes(StandardCharsets.UTF_8));

            connection.setRequestProperty(
                    "Authorization", "Basic " + encodedCredentials);

            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception(
                        "API request failed: HTTP " + responseCode);
            }

            StringBuilder xml = new StringBuilder();

            try (InputStream input = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(
                                 input, StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    xml.append(line).append('\n');
                }
            }

            return xml.toString();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}


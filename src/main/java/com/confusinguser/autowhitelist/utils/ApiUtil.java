package com.confusinguser.autowhitelist.utils;

import com.confusinguser.autowhitelist.AutoWhiteList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ApiUtil {

    public static final String BASE_URL = "https://api.hypixel.net/";
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final int REQUEST_RATE = 60; // unit: requests
    private static int allowance = REQUEST_RATE; // unit: requests
    private static long LAST_CHECK = System.currentTimeMillis();
    private static int fails = 0;

    public static String getResponse(String url_string, int cacheTime) {
        // See if request already in cache
        long current = System.currentTimeMillis();

        // rate limiting
        int timePassed = (int) ((current - LAST_CHECK) / 1000);
        LAST_CHECK = current;
        // unit: seconds
        int PER = 60;
        allowance += timePassed * (REQUEST_RATE / PER);
        if (allowance > REQUEST_RATE) {
            allowance = REQUEST_RATE; // throttle
        }
        while (allowance < 1) {
            try {
                //noinspection BusyWait
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // rate limiting
            int timePassedLoop = (int) ((current - LAST_CHECK) / 1000);
            LAST_CHECK = current;
            allowance += timePassed * (REQUEST_RATE / PER);
            if (allowance > REQUEST_RATE) {
                allowance = REQUEST_RATE; // throttle
            }
        }
        allowance -= 1;


        StringBuffer response;
        HttpURLConnection con = null;
        try {
            URL url = new URL(url_string);

            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            response = new StringBuffer();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            fails = 0;
            return response.toString();
        } catch (IOException ioException) {

            int responseCode = -1;
            try {
                if (con != null) responseCode = con.getResponseCode();
            } catch (IOException ex) {
                if (fails > 20) return null;
                fails++;
                if (fails % 10 == 0) {
                    AutoWhiteList.logger.warning("Failed to connect to the SlothPixel API " + fails + " times: " + ex);
                }
            }

            if (responseCode == 429) {
                try {
                    Thread.sleep(17000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                if (fails > 20) return null;
                fails++;
                if (fails % 10 == 0) {
                    AutoWhiteList.logger.warning("Failed to connect to the Hypixel API " + fails + " times: " + ioException);
                }
                try {
                    Thread.sleep((long) Math.min(20, Math.pow(fails, 2)));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return getResponse(url_string, cacheTime);
        }
    }

    public void sendData(String dataString, String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);

            byte[] out = dataString.getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.connect();
            try (OutputStream os = http.getOutputStream()) {
                os.write(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getGuildMembers(String guildId) {
        String response = getResponse(BASE_URL + "guild" + "?id=" + guildId + "&key=" + getKey(), 300000);
        if (response == null) return getGuildMembers(guildId);

        List<String> output = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(response);
        jsonObject = jsonObject.getJSONObject("guild");
        JSONArray members = jsonObject.getJSONArray("members");

        for (int i = 0; i < members.length(); i++) {
            JSONObject currentMember = members.getJSONObject(i);
            String uuid = currentMember.getString("uuid");
            output.add(uuid);
        }

        return output;
    }

    private static String getKey() {
        return AutoWhiteList.instance.getConfigStorage().getConfig().getString("API_Key");
    }
}
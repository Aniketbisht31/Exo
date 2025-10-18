package application;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ExoplanetAPIClient {

    private static final String NASA_URL =
            "https://exoplanetarchive.ipac.caltech.edu/TAP/sync?query=select+pl_name,pl_rade,pl_bmasse,st_dist+from+pscomppars&format=json";

    public JsonObject queryPlanetByName(String name) {
        try {
            URL url = new URL(NASA_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder(); String line;
            while((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            JsonArray arr = JsonParser.parseString(sb.toString()).getAsJsonArray();
            for(JsonElement el: arr) {
                JsonObject obj = el.getAsJsonObject();
                if(obj.get("pl_name").getAsString().equalsIgnoreCase(name)) return obj;
            }
        } catch(Exception e) { e.printStackTrace(); }
        return null;
    }
}

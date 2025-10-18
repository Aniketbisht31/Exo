package application;

import java.io.*;
import java.util.*;

public class ExoplanetManager {
    private static final String FILE_PATH = "data/planets.txt";
    private List<Exoplanet> exoplanets = new ArrayList<>();

    public void addExoplanet(Exoplanet exo) {
        exoplanets.add(exo);
        saveToFile();
    }

    public List<Exoplanet> getAllExoplanets() { return exoplanets; }

    public List<Exoplanet> searchByName(String name) {
        List<Exoplanet> results = new ArrayList<>();
        for (Exoplanet e : exoplanets) {
            if (e.getName().toLowerCase().contains(name.toLowerCase())) results.add(e);
        }
        return results;
    }

    public void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Exoplanet e : exoplanets) pw.println(e.toString());
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void loadFromFile() {
        exoplanets.clear();
        File file = new File(FILE_PATH);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4) {
                    Exoplanet e = new Exoplanet(
                            data[0],
                            Double.parseDouble(data[1]),
                            Double.parseDouble(data[2]),
                            Double.parseDouble(data[3])
                    );
                    exoplanets.add(e);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}

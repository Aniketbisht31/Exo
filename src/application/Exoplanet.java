package application;

public class Exoplanet {
    private String name;
    private double distance; // in AU
    private double radius;   // in Earth radii
    private double temperature;
    private double habitabilityScore;

    public Exoplanet(String name, double distance, double radius, double temperature) {
        this.name = name;
        this.distance = distance;
        this.radius = radius;
        this.temperature = temperature;
        this.habitabilityScore = 0;
    }

    public double model1_simpleHZ(double starLuminosity) {
        double inner = Math.sqrt(starLuminosity / 1.1);
        double outer = Math.sqrt(starLuminosity / 0.53);
        double score = 0;
        if (distance >= inner && distance <= outer) score += 50;
        if (temperature >= 0 && temperature <= 50) score += 50;
        return score;
    }

    public double model2_ESI() {
        double radiusScore = 1 - Math.abs(radius - 1) / (radius + 1);
        double tempScore = 1 - Math.abs(temperature - 288) / (temperature + 288); // 288K = 15°C
        double esi = Math.pow(radiusScore, 0.5) * Math.pow(tempScore, 0.5);
        return esi * 100;
    }

    public double model3_probability(String starType) {
        double score = 0;
        if (starType.equalsIgnoreCase("G")) score += 30;
        else if (starType.equalsIgnoreCase("K")) score += 20;
        else if (starType.equalsIgnoreCase("M")) score += 10;

        if (radius >= 0.8 && radius <= 1.5) score += 30;
        if (temperature >= 0 && temperature <= 50) score += 40;
        return score;
    }

    public void computeFinalHabitability(double starLuminosity, String starType) {
        double s1 = model1_simpleHZ(starLuminosity);
        double s2 = model2_ESI();
        double s3 = model3_probability(starType);
        this.habitabilityScore = (s1 + s2 + s3) / 3;
    }

    public double getHabitabilityScore() { return habitabilityScore; }
    public String getName() { return name; }
    public double getDistance() { return distance; }
    public double getRadius() { return radius; }
    public double getTemperature() { return temperature; }

    @Override
    public String toString() {
        return name + "," + distance + "," + radius + "," + temperature + "," + habitabilityScore;
    }
}

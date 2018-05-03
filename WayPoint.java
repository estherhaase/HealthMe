package com.example.android.healthme;


public class WayPoint {

    private String name;
    private int diva;
    private String platform;

    public WayPoint(String name, int diva, String platform) {
        this.name = name;
        this.diva = diva;
        this.platform = platform;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDiva() {
        return diva;
    }

    public void setDiva(int diva) {
        this.diva = diva;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}

package com.example.android.healthme;



public class JourneyLeg {

    private int legTime;
    private WayPoint[] points;
    private WayPoint[] stopSeq;

    public JourneyLeg(int legTime, WayPoint[] points, WayPoint[] stopSeq) {
        this.legTime = legTime;
        this.points = points;
        this.stopSeq = stopSeq;
    }

    public void setLegTime(int legTime) {
        this.legTime = legTime;
    }

    public void setPoints(WayPoint[] points) {
        this.points = points;
    }

    public void setStopSeq(WayPoint[] stopSeq) {
        this.stopSeq = stopSeq;
    }

    public int getLegTime() {

        return legTime;
    }

    public WayPoint[] getPoints() {
        return points;
    }

    public WayPoint[] getStopSeq() {
        return stopSeq;
    }



}

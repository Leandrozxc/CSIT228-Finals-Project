package com.example.classsync.model;

public class Evaluation {
    private final String id;
    private final User   evaluator;
    private final User   evaluated;
    private final String groupId;
    private int    effort;      // 0–5
    private int    reliability; // 0–5
    private int    quality;     // 0–5
    private int    overall;     // 0–5
    private String notes;

    public Evaluation(String id, User evaluator, User evaluated, String groupId,
                      int effort, int reliability, int quality, int overall, String notes) {
        this.id          = id;
        this.evaluator   = evaluator;
        this.evaluated   = evaluated;
        this.groupId     = groupId;
        this.effort      = effort;
        this.reliability = reliability;
        this.quality     = quality;
        this.overall     = overall;
        this.notes       = notes;
    }

    public String getId()          { return id; }
    public User   getEvaluator()   { return evaluator; }
    public User   getEvaluated()   { return evaluated; }
    public String getGroupId()     { return groupId; }
    public int    getEffort()      { return effort; }
    public int    getReliability() { return reliability; }
    public int    getQuality()     { return quality; }
    public int    getOverall()     { return overall; }
    public String getNotes()       { return notes; }
}
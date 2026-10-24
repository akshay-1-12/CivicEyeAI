package com.civiceye.app.models;

import java.io.Serializable;

public class Complaint implements Serializable {
    private int id;
    private String complaint_code;
    private String issue_type;
    private String status;
    private String address;
    private String description;
    private String severity;
    private String created_at;
    private String image_url;
    private String department;

    public Complaint() {}

    public int getId() { return id; }
    public String getComplaintCode() { return complaint_code; }
    public String getIssueType() { return issue_type; }
    public String getStatus() { return status; }
    public String getAddress() { return address; }
    public String getDescription() { return description; }
    public String getSeverity() { return severity; }
    public String getCreatedAt() { return created_at; }
    public String getImageUrl() { return image_url; }
    public String getDepartment() { return department; }

    public void setStatus(String status) { this.status = status; }
}

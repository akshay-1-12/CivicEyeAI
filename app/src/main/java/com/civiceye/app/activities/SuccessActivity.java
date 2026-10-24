package com.civiceye.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.civiceye.app.R;

public class SuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        Intent intent = getIntent();
        String complaintId = intent.getStringExtra("complaint_id");
        String officialEmail = intent.getStringExtra("official");
        String department = intent.getStringExtra("department");
        String address = intent.getStringExtra("address");
        boolean emailSent = intent.getBooleanExtra("email_sent", false);

        // 1. Display Complaint ID
        TextView tvId = findViewById(R.id.tvComplaintId);
        tvId.setText(complaintId != null ? complaintId : "CEA-UNKNOWN");

        // 2. Official Info
        TextView tvOfficial = findViewById(R.id.tvOfficial);
        if (officialEmail != null && !officialEmail.isEmpty() && !officialEmail.equals("none")) {
            tvOfficial.setText("Official notified: " + officialEmail);
        } else {
            tvOfficial.setText("Official: Assigned to " + (department != null ? department : "Local Authority"));
        }

        // 3. Email Status
        TextView tvEmailStatus = findViewById(R.id.tvEmailStatus);
        if (emailSent) {
            tvEmailStatus.setText("Email status: Sent Successfully ✅");
            tvEmailStatus.setTextColor(getColor(R.color.primary));
        } else {
            tvEmailStatus.setText("Email status: Pending/Internal Only");
        }

        // 4. Action: Call Official
        findViewById(R.id.btnCall).setOnClickListener(v -> {
            // Using your testing number as default, in production this would come from a database/API
            String phone = "9284044635";
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phone));
            startActivity(callIntent);
        });

        // 5. Action: Email Official
        findViewById(R.id.btnEmail).setOnClickListener(v -> {
            String targetEmail = (officialEmail != null && !officialEmail.equals("none")) ? officialEmail : "akshaydodke800@gmail.com";
            Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
            mailIntent.setData(Uri.parse("mailto:"));
            mailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{targetEmail});
            mailIntent.putExtra(Intent.EXTRA_SUBJECT, "Complaint " + complaintId + ": Action Required");
            mailIntent.putExtra(Intent.EXTRA_TEXT, "Hello,\n\nI am reporting an issue via CivicEye.\nComplaint ID: " + complaintId + "\nDepartment: " + department + "\nLocation: " + address);
            
            try {
                startActivity(Intent.createChooser(mailIntent, "Send Email..."));
            } catch (Exception e) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

        // 6. Action: Search for Real Local Official
        findViewById(R.id.btnSearchOfficial).setOnClickListener(v -> {
            String query = (department != null ? department : "Municipal Corporation") + " contact number " + (address != null ? address : "");
            String url = "https://www.google.com/search?q=" + Uri.encode(query);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });

        // 7. Navigation
        findViewById(R.id.btnTrack).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        findViewById(R.id.tvGoHome).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }
}

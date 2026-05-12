package com.municipal.service;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.municipal.model.Complaint;
import com.municipal.repository.ComplaintRepository;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private EmailService emailService;

    public double distance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000;
    }

    public Complaint saveComplaint(Complaint complaint, String ipAddress) {

        complaint.setStatus("PENDING");
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setIpAddress(ipAddress);

        String id = "MUN-" + UUID.randomUUID().toString().substring(0, 8);
        complaint.setComplaintId(id);

        List<Complaint> all = complaintRepository.findAll();

        for (Complaint c : all) {

            if (c.getLatitude() == null || c.getLongitude() == null) {
                continue;
            }
            if (complaint.getCategory() == null || c.getCategory() == null) {
                continue;
            }

            if (!c.getCategory().equalsIgnoreCase(complaint.getCategory())) {
                continue;
            }

            double dist = distance(
                    complaint.getLatitude(), complaint.getLongitude(),
                    c.getLatitude(), c.getLongitude()
            );

            if (dist > 10) {
                continue;
            }

            if ("SOLVED".equalsIgnoreCase(c.getStatus())) {
                if (c.getCreatedAt() != null
                        && c.getCreatedAt().isBefore(LocalDateTime.now().minusDays(10))) {
                    continue;
                }
            }

            complaint.setDuplicate(true);
            complaint.setParentComplaintId(c.getComplaintId());
            complaint.setStatus(c.getStatus());
            break;
        }

        Complaint saved = complaintRepository.save(complaint);

        try {
            if (saved.getEmail() != null && !saved.getEmail().isEmpty()) {
                emailService.sendComplaintEmail(saved.getEmail(), saved.getComplaintId());
            }
        } catch (Exception e) {
            System.out.println("Email failed");
        }

        return saved;
    }

    // 🔥 MARK SPAM
    public void markAsSpam(String complaintId, String reason) {
        Complaint c = complaintRepository.findByComplaintId(complaintId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        c.setSpam(true);
        c.setSpamReason(reason);

        complaintRepository.save(c);
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    public Map<String, Object> getStats() {

        long total = complaintRepository.count();
        long pending = complaintRepository.countByStatus("PENDING");
        long solved = complaintRepository.countByStatus("SOLVED");
        long spam = complaintRepository.countBySpamTrue();

        double accuracy = total > 0 ? (solved * 100.0) / total : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("solved", solved);
        stats.put("spam", spam);
        stats.put("accuracy", Math.round(accuracy * 100.0) / 100.0);

        return stats;
    }

    public Complaint addFeedback(String complaintId, String feedback) {
        Complaint c = complaintRepository.findByComplaintId(complaintId).orElseThrow();

        if (c.isDuplicate()) {
            Complaint parent = complaintRepository
                    .findByComplaintId(c.getParentComplaintId()).orElseThrow();

            String existing = parent.getFeedback();
            parent.setFeedback(existing == null ? feedback : existing + " | " + feedback);

            return complaintRepository.save(parent);
        }

        c.setFeedback(feedback);
        return complaintRepository.save(c);
    }

    public Complaint markSolved(Long id, Complaint update) {

        Complaint parent = complaintRepository.findById(id).orElseThrow();
        parent.setStatus("SOLVED");

        if (update.getResolvedImageUrl() != null) {
            parent.setResolvedImageUrl(update.getResolvedImageUrl());
        }

        complaintRepository.save(parent);

        List<Complaint> all = complaintRepository.findAll();

        for (Complaint c : all) {
            if (c.getParentComplaintId() != null
                    && c.getParentComplaintId().equals(parent.getComplaintId())) {

                c.setStatus("SOLVED");
                c.setResolvedImageUrl(parent.getResolvedImageUrl());
                complaintRepository.save(c);
            }
        }

        return parent;
    }

    public Complaint getByComplaintId(String complaintId) {
        return complaintRepository.findByComplaintId(complaintId).orElse(null);
    }
}

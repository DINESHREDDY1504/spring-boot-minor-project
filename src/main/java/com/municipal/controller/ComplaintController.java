package com.municipal.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.municipal.model.Complaint;
import com.municipal.service.ComplaintService;
import com.municipal.service.EmailService;
import com.municipal.service.SmsService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "*")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    // ✅ NEW: Cloudinary injection
    @Autowired
    private Cloudinary cloudinary;

    // 🔹 Upload Complaint (MODIFIED)
    @PostMapping("/upload")
    public Complaint uploadComplaint(
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("image") MultipartFile file,
            HttpServletRequest request) throws Exception {

        // 🔥 Upload to Cloudinary (instead of local storage)
        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.emptyMap()
        );

        String imageUrl = uploadResult.get("secure_url").toString();

        String ipAddress = request.getRemoteAddr();

        Complaint complaint = new Complaint();
        complaint.setCategory(category);
        complaint.setDescription(description);
        complaint.setLatitude(latitude);
        complaint.setLongitude(longitude);
        complaint.setImageUrl(imageUrl); // ✅ Cloudinary URL

        return complaintService.saveComplaint(complaint, ipAddress);
    }

    // 🔹 SMS (UNCHANGED)
    @PostMapping("/sendSMS")
    public String sendSMS(@RequestBody Map<String, String> body) {

        String phone = body.get("phone");
        String complaintId = body.get("complaintId");

        smsService.sendSMS(phone, complaintId);

        return "SMS sent successfully";
    }

    // 🔹 Email (UNCHANGED)
    @PostMapping("/sendEmail")
    public String sendEmail(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String complaintId = body.get("complaintId");

        emailService.sendComplaintEmail(email, complaintId);

        return "Email sent successfully";
    }

    // 🔹 Get All Complaints (UNCHANGED)
    @GetMapping("/all")
    public List<Complaint> getAllComplaints() {
        return complaintService.getAllComplaints();
    }

    // 🔹 Stats (UNCHANGED)
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return complaintService.getStats();
    }

    // 🔹 Feedback (UNCHANGED)
    @PutMapping("/feedback/{id}")
    public Complaint addFeedback(@PathVariable String id,
            @RequestBody Map<String, String> body) {

        return complaintService.addFeedback(id, body.get("feedback"));
    }

    // 🔹 Solve Complaint (MODIFIED)
    @PutMapping(value = "/solve/{id}", consumes = "multipart/form-data")
    public Complaint solveComplaint(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile file) throws Exception {

        // 🔥 Upload resolved image to Cloudinary
        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.emptyMap()
        );

        String imageUrl = uploadResult.get("secure_url").toString();

        Complaint update = new Complaint();
        update.setResolvedImageUrl(imageUrl); // ✅ Cloudinary URL

        return complaintService.markSolved(id, update);
    }

    // 🔹 Tracking (UNCHANGED)
    @GetMapping("/{complaintId}")
    public Complaint getComplaintById(@PathVariable String complaintId) {

        Complaint c = complaintService.getByComplaintId(complaintId);

        if (c != null && c.isDuplicate()) {

            Complaint parent = complaintService.getByComplaintId(c.getParentComplaintId());

            if (parent != null) {

                c.setStatus(parent.getStatus());
                c.setResolvedImageUrl(parent.getResolvedImageUrl());
                c.setFeedback(parent.getFeedback());
            }
        }

        return c;
    }
}

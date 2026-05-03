let stream;
let capturedBlob = null;

document.addEventListener("DOMContentLoaded", function () {

    const form = document.getElementById("complaintForm");

    if (form) {
        form.addEventListener("submit", function (e) {
            e.preventDefault();

            const submitBtn = form.querySelector("button");
            submitBtn.disabled = true;
            submitBtn.innerText = "Submitting...";

            navigator.geolocation.getCurrentPosition(function (position) {

                let category = document.getElementById("category").value;
                let description = document.getElementById("description").value;
                let imageInput = document.getElementById("image");

                // 🔥 USE CAMERA OR FILE
                let imageFile = capturedBlob
                    ? new File([capturedBlob], "photo.jpg", { type: "image/jpeg" })
                    : imageInput.files[0];

                // 🔴 Validation
                if (!category || !description || !imageFile) {
                    alert("Please fill all fields and capture/select image");
                    resetButton(submitBtn);
                    return;
                }

                // ⚠️ Prevent 413 error
                if (imageFile.size > 5 * 1024 * 1024) {
                    alert("Image too large (max 5MB)");
                    resetButton(submitBtn);
                    return;
                }

                let formData = new FormData();
                formData.append("category", category);
                formData.append("description", description);
                formData.append("latitude", position.coords.latitude);
                formData.append("longitude", position.coords.longitude);
                formData.append("image", imageFile);

                fetch("https://spring-boot-minor-project.onrender.com/api/complaints/upload", {
                    method: "POST",
                    body: formData
                })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error("Server error: " + response.status);
                        }
                        return response.json();
                    })
                    .then(result => {

                        let msg = document.getElementById("message");

                        if (!result.complaintId) {
                            msg.innerHTML = "<h3 style='color:red'>❌ Complaint ID not received</h3>";
                            return;
                        }

                        msg.innerHTML = `
                        <h3 style="color:green">✅ Complaint Registered Successfully!</h3>

                        <p><b>Complaint ID:</b> ${result.complaintId}</p>

                        <h4>📩 Get Notification</h4>

                        <input type="email" id="userEmail" placeholder="Enter your email">
                        <button onclick="sendEmail('${result.complaintId}')">Send Email</button>

                        <br><br>

                        <button onclick="sendWhatsApp('${result.complaintId}')">
                            📱 WhatsApp Notification
                        </button>

                        <br><br>

                        <button onclick="goTrack('${result.complaintId}')">
                            🔍 Track Complaint
                        </button>
                    `;

                        form.reset();
                        capturedBlob = null;
                    })
                    .catch(error => {
                        console.error("❌ Error:", error);
                        alert("❌ Failed to submit complaint.");
                    })
                    .finally(() => {
                        resetButton(submitBtn);
                    });

            }, function () {
                alert("⚠ Please allow location access.");
                resetButton(submitBtn);
            });

        });
    }

});

// 🔁 Reset Button
function resetButton(btn) {
    btn.disabled = false;
    btn.innerText = "Submit Complaint";
}

// 📷 Open Camera
function openCamera() {
    navigator.mediaDevices.getUserMedia({ video: true })
        .then(s => {
            stream = s;
            const video = document.getElementById("camera");
            video.srcObject = stream;
            video.style.display = "block";
        })
        .catch(() => alert("Camera not accessible"));
}

// 📸 Capture Photo
function capturePhoto() {

    const video = document.getElementById("camera");
    const canvas = document.getElementById("canvas");
    const preview = document.getElementById("preview");

    if (!video.videoWidth) {
        alert("Camera not ready. Click Open Camera first.");
        return;
    }

    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;

    const ctx = canvas.getContext("2d");
    ctx.drawImage(video, 0, 0);

    canvas.toBlob(blob => {

        if (!blob) {
            alert("Capture failed");
            return;
        }

        capturedBlob = blob;

        // 🔥 SHOW PREVIEW
        preview.src = URL.createObjectURL(blob);
        preview.style.display = "block";

    }, "image/jpeg", 0.8);

    stopCameraManually(); // auto close
}

// ❌ Stop Camera (FIXED)
function stopCameraManually() {
    if (stream) {
        stream.getTracks().forEach(track => track.stop());
        stream = null;
    }
    document.getElementById("camera").style.display = "none";
}

// 🔙 Navigation
function goBack() {
    if (window.history.length > 1) {
        window.history.back();
    } else {
        window.location.href = "index.html";
    }
}
function cancelCapture() {

    // 🔥 Stop camera if running
    if (stream) {
        stream.getTracks().forEach(track => track.stop());
        stream = null;
    }

    // 🔥 Hide camera
    const video = document.getElementById("camera");
    if (video) {
        video.style.display = "none";
        video.srcObject = null;
    }

    // 🔥 Clear preview
    const preview = document.getElementById("preview");
    if (preview) {
        preview.src = "";
        preview.style.display = "none";
    }

    // 🔥 Clear captured blob
    capturedBlob = null;

    // 🔥 Clear file input
    const fileInput = document.getElementById("image");
    if (fileInput) {
        fileInput.value = "";
    }

    // alert("Capture cancelled");
}

function goHome() {
    window.location.href = "index.html";
}

// 📧 Email
function sendEmail(id) {
    let email = document.getElementById("userEmail").value;

    if (!email) {
        alert("Enter email");
        return;
    }

    // 🔥 get button and disable it
    const btn = event.target;
    btn.disabled = true;
    btn.innerText = "Sending...";

    fetch("https://spring-boot-minor-project.onrender.com/api/complaints/sendEmail", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ email, complaintId: id })
    })
        .then(res => res.text()) // 🔥 important
        .then(data => {
            if (data === "SUCCESS" || data.includes("success")) {
                alert("✅ Email sent successfully");
            } else {
                alert("⚠ Email may not have been sent");
            }
        })
        .catch(err => {
            console.error(err);
            alert("❌ Failed to send email");
        })
        .finally(() => {
            btn.disabled = false;
            btn.innerText = "Send Email";
        });
}

// 📱 WhatsApp
function sendWhatsApp(id) {
    let msg = encodeURIComponent(
        "Your complaint has been registered.\nComplaint ID: " + id
    );
    window.open(`https://wa.me/?text=${msg}`, "_blank");
}

// 🔍 Track redirect
function goTrack(id) {
    window.location.href = "track.html?id=" + id;
}
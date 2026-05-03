document.addEventListener("DOMContentLoaded", function () {

    const form = document.getElementById("complaintForm");

    if (form) {
        form.addEventListener("submit", function (e) {
            e.preventDefault();

            const submitBtn = form.querySelector("button");
            submitBtn.disabled = true;
            submitBtn.innerText = "Submitting...";

            // 🔥 Get location
            navigator.geolocation.getCurrentPosition(function (position) {

                let category = document.getElementById("category").value;
                let description = document.getElementById("description").value;
                let imageInput = document.getElementById("image");
                let imageFile = imageInput.files[0];

                // 🔴 Validation
                if (!category || !description || !imageFile) {
                    alert("Please fill all fields and select an image");
                    resetButton(submitBtn);
                    return;
                }

                let formData = new FormData();
                formData.append("category", category);
                formData.append("description", description);
                formData.append("latitude", position.coords.latitude);
                formData.append("longitude", position.coords.longitude);
                formData.append("image", imageFile);

                // 🔥 API CALL
                fetch("http://localhost:8080/api/complaints/upload", {
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

                        if (!msg) {
                            alert("UI Error: message div not found");
                            return;
                        }

                        if (!result.complaintId) {
                            msg.innerHTML = "<h3 style='color:red'>❌ Complaint ID not received</h3>";
                            return;
                        }

                        // 🔥 NEW UI (POST SUBMISSION EMAIL)
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
                    `;

                        form.reset();
                    })
                    .catch(error => {
                        console.error("❌ Error:", error);
                        alert("❌ Failed to submit complaint. Check backend.");
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

// 🔥 REAL EMAIL API CALL
function sendEmail(id) {

    let email = document.getElementById("userEmail").value;

    if (!email) {
        alert("Enter email");
        return;
    }

    fetch("http://localhost:8080/api/complaints/sendEmail", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            email: email,
            complaintId: id
        })
    })
        .then(() => alert("✅ Email sent successfully"))
        .catch(() => alert("❌ Failed to send email"));
}

// 📱 WhatsApp (REAL CLICK)
function sendWhatsApp(id) {

    let msg = encodeURIComponent(
        "Your complaint has been registered.\nComplaint ID: " + id
    );

    window.open(`https://wa.me/?text=${msg}`, "_blank");
}
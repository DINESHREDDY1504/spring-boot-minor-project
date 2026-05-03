document.addEventListener("DOMContentLoaded", function () {

    const form = document.getElementById("complaintForm");

    if (form) {
        form.addEventListener("submit", function (e) {
            e.preventDefault();

            // Disable button to prevent multiple clicks
            const submitBtn = form.querySelector("button");
            submitBtn.disabled = true;
            submitBtn.innerText = "Submitting...";

            // Get user location
            navigator.geolocation.getCurrentPosition(function (position) {

                let category = document.getElementById("category").value;
                let description = document.getElementById("description").value;
                let imageInput = document.getElementById("image");
                let imageFile = imageInput.files[0];

                // Validation
                if (!category || !description || !imageFile) {
                    alert("Please fill all fields and select an image");
                    submitBtn.disabled = false;
                    submitBtn.innerText = "Submit Complaint";
                    return;
                }

                // Create FormData
                let formData = new FormData();
                formData.append("category", category);
                formData.append("description", description);
                formData.append("latitude", position.coords.latitude);
                formData.append("longitude", position.coords.longitude);
                formData.append("image", imageFile);

                fetch("/api/complaints/upload", {
                    method: "POST",
                    body: formData
                })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error("Server error");
                        }
                        return response.json();
                    })
                    .then(result => {

                        // Success UI
                        document.getElementById("message").innerHTML =
                            "<h3>✅ Complaint Registered Successfully!</h3>" +
                            "<p><strong>Complaint ID:</strong> " + result.complaintId + "</p>" +
                            "<p>Select notification option:</p>" +
                            "<button onclick=\"fakeEmail('" + result.complaintId + "')\">📧 Email</button> " +
                            "<button onclick=\"fakeWhatsApp('" + result.complaintId + "')\">📱 WhatsApp</button>";

                        form.reset();
                    })
                    .catch(error => {
                        console.error("Error:", error);
                        alert("❌ Something went wrong!");
                    })
                    .finally(() => {
                        submitBtn.disabled = false;
                        submitBtn.innerText = "Submit Complaint";
                    });

            }, function () {
                alert("⚠ Please allow location access.");
                submitBtn.disabled = false;
                submitBtn.innerText = "Submit Complaint";
            });

        });
    }

});

// ✅ Demo Email Notification
function fakeEmail(id) {
    alert(
        "📧 Email Notification (Demo)\n\n" +
        "Your complaint has been registered successfully.\n" +
        "Complaint ID: " + id
    );
}

// ✅ Demo WhatsApp Notification
function fakeWhatsApp(id) {
    alert(
        "📱 WhatsApp Notification (Demo)\n\n" +
        "Complaint submitted successfully!\n" +
        "Complaint ID: " + id
    );
}
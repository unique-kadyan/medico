import api from "./api";

const razorpayService = {
  // Create a Razorpay order for a medicine order
  createOrder: async (orderId) => {
    const response = await api.post(`/razorpay/create-order/${orderId}`);
    return response.data;
  },

  // Verify payment after successful Razorpay checkout
  verifyPayment: async (paymentData) => {
    const response = await api.post("/razorpay/verify-payment", paymentData);
    return response.data;
  },

  // Get payment status
  getPaymentStatus: async (razorpayOrderId) => {
    const response = await api.get(`/razorpay/status/${razorpayOrderId}`);
    return response.data;
  },

  // Load Razorpay script dynamically
  loadRazorpayScript: () => {
    return new Promise((resolve, reject) => {
      if (window.Razorpay) {
        resolve(true);
        return;
      }

      const script = document.createElement("script");
      script.src = "https://checkout.razorpay.com/v1/checkout.js";
      script.async = true;
      script.onload = () => resolve(true);
      script.onerror = () => reject(new Error("Failed to load Razorpay SDK"));
      document.body.appendChild(script);
    });
  },

  // Initialize and open Razorpay checkout
  openCheckout: async (orderData, callbacks = {}) => {
    await razorpayService.loadRazorpayScript();

    const options = {
      key: orderData.keyId,
      amount: orderData.amount,
      currency: orderData.currency || "INR",
      name: "Medico Hospital",
      description: `Payment for Order #${orderData.medicineOrderId}`,
      order_id: orderData.orderId,
      prefill: {
        name: orderData.patientName || "",
        email: orderData.patientEmail || "",
        contact: orderData.patientPhone || "",
      },
      notes: {
        medicine_order_id: orderData.medicineOrderId,
      },
      theme: {
        color: "#1976d2",
      },
      handler: async (response) => {
        try {
          // Verify payment on backend
          const verificationResult = await razorpayService.verifyPayment({
            razorpay_order_id: response.razorpay_order_id,
            razorpay_payment_id: response.razorpay_payment_id,
            razorpay_signature: response.razorpay_signature,
          });

          if (callbacks.onSuccess) {
            callbacks.onSuccess(verificationResult);
          }
        } catch (error) {
          if (callbacks.onError) {
            callbacks.onError(error);
          }
        }
      },
      modal: {
        ondismiss: () => {
          if (callbacks.onDismiss) {
            callbacks.onDismiss();
          }
        },
        escape: true,
        backdropclose: false,
      },
    };

    const razorpay = new window.Razorpay(options);

    razorpay.on("payment.failed", (response) => {
      if (callbacks.onError) {
        callbacks.onError({
          code: response.error.code,
          description: response.error.description,
          source: response.error.source,
          step: response.error.step,
          reason: response.error.reason,
        });
      }
    });

    razorpay.open();
  },
};

export default razorpayService;

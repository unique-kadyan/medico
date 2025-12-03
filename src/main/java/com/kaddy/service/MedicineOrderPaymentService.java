package com.kaddy.service;

import com.kaddy.dto.MedicineOrderPaymentDTO;
import com.kaddy.dto.RecordPaymentRequest;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.MedicineOrder;
import com.kaddy.model.MedicineOrderPayment;
import com.kaddy.model.User;
import com.kaddy.model.enums.PaymentStatus;
import com.kaddy.repository.MedicineOrderPaymentRepository;
import com.kaddy.repository.MedicineOrderRepository;
import com.kaddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedicineOrderPaymentService {

    private final MedicineOrderPaymentRepository paymentRepository;
    private final MedicineOrderRepository orderRepository;
    private final UserRepository userRepository;

    public MedicineOrderPaymentDTO recordPayment(RecordPaymentRequest request, Long receivedByUserId) {
        log.info("Recording payment for order ID: {}", request.getOrderId());

        MedicineOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medicine order not found with ID: " + request.getOrderId()));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Order is already fully paid");
        }

        BigDecimal totalPaid = paymentRepository.getTotalPaidForOrder(order.getId());
        if (totalPaid == null) {
            totalPaid = BigDecimal.ZERO;
        }

        BigDecimal remainingAmount = order.getFinalAmount().subtract(totalPaid);
        if (request.getAmount().compareTo(remainingAmount) > 0) {
            throw new IllegalArgumentException("Payment amount (" + request.getAmount() +
                    ") exceeds remaining balance (" + remainingAmount + ")");
        }

        User receivedBy = userRepository.findById(receivedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + receivedByUserId));

        MedicineOrderPayment payment = new MedicineOrderPayment();
        payment.setReceiptNumber(generateReceiptNumber());
        payment.setOrder(order);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setTransactionId(request.getTransactionId());
        payment.setCardLastFourDigits(request.getCardLastFourDigits());
        payment.setBankName(request.getBankName());
        payment.setChequeNumber(request.getChequeNumber());
        payment.setChequeDate(request.getChequeDate());
        payment.setUpiId(request.getUpiId());
        payment.setNotes(request.getNotes());
        payment.setReceivedBy(receivedBy);

        MedicineOrderPayment savedPayment = paymentRepository.save(payment);

        // Update order payment status
        BigDecimal newTotalPaid = totalPaid.add(request.getAmount());
        if (newTotalPaid.compareTo(order.getFinalAmount()) >= 0) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }
        orderRepository.save(order);

        log.info("Payment {} of {} recorded for order {}", savedPayment.getReceiptNumber(),
                request.getAmount(), order.getOrderNumber());
        return convertToDTO(savedPayment);
    }

    @Transactional(readOnly = true)
    public MedicineOrderPaymentDTO getPaymentById(Long paymentId) {
        log.info("Fetching payment with ID: {}", paymentId);
        MedicineOrderPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        return convertToDTO(payment);
    }

    @Transactional(readOnly = true)
    public MedicineOrderPaymentDTO getPaymentByReceiptNumber(String receiptNumber) {
        log.info("Fetching payment with receipt number: {}", receiptNumber);
        MedicineOrderPayment payment = paymentRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with receipt: " + receiptNumber));
        return convertToDTO(payment);
    }

    @Transactional(readOnly = true)
    public List<MedicineOrderPaymentDTO> getPaymentsByOrderId(Long orderId) {
        log.info("Fetching payments for order ID: {}", orderId);
        return paymentRepository.findActivePaymentsByOrderId(orderId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineOrderPaymentDTO> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching payments between {} and {}", startDate, endDate);
        return paymentRepository.findPaymentsBetweenDates(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineOrderPaymentDTO> getPaymentsByUser(Long userId) {
        log.info("Fetching payments received by user ID: {}", userId);
        return paymentRepository.findByReceivedByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidForOrder(Long orderId) {
        BigDecimal total = paymentRepository.getTotalPaidForOrder(orderId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getRemainingBalanceForOrder(Long orderId) {
        MedicineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine order not found with ID: " + orderId));
        BigDecimal totalPaid = getTotalPaidForOrder(orderId);
        return order.getFinalAmount().subtract(totalPaid);
    }

    public MedicineOrderPaymentDTO refundPayment(Long paymentId, BigDecimal refundAmount, String reason,
            Long refundedByUserId) {
        log.info("Processing refund for payment ID: {}", paymentId);

        MedicineOrderPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        if (payment.getIsRefunded()) {
            throw new IllegalStateException("Payment is already fully refunded");
        }

        BigDecimal maxRefund = payment.getAmount().subtract(payment.getRefundedAmount());
        if (refundAmount.compareTo(maxRefund) > 0) {
            throw new IllegalArgumentException("Refund amount exceeds available amount");
        }

        User refundedBy = userRepository.findById(refundedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + refundedByUserId));

        payment.setRefundedAmount(payment.getRefundedAmount().add(refundAmount));
        payment.setRefundReason(reason);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundedBy(refundedBy);

        if (payment.getRefundedAmount().compareTo(payment.getAmount()) >= 0) {
            payment.setIsRefunded(true);
        }

        // Update order payment status if needed
        MedicineOrder order = payment.getOrder();
        BigDecimal totalPaid = getTotalPaidForOrder(order.getId());
        BigDecimal effectivePaid = totalPaid.subtract(refundAmount);

        if (effectivePaid.compareTo(BigDecimal.ZERO) <= 0) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        } else if (effectivePaid.compareTo(order.getFinalAmount()) < 0) {
            order.setPaymentStatus(PaymentStatus.PENDING);
        }
        orderRepository.save(order);

        log.info("Refund of {} processed for payment {}", refundAmount, payment.getReceiptNumber());
        return convertToDTO(paymentRepository.save(payment));
    }

    public MedicineOrderPaymentDTO verifyPayment(Long paymentId, Long verifiedByUserId) {
        log.info("Verifying payment ID: {}", paymentId);

        MedicineOrderPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        if (payment.getIsVerified()) {
            throw new IllegalStateException("Payment is already verified");
        }

        User verifiedBy = userRepository.findById(verifiedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + verifiedByUserId));

        payment.setIsVerified(true);
        payment.setVerifiedBy(verifiedBy);
        payment.setVerifiedAt(LocalDateTime.now());

        log.info("Payment {} verified by user {}", payment.getReceiptNumber(), verifiedByUserId);
        return convertToDTO(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentSummary(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalCollection = paymentRepository.getTotalCollectionBetweenDates(startDate, endDate);
        List<Object[]> breakdown = paymentRepository.getPaymentMethodBreakdown(startDate, endDate);

        Map<String, BigDecimal> methodBreakdown = new HashMap<>();
        for (Object[] row : breakdown) {
            methodBreakdown.put(row[0].toString(), (BigDecimal) row[1]);
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCollection", totalCollection != null ? totalCollection : BigDecimal.ZERO);
        summary.put("paymentMethodBreakdown", methodBreakdown);
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);

        return summary;
    }

    private String generateReceiptNumber() {
        String prefix = "MOP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        String lastNumber = paymentRepository.findLastReceiptNumber(prefix);

        int nextNumber = 1;
        if (lastNumber != null) {
            String numPart = lastNumber.substring(prefix.length());
            nextNumber = Integer.parseInt(numPart) + 1;
        }

        return prefix + String.format("%05d", nextNumber);
    }

    private MedicineOrderPaymentDTO convertToDTO(MedicineOrderPayment payment) {
        MedicineOrderPaymentDTO dto = new MedicineOrderPaymentDTO();
        dto.setId(payment.getId());
        dto.setReceiptNumber(payment.getReceiptNumber());
        dto.setOrderId(payment.getOrder().getId());
        dto.setOrderNumber(payment.getOrder().getOrderNumber());
        dto.setPatientId(payment.getOrder().getPatient().getId());
        dto.setPatientName(payment.getOrder().getPatient().getUser().getFirstName() + " " +
                payment.getOrder().getPatient().getUser().getLastName());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setTransactionId(payment.getTransactionId());
        dto.setCardLastFourDigits(payment.getCardLastFourDigits());
        dto.setBankName(payment.getBankName());
        dto.setChequeNumber(payment.getChequeNumber());
        dto.setChequeDate(payment.getChequeDate());
        dto.setUpiId(payment.getUpiId());
        dto.setIsRefunded(payment.getIsRefunded());
        dto.setRefundedAmount(payment.getRefundedAmount());
        dto.setRefundedAt(payment.getRefundedAt());
        dto.setRefundReason(payment.getRefundReason());
        dto.setNotes(payment.getNotes());
        dto.setIsVerified(payment.getIsVerified());
        dto.setVerifiedAt(payment.getVerifiedAt());

        if (payment.getReceivedBy() != null) {
            dto.setReceivedById(payment.getReceivedBy().getId());
            dto.setReceivedByName(payment.getReceivedBy().getFirstName() + " " + payment.getReceivedBy().getLastName());
        }

        if (payment.getRefundedBy() != null) {
            dto.setRefundedById(payment.getRefundedBy().getId());
            dto.setRefundedByName(payment.getRefundedBy().getFirstName() + " " + payment.getRefundedBy().getLastName());
        }

        if (payment.getVerifiedBy() != null) {
            dto.setVerifiedById(payment.getVerifiedBy().getId());
            dto.setVerifiedByName(payment.getVerifiedBy().getFirstName() + " " + payment.getVerifiedBy().getLastName());
        }

        return dto;
    }
}

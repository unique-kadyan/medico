package com.kaddy.service;

import com.kaddy.dto.CreateMedicineOrderRequest;
import com.kaddy.dto.MedicineOrderDTO;
import com.kaddy.dto.MedicineOrderItemDTO;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.*;
import com.kaddy.model.enums.MedicineOrderStatus;
import com.kaddy.model.enums.PaymentStatus;
import com.kaddy.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedicineOrderService {

    private final MedicineOrderRepository orderRepository;
    private final MedicineOrderItemRepository orderItemRepository;
    private final PatientRepository patientRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicationRepository medicationRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final UserRepository userRepository;

    public MedicineOrderDTO createOrder(CreateMedicineOrderRequest request) {
        log.info("Creating new medicine order for patient ID: {}", request.getPatientId());

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Patient not found with ID: " + request.getPatientId()));

        MedicineOrder order = new MedicineOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setPatient(patient);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(MedicineOrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setContactPhone(request.getContactPhone());
        order.setNotes(request.getNotes());

        if (request.getDiscountAmount() != null) {
            order.setDiscountAmount(request.getDiscountAmount());
        }

        if (request.getPrescriptionId() != null) {
            Prescription prescription = prescriptionRepository.findById(request.getPrescriptionId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Prescription not found with ID: " + request.getPrescriptionId()));
            order.setPrescription(prescription);
        }

        MedicineOrder savedOrder = orderRepository.save(order);

        for (CreateMedicineOrderRequest.CreateMedicineOrderItemRequest itemRequest : request.getItems()) {
            Medication medication = medicationRepository.findById(itemRequest.getMedicationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Medication not found with ID: " + itemRequest.getMedicationId()));

            if (medication.getStockQuantity() < itemRequest.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for medication: " + medication.getName());
            }

            MedicineOrderItem item = new MedicineOrderItem();
            item.setOrder(savedOrder);
            item.setMedication(medication);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(medication.getUnitPrice());
            item.setDosage(itemRequest.getDosage());
            item.setFrequency(itemRequest.getFrequency());
            item.setDuration(itemRequest.getDuration());
            item.setInstructions(itemRequest.getInstructions());

            if (itemRequest.getPrescriptionItemId() != null) {
                PrescriptionItem prescriptionItem = prescriptionItemRepository
                        .findById(itemRequest.getPrescriptionItemId())
                        .orElse(null);
                item.setPrescriptionItem(prescriptionItem);
            }

            orderItemRepository.save(item);
            savedOrder.getItems().add(item);
        }

        savedOrder.calculateTotals();
        savedOrder = orderRepository.save(savedOrder);

        log.info("Medicine order created successfully with order number: {}", savedOrder.getOrderNumber());
        return convertToDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public MedicineOrderDTO getOrderById(Long id) {
        log.info("Fetching medicine order with ID: {}", id);
        MedicineOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine order not found with ID: " + id));
        return convertToDTO(order);
    }

    @Transactional(readOnly = true)
    public MedicineOrderDTO getOrderByNumber(String orderNumber) {
        log.info("Fetching medicine order with number: {}", orderNumber);
        MedicineOrder order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Medicine order not found with number: " + orderNumber));
        return convertToDTO(order);
    }

    @Transactional(readOnly = true)
    public List<MedicineOrderDTO> getAllOrders() {
        log.info("Fetching all active medicine orders");
        return orderRepository.findAllActiveOrders().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineOrderDTO> getOrdersByPatientId(Long patientId) {
        log.info("Fetching medicine orders for patient ID: {}", patientId);
        return orderRepository.findByPatientId(patientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineOrderDTO> getOrdersByPatientUserId(Long userId) {
        log.info("Fetching medicine orders for user ID: {}", userId);
        return orderRepository.findByPatientUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineOrderDTO> getOrdersByPrescriptionId(Long prescriptionId) {
        log.info("Fetching medicine orders for prescription ID: {}", prescriptionId);
        return orderRepository.findByPrescriptionId(prescriptionId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineOrderDTO> getOrdersByStatus(MedicineOrderStatus status) {
        log.info("Fetching medicine orders by status: {}", status);
        return orderRepository.findActiveOrdersByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineOrderDTO> getOrdersByPaymentStatus(PaymentStatus paymentStatus) {
        log.info("Fetching medicine orders by payment status: {}", paymentStatus);
        return orderRepository.findByPaymentStatus(paymentStatus).stream()
                .filter(order -> order.getActive())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MedicineOrderDTO updateOrderStatus(Long orderId, MedicineOrderStatus status, Long processedById) {
        log.info("Updating medicine order {} status to: {}", orderId, status);

        MedicineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine order not found with ID: " + orderId));

        order.setStatus(status);

        if (processedById != null && order.getProcessedBy() == null) {
            User processedBy = userRepository.findById(processedById)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + processedById));
            order.setProcessedBy(processedBy);
            order.setProcessedDate(LocalDateTime.now());
        }

        if (status == MedicineOrderStatus.CONFIRMED) {
            for (MedicineOrderItem item : order.getItems()) {
                Medication medication = item.getMedication();
                medication.setStockQuantity(medication.getStockQuantity() - item.getQuantity());
                medicationRepository.save(medication);
            }
        }

        if (status == MedicineOrderStatus.DELIVERED) {
            order.setDeliveryDate(LocalDateTime.now());
        }

        MedicineOrder updatedOrder = orderRepository.save(order);
        log.info("Medicine order {} status updated to: {}", orderId, status);
        return convertToDTO(updatedOrder);
    }

    public MedicineOrderDTO updatePaymentStatus(Long orderId, PaymentStatus paymentStatus) {
        log.info("Updating medicine order {} payment status to: {}", orderId, paymentStatus);

        MedicineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine order not found with ID: " + orderId));

        order.setPaymentStatus(paymentStatus);
        MedicineOrder updatedOrder = orderRepository.save(order);

        log.info("Medicine order {} payment status updated to: {}", orderId, paymentStatus);
        return convertToDTO(updatedOrder);
    }

    public MedicineOrderDTO cancelOrder(Long orderId) {
        log.info("Cancelling medicine order: {}", orderId);

        MedicineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine order not found with ID: " + orderId));

        if (order.getStatus() == MedicineOrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel a delivered order");
        }

        if (order.getStatus() == MedicineOrderStatus.CONFIRMED ||
                order.getStatus() == MedicineOrderStatus.PROCESSING ||
                order.getStatus() == MedicineOrderStatus.READY_FOR_PICKUP) {
            for (MedicineOrderItem item : order.getItems()) {
                Medication medication = item.getMedication();
                medication.setStockQuantity(medication.getStockQuantity() + item.getQuantity());
                medicationRepository.save(medication);
            }
        }

        order.setStatus(MedicineOrderStatus.CANCELLED);
        MedicineOrder cancelledOrder = orderRepository.save(order);

        log.info("Medicine order {} cancelled successfully", orderId);
        return convertToDTO(cancelledOrder);
    }

    public void deleteOrder(Long orderId) {
        log.info("Deleting medicine order: {}", orderId);

        MedicineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine order not found with ID: " + orderId));

        order.setActive(false);
        orderRepository.save(order);
        log.info("Medicine order {} deactivated successfully", orderId);
    }

    @Transactional(readOnly = true)
    public long countOrdersByStatus(MedicineOrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "ORD-" + timestamp + "-" + random;
    }

    private MedicineOrderDTO convertToDTO(MedicineOrder order) {
        MedicineOrderDTO dto = new MedicineOrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setPatientId(order.getPatient().getId());
        dto.setPatientName(order.getPatient().getFirstName() + " " + order.getPatient().getLastName());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setFinalAmount(order.getFinalAmount());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setContactPhone(order.getContactPhone());
        dto.setNotes(order.getNotes());
        dto.setProcessedDate(order.getProcessedDate());
        dto.setDeliveryDate(order.getDeliveryDate());

        if (order.getPrescription() != null) {
            dto.setPrescriptionId(order.getPrescription().getId());
            dto.setPrescriptionNumber(order.getPrescription().getPrescriptionNumber());
        }

        if (order.getProcessedBy() != null) {
            dto.setProcessedById(order.getProcessedBy().getId());
            dto.setProcessedByName(order.getProcessedBy().getFirstName() + " " + order.getProcessedBy().getLastName());
        }

        List<MedicineOrderItemDTO> itemDTOs = order.getItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }

    private MedicineOrderItemDTO convertItemToDTO(MedicineOrderItem item) {
        MedicineOrderItemDTO dto = new MedicineOrderItemDTO();
        dto.setId(item.getId());
        dto.setOrderId(item.getOrder().getId());
        dto.setMedicationId(item.getMedication().getId());
        dto.setMedicationName(item.getMedication().getName());
        dto.setMedicationCode(item.getMedication().getMedicationCode());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.getSubtotal());
        dto.setDosage(item.getDosage());
        dto.setFrequency(item.getFrequency());
        dto.setDuration(item.getDuration());
        dto.setInstructions(item.getInstructions());

        if (item.getPrescriptionItem() != null) {
            dto.setPrescriptionItemId(item.getPrescriptionItem().getId());
        }

        return dto;
    }
}

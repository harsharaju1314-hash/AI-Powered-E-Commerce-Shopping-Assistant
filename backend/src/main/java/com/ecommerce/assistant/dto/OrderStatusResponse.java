package com.ecommerce.assistant.dto;

import java.time.Instant;

public class OrderStatusResponse {
    private String orderNumber;
    private String status;
    private Instant estimatedDelivery;
    private String trackingDescription;

    public OrderStatusResponse() {}

    public OrderStatusResponse(String orderNumber, String status, Instant estimatedDelivery, String trackingDescription) {
        this.orderNumber = orderNumber;
        this.status = status;
        this.estimatedDelivery = estimatedDelivery;
        this.trackingDescription = trackingDescription;
    }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(Instant estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
    public String getTrackingDescription() { return trackingDescription; }
    public void setTrackingDescription(String trackingDescription) { this.trackingDescription = trackingDescription; }
}

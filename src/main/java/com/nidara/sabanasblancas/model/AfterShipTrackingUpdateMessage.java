package com.nidara.sabanasblancas.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Optional;

public class AfterShipTrackingUpdateMessage {

    private static final String DELIVERED_STATE = "Delivered";

    private String tag;
    @JsonProperty("updated_at")
    private Date updatedAt;
    @JsonProperty("order_id")
    private String orderId;
    @JsonProperty("tracking_number")
    private String trackingNumber;

    @JsonProperty("order_id_path")
    private String orderIdPath;

    public AfterShipTrackingUpdateMessage() {
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Optional<String> getNumericOrderId() {
        if (orderId == null) {
            return Optional.empty();
        }
        try {
            Integer.parseInt(orderId);
            return Optional.of(orderId);

        } catch (NumberFormatException e) {
            return getOrderIdFromOrderPath();
        }
    }

    private Optional<String> getOrderIdFromOrderPath() {
        // https://www.mipresta.com/es/index.php?controller=order-detail&id_order=12191
        if (orderIdPath==null || orderIdPath.isEmpty()) {
            return Optional.empty();
        }
        String queryString = orderIdPath.substring(orderIdPath.indexOf("?") + 1);
        String[] params = queryString.split("&");
        for (String param : params) {
            String [] keyValue = param.split("=");
            if ("id_order".equals(keyValue[0])) {
                return Optional.of(keyValue[1]);
            }
        }
        return Optional.empty();
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public boolean isDelivered() {
        return getTag().equalsIgnoreCase(DELIVERED_STATE);
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getOrderIdPath() {
        return orderIdPath;
    }

    public void setOrderIdPath(String orderIdPath) {
        this.orderIdPath = orderIdPath;
    }

    public String toString() {
        return "El pedido " + getNumericOrderId().orElse("???") + " (" + trackingNumber + ") ha cambiado a estado: " + tag;
    }
}

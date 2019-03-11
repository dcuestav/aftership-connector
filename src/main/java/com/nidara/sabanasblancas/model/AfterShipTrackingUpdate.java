package com.nidara.sabanasblancas.model;

public class AfterShipTrackingUpdate {

    private String event;
    private AfterShipTrackingUpdateMessage msg;

    public AfterShipTrackingUpdate() {
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public AfterShipTrackingUpdateMessage getMsg() {
        return msg;
    }

    public void setMsg(AfterShipTrackingUpdateMessage msg) {
        this.msg = msg;
    }
}

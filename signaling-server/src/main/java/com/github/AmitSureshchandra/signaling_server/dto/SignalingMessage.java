package com.github.AmitSureshchandra.signaling_server.dto;

public class SignalingMessage {
    private String type;  // "offer", "answer", "candidate"
    private String sdp;
    private String candidate;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSdp() { return sdp; }
    public void setSdp(String sdp) { this.sdp = sdp; }

    public String getCandidate() { return candidate; }
    public void setCandidate(String candidate) { this.candidate = candidate; }
}

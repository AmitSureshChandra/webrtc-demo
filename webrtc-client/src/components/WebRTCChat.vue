<template>
  <div>
    <h2>WebRTC Video Chat</h2>
    <video ref="localVideo" autoplay playsinline></video>
    <video ref="remoteVideo" autoplay playsinline></video>
    <button @click="startCall">Start Call</button>
  </div>
</template>

<script>
const SIGNALING_SERVER_URL = import.meta.env.VITE_WS_URL + "/ws";

export default {
  data() {
    return {
      ws: null, // WebSocket connection
      localStream: null,
      peerConnection: null,
      configuration: { iceServers: [{ urls: "stun:stun.l.google.com:19302" }] },
    };
  },
  methods: {
    async startCall() {
      console.log({SIGNALING_SERVER_URL})
      this.ws = new WebSocket(SIGNALING_SERVER_URL);
      this.ws.onmessage = this.handleSignalingMessage;

      // Get webcam and microphone
      this.localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
      this.$refs.localVideo.srcObject = this.localStream;

      // Create PeerConnection
      this.peerConnection = new RTCPeerConnection(this.configuration);
      this.localStream.getTracks().forEach(track => this.peerConnection.addTrack(track, this.localStream));

      this.peerConnection.onicecandidate = event => {
        if (event.candidate) {
          this.sendMessage({ type: "candidate", candidate: event.candidate });
        }
      };

      this.peerConnection.ontrack = event => {
        this.$refs.remoteVideo.srcObject = event.streams[0];
      };

      // Create SDP Offer
      const offer = await this.peerConnection.createOffer();
      await this.peerConnection.setLocalDescription(offer);
      this.sendMessage({ type: "offer", sdp: offer.sdp });
    },

    async handleSignalingMessage(event) {
      const message = JSON.parse(event.data);

      if (message.type === "offer") {
        // Remote offer received
        this.peerConnection = new RTCPeerConnection(this.configuration);
        this.localStream.getTracks().forEach(track => this.peerConnection.addTrack(track, this.localStream));

        this.peerConnection.onicecandidate = event => {
          if (event.candidate) {
            this.sendMessage({ type: "candidate", candidate: event.candidate });
          }
        };

        this.peerConnection.ontrack = event => {
          this.$refs.remoteVideo.srcObject = event.streams[0];
        };

        await this.peerConnection.setRemoteDescription(new RTCSessionDescription({ type: "offer", sdp: message.sdp }));
        const answer = await this.peerConnection.createAnswer();
        await this.peerConnection.setLocalDescription(answer);
        this.sendMessage({ type: "answer", sdp: answer.sdp });
      }

      if (message.type === "answer") {
        // Remote answer received
        await this.peerConnection.setRemoteDescription(new RTCSessionDescription({ type: "answer", sdp: message.sdp }));
      }

      if (message.type === "candidate") {
        // ICE Candidate received
        await this.peerConnection.addIceCandidate(new RTCIceCandidate(message.candidate));
      }
    },

    sendMessage(message) {
      this.ws.send(JSON.stringify(message));
    }
  }
};
</script>

<style scoped>
video {
  width: 45%;
  margin: 10px;
  border: 2px solid black;
}
</style>

<template>
    <div>
      <div v-if="roomId" style="position: relative;">
        <section>
            <button @click="startCall">Start Call</button>
        </section>
        <section>
        <video ref="remoteVideo" autoplay playsinline style="border: 1px solid wheat; width: 90vw; margin: 5vw;"></video>
    </section>
    <section style="position: absolute; top: 0; right: 0;">
        <video ref="localVideo" autoplay playsinline style="border: 1px solid wheat; width: 15vw; margin: 10vw;"></video>
    </section>
  </div>
  <div v-else style="text-align: left; padding: 20px;">
    <p> Want to create meeting 
      <button @click="createRoom">Create Room</button>
    </p>

    <p @submit.prevent> Want to join meeting
      <button @click="joinRoom">Join Room</button>
    </p>
    
  </div>
    </div>
  
  
</template>

<script>
const SIGNALING_SERVER_URL = import.meta.env.VITE_WS_URL + "/ws";

export default {
  created() {
      this.roomId = window.location.href.split("/").pop();
      console.log(this.roomId)

      // if roomId is not null join the room automatically
  },
  data() {
    return {
      roomCreated: false,
      roomId: null,
      ws: null, // WebSocket connection
      localStream: null,
      peerConnection: null,
      configuration: { iceServers: [{ urls: "stun:stun.l.google.com:19302" }] },
    };
  },
  methods: {
    joinRoom() {
      let roomId = prompt("Enter Room ID");
      window.location.href = window.location.href + roomId;
    },
    createRoom() {

      // fetch room id from server
      // this.roomId = "1234";
      let roomId = 1234;

      window.location.href = window.location.href + roomId;

    },
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

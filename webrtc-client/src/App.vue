<template>
  <div style="height: 100vh;text-align: center;padding-top: 10px;">
    <div>
      <h1>Video Meet</h1>
      <WebRTCChat />
    </div>
  </div>
</template>

<script>
import WebRTCChat from "./components/WebRTCChat.vue";

export default {
  components: { WebRTCChat }
};
</script>

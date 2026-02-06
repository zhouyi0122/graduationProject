<template>
  <span class="font-bold text-orange-500">{{ timeRemaining }}</span>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

const props = defineProps({
  targetTimestamp: {
    type: Number,
    required: true,
  },
});

const timeRemaining = ref('');
let intervalId = null;

const updateCountdown = () => {
  const now = Date.now();
  const threeDaysInMillis = 3 * 24 * 60 * 60 * 1000;
  const endTime = props.targetTimestamp + threeDaysInMillis;
  const distance = endTime - now;

  if (distance < 0) {
    timeRemaining.value = '00:00:00';
    clearInterval(intervalId);
    return;
  }

  const days = Math.floor(distance / (1000 * 60 * 60 * 24));
  const hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
  const seconds = Math.floor((distance % (1000 * 60)) / 1000);

  const totalHours = days * 24 + hours;

  timeRemaining.value = `${String(totalHours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
};

onMounted(() => {
  updateCountdown();
  intervalId = setInterval(updateCountdown, 1000);
});

onUnmounted(() => {
  clearInterval(intervalId);
});
</script>

<template>
  <span class="inline-flex items-center cursor-pointer hover:text-orange-500" @click.prevent.stop="toggleFavorite">
    <component :is="isFavorited ? HeartIconSolid : HeartIconOutline" class="h-4 w-4 mr-0.5" :class="{ 'text-orange-500': isFavorited }" />
    <span>{{ favoriteCount }}</span>
  </span>
</template>

<script setup>
import { ref } from 'vue';
import { HeartIcon as HeartIconOutline } from '@heroicons/vue/24/outline';
import { HeartIcon as HeartIconSolid } from '@heroicons/vue/24/solid';

const props = defineProps({
  initialCount: {
    type: Number,
    default: 0
  }
});

const isFavorited = ref(false);
const favoriteCount = ref(props.initialCount);

const toggleFavorite = () => {
    isFavorited.value = !isFavorited.value;
    if (isFavorited.value) {
        favoriteCount.value++;
    } else {
        favoriteCount.value--;
    }
};
</script>

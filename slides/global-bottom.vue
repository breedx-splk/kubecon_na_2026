<!-- global-bottom.vue -->
<template>
  <footer 
    v-if="$nav.currentLayout !== 'cover'" 
    class="absolute bottom-0 left-0 right-0 p-4 flex justify-between text-xs text-gray-400"
  >
    <div>
      <div class="flex items-center gap-2">
        <img src="./opentelemetry_logo.png" width="20px" class="float-left mr-2 w-5"/>
        OpenTelemetry In Your Pocket
      </div>
    </div>
    <div>{{ $nav.currentPage }} / {{ $nav.total }}</div>
  </footer>
</template>
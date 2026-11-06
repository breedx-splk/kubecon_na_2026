<template>
  <footer
    class="pointer-events-none absolute inset-x-0 bottom-0 z-20 flex items-end justify-between px-5 pb-4 text-xs text-gray-400"
  >
    <div class="flex items-center gap-2">
      <img
        src="./opentelemetry_logo.png"
        width="42"
        height="42"
        alt="OpenTelemetry logo"
      />
    </div>
    <svg
      class="h-11 w-11"
      viewBox="0 0 48 48"
      role="img"
      aria-label="Android mascot"
    >
      <g fill="none" stroke="#3ddc84" stroke-linecap="round" stroke-width="2.5">
        <path d="m16 12-4-6" />
        <path d="m32 12 4-6" />
      </g>
      <path fill="#3ddc84" d="M10 24a14 14 0 0 1 28 0v2H10z" />
      <circle cx="18" cy="20" r="1.5" fill="#111" />
      <circle cx="30" cy="20" r="1.5" fill="#111" />
      <rect x="10" y="27" width="28" height="12" rx="3" fill="#3ddc84" />
      <rect x="4" y="27" width="5" height="12" rx="2.5" fill="#3ddc84" />
      <rect x="39" y="27" width="5" height="12" rx="2.5" fill="#3ddc84" />
      <rect x="15" y="38" width="6" height="8" rx="3" fill="#3ddc84" />
      <rect x="27" y="38" width="6" height="8" rx="3" fill="#3ddc84" />
    </svg>
  </footer>
</template>

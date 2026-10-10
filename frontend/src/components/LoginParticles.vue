<template>
  <div ref="surface" class="login-particles" aria-hidden="true"></div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { createLoginParticleOptions } from '@/utils/login-particles'

const surface = ref()
let container
let engine
let reducedMotion
let mobile
let disposed = false
let revision = 0

async function refreshParticles() {
  const currentRevision = ++revision
  container?.destroy()
  container = undefined
  const colors = getComputedStyle(surface.value)
  try {
    const next = await engine.load({
      element: surface.value,
      options: createLoginParticleOptions({
        brand: colors.getPropertyValue('--color-brand').trim(),
        accent: colors.getPropertyValue('--color-navigation-muted').trim(),
        mobile: mobile.matches,
        reducedMotion: reducedMotion.matches
      })
    })
    // Imports and canvas initialization can finish after navigating away.
    if (disposed || currentRevision !== revision) {
      next?.destroy()
      return
    }
    container = next
    if (reducedMotion.matches) {
      container?.pause()
      container?.draw(true)
    }
  } catch {
    // Keep the solid theme background if canvas initialization is unavailable.
  }
}

onMounted(async () => {
  reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)')
  mobile = window.matchMedia('(max-width: 900px)')
  try {
    const { tsParticles } = await import('@tsparticles/engine')
    const { loadSlim } = await import('@tsparticles/slim')
    if (disposed) return
    await loadSlim(tsParticles)
    if (disposed) return
    engine = tsParticles
    reducedMotion.addEventListener('change', refreshParticles)
    mobile.addEventListener('change', refreshParticles)
    await refreshParticles()
  } catch {
    // Login remains usable when the optional animation cannot load.
  }
})

onBeforeUnmount(() => {
  disposed = true
  revision++
  reducedMotion?.removeEventListener('change', refreshParticles)
  mobile?.removeEventListener('change', refreshParticles)
  container?.destroy()
})
</script>

<style scoped>
.login-particles {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.login-particles :deep(canvas) {
  display: block;
}
</style>

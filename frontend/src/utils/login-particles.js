export function createLoginParticleOptions({ brand, accent, mobile = false, reducedMotion = false }) {
  return {
    autoPlay: !reducedMotion,
    fullScreen: { enable: false },
    fpsLimit: 30,
    detectRetina: false,
    pauseOnBlur: true,
    pauseOnOutsideViewport: true,
    particles: {
      number: { value: mobile ? 24 : 56, density: { enable: false } },
      color: { value: [brand, accent] },
      shape: { type: 'circle' },
      opacity: { value: { min: 0.3, max: 0.65 } },
      size: { value: { min: 1, max: 2.5 } },
      links: { enable: true, color: brand, distance: mobile ? 120 : 165, opacity: 0.25, width: 1 },
      move: { enable: !reducedMotion, speed: 0.45, outModes: { default: 'bounce' } }
    },
    interactivity: {
      detectsOn: 'window',
      events: { onHover: { enable: !mobile && !reducedMotion, mode: 'grab' }, onClick: { enable: false } },
      modes: { grab: { distance: 180, links: { opacity: 0.45 } } }
    }
  }
}

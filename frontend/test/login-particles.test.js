import test from 'node:test'
import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'

const optionsUrl = new URL('../src/utils/login-particles.js', import.meta.url)
const read = (path) => readFileSync(new URL(path, import.meta.url), 'utf8')

test('particle options use supplied theme colors and bound the animation workload', async () => {
  assert.ok(existsSync(optionsUrl), 'login particle options must exist')
  const { createLoginParticleOptions } = await import(optionsUrl.href)
  const options = createLoginParticleOptions({ brand: '#245bdb', accent: '#8f9db6', mobile: false, reducedMotion: false })
  assert.equal(options.fullScreen.enable, false)
  assert.equal(options.fpsLimit, 30)
  assert.equal(options.pauseOnBlur, true)
  assert.equal(options.pauseOnOutsideViewport, true)
  assert.deepEqual(options.particles.color.value, ['#245bdb', '#8f9db6'])
  assert.equal(options.particles.links.color, '#245bdb')
  assert.equal(options.interactivity.events.onClick.enable, false)
  assert.equal(options.interactivity.events.onHover.enable, true)
  assert.ok(options.particles.number.value <= 60)
})

test('mobile and reduced-motion settings reduce particles and disable interactive movement', async () => {
  assert.ok(existsSync(optionsUrl), 'login particle options must exist')
  const { createLoginParticleOptions } = await import(optionsUrl.href)
  const colors = { brand: '#245bdb', accent: '#8f9db6' }
  const desktop = createLoginParticleOptions(colors)
  const mobile = createLoginParticleOptions({ ...colors, mobile: true })
  const reduced = createLoginParticleOptions({ ...colors, reducedMotion: true })
  assert.ok(mobile.particles.number.value < desktop.particles.number.value)
  assert.equal(mobile.interactivity.events.onHover.enable, false)
  assert.equal(reduced.particles.move.enable, false)
  assert.equal(reduced.interactivity.events.onHover.enable, false)
  assert.equal(reduced.autoPlay, false)
})

test('login and navigation share background and brand tokens without a local palette override', () => {
  const login = read('../src/views/Login.vue')
  const layout = read('../src/layout/Layout.vue')
  const styles = read('../src/styles.css')
  assert.match(styles, /--color-navigation-bg:\s*#111827/)
  assert.match(styles, /--color-brand:\s*#245bdb/)
  assert.match(login, /<LoginParticles\s*\/>/)
  assert.match(login, /--portal-blue:\s*var\(--color-brand\)/)
  assert.match(login, /background:\s*var\(--color-navigation-bg\)/)
  assert.match(layout, /background:\s*var\(--color-navigation-bg\)/)
  assert.doesNotMatch(layout, /rgba\(79,\s*70,\s*229/)
  assert.doesNotMatch(login, /--el-color-primary:\s*#/)
})

test('particle component is decorative, lazy loaded and destroys its container on exit', () => {
  const url = new URL('../src/components/LoginParticles.vue', import.meta.url)
  assert.ok(existsSync(url), 'login particle component must exist')
  const component = readFileSync(url, 'utf8')
  assert.match(component, /aria-hidden="true"/)
  assert.match(component, /import\('@tsparticles\/slim'\)/)
  assert.match(component, /getComputedStyle/)
  assert.match(component, /prefers-reduced-motion/)
  assert.match(component, /onBeforeUnmount/)
  assert.match(component, /\.destroy\(/)
  assert.match(component, /pointer-events:\s*none/)
})

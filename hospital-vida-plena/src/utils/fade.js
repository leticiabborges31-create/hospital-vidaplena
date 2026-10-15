// Diretiva v-fade: o elemento aparece (fade-in) ao entrar na tela e some (fade-out) ao sair.
// Uso: <h2 v-fade>  ou, com atraso em ms para escalonar: <li v-fade="150">
let observer

function obs() {
  if (!observer) {
    observer = new IntersectionObserver(
      entradas => entradas.forEach(e => e.target.classList.toggle('fade-in', e.isIntersecting)),
      { threshold: 0.15 }
    )
  }
  return observer
}

export const fade = {
  mounted(el, binding) {
    el.classList.add('fade')
    if (binding.value) el.style.transitionDelay = `${binding.value}ms`
    // Sem suporte ao observer: mostra direto, sem animar.
    if (typeof IntersectionObserver === 'undefined') return el.classList.add('fade-in')
    obs().observe(el)
  },
  unmounted(el) {
    observer?.unobserve(el)
  }
}

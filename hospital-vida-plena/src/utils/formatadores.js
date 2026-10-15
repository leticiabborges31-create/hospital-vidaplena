export const pad = n => String(n).padStart(2, '0')

export const hash = s => [...s].reduce((a, c) => (a * 31 + c.charCodeAt(0)) >>> 0, 7)

export const mascaraCpf = v =>
  v.replace(/\D/g, '').slice(0, 11)
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d{1,2})$/, '$1-$2')

export const mascaraTel = v =>
  v.replace(/\D/g, '').slice(0, 11)
    .replace(/^(\d{2})(\d)/, '($1) $2')
    .replace(/(\d{5})(\d{1,4})$/, '$1-$2')

export const dataBR = iso => (iso ? iso.split('-').reverse().join('/') : '')

export function linkAgenda(a) {
  const dia = a.data.replace(/-/g, '')
  const h = +a.hora.slice(0, 2)
  const m = +a.hora.slice(3)
  const ini = `${dia}T${a.hora.replace(':', '')}00`
  const fim = `${dia}T${pad(m === 30 ? h + 1 : h)}${m === 30 ? '00' : '30'}00`
  return 'https://calendar.google.com/calendar/render?action=TEMPLATE'
    + `&text=${encodeURIComponent(a.especialidade + ' - Hospital Vida Plena')}`
    + `&dates=${ini}/${fim}&details=${encodeURIComponent('Código: ' + a.code)}`
}

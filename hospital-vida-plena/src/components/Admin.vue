<script setup>
import { ref, reactive, computed, watch } from 'vue'
import LoginModal from './LoginModal.vue'
import { useAuth } from '../composables/useAuth.js'
import { ESP } from '../data/dados.js'
import { api, apiAdmin } from '../service/api.js'
import { mascaraTel, dataBR, pad } from '../utils/formatadores.js'

const aba = ref('agendamentos')
const aviso = ref('')

// ---------- util ----------
const cmp = (a, b) => String(a ?? '').localeCompare(String(b ?? ''), 'pt-BR', { sensitivity: 'base' })

function ordenar(lista, ord) {
  const out = [...lista].sort((a, b) => cmp(a[ord.campo], b[ord.campo]))
  return ord.dir === 'asc' ? out : out.reverse()
}
function alternar(ord, campo) {
  if (ord.campo === campo) ord.dir = ord.dir === 'asc' ? 'desc' : 'asc'
  else { ord.campo = campo; ord.dir = 'asc' }
}
const seta = (ord, campo) => (ord.campo === campo ? (ord.dir === 'asc' ? ' ▲' : ' ▼') : '')
const POR_PAGINA = 8
const paginar = (lista, pag) => lista.slice((pag - 1) * POR_PAGINA, pag * POR_PAGINA)
const totalPag = lista => Math.max(1, Math.ceil(lista.length / POR_PAGINA))

// ---------- agendamentos ----------
const STATUS_AG = {
  AGENDADO: { rotulo: 'Agendado', cor: 'azul' },
  CHECKIN_FEITO: { rotulo: 'Check-in feito', cor: 'verde' },
  CANCELADO: { rotulo: 'Cancelado', cor: 'vermelho' },
  ESPERA: { rotulo: 'Lista de espera', cor: 'amarelo' }
}
const agend = ref([])
const ordAg = reactive({ campo: 'nomePaciente', dir: 'asc' })
const fAg = reactive({ busca: '', medico: '', status: '', data: '' })
const pagAg = ref(1)

const medicosAg = computed(() => [...new Set(agend.value.map(a => a.medico))].sort(cmp))

const agFiltrados = computed(() => {
  const q = fAg.busca.trim().toLowerCase()
  const lista = agend.value.filter(a =>
    (!q || [a.nomePaciente, a.cpf, a.codigo].some(v => String(v ?? '').toLowerCase().includes(q))) &&
    (!fAg.medico || a.medico === fAg.medico) &&
    (!fAg.status || a.status === fAg.status) &&
    (!fAg.data || (a.dataHora || '').startsWith(fAg.data)))
  return ordenar(lista, ordAg)
})
const agPagina = computed(() => paginar(agFiltrados.value, pagAg.value))

async function carregarAgendamentos() {
  try { agend.value = await api.listar() } catch (e) { aviso.value = 'Não consegui carregar os agendamentos. O backend está rodando?' }
}
// O check-in só vale no dia da consulta (regra do backend): o botão fica desabilitado nos outros dias.
const hojeISO = () => {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}
const ehHoje = iso => !!iso && iso.slice(0, 10) === hojeISO()

async function acaoAg(fn, codigo) {
  aviso.value = ''
  try { await fn(codigo); await carregarAgendamentos() } catch (e) {
    const msg = String(e.message)
    if (msg.includes('409')) {
      aviso.value = fn === apiAdmin.checkin
        ? 'Check-in não permitido: só é possível no dia da consulta, para agendamentos ainda não confirmados.'
        : 'Não é possível cancelar um agendamento que já teve check-in.'
    } else if (msg.includes('401') || msg.includes('403')) {
      aviso.value = 'Sua sessão expirou ou você não tem permissão. Entre novamente.'
    } else {
      aviso.value = 'Não foi possível concluir a ação. Tente novamente.'
    }
    await carregarAgendamentos()
  }
}
const dataHoraBR = iso => (iso ? `${dataBR(iso.slice(0, 10))} · ${iso.slice(11, 16)}` : '—')

// ---------- médicos ----------
const STATUS_MED = {
  ATIVO: { rotulo: 'Ativo', cor: 'verde' },
  FERIAS: { rotulo: 'Férias', cor: 'amarelo' },
  INATIVO: { rotulo: 'Inativo', cor: 'vermelho' }
}
const medicos = ref([])
const ordMed = reactive({ campo: 'nome', dir: 'asc' })
const buscaMed = ref('')
const pagMed = ref(1)
const medicosLocais = ref(false)

const medFiltrados = computed(() => {
  const q = buscaMed.value.trim().toLowerCase()
  const lista = medicos.value.filter(m => !q || [m.nome, m.crm, m.especialidade].some(v => String(v ?? '').toLowerCase().includes(q)))
  return ordenar(lista, ordMed)
})
const medPagina = computed(() => paginar(medFiltrados.value, pagMed.value))

// Dados de exemplo enquanto a rota /api/medicos não existe no backend.
const exemplo = () => ESP.flatMap(e => e.medicos.map(m => ({
  id: m.id, nome: m.nome, crm: m.crm, especialidade: e.nome,
  telefone: '', email: '', tipo: 'Ambos', status: 'ATIVO'
})))

async function carregarMedicos() {
  try { medicos.value = await apiAdmin.medicos(); medicosLocais.value = false }
  catch (e) { medicos.value = exemplo(); medicosLocais.value = true }
}

const form = reactive({ aberto: false, id: null, nome: '', crm: '', especialidade: '', telefone: '', email: '', tipo: 'Ambos', status: 'ATIVO', erro: '' })
function novoMedico(m) {
  Object.assign(form, { aberto: true, erro: '', id: null, nome: '', crm: '', especialidade: '', telefone: '', email: '', tipo: 'Ambos', status: 'ATIVO' }, m || {})
}
async function salvarMedico() {
  form.erro = ''
  if (form.nome.trim().length < 5) return (form.erro = 'Digite o nome do médico.')
  if (!form.crm.trim()) return (form.erro = 'Informe o CRM.')
  if (!form.especialidade) return (form.erro = 'Escolha a especialidade.')
  const dados = { nome: form.nome.trim(), crm: form.crm.trim(), especialidade: form.especialidade, telefone: form.telefone, email: form.email, tipo: form.tipo, status: form.status }
  try {
    if (medicosLocais.value) {
      if (form.id) medicos.value = medicos.value.map(m => (m.id === form.id ? { ...m, ...dados } : m))
      else medicos.value = [...medicos.value, { id: 'L' + Date.now(), ...dados }]
    } else {
      if (form.id) await apiAdmin.atualizarMedico(form.id, dados)
      else await apiAdmin.criarMedico(dados)
      await carregarMedicos()
    }
    form.aberto = false
  } catch (e) { form.erro = e.message }
}
async function trocarStatus(m, status) {
  try {
    if (medicosLocais.value) medicos.value = medicos.value.map(x => (x.id === m.id ? { ...x, status } : x))
    else { await apiAdmin.mudarStatusMedico(m.id, status); await carregarMedicos() }
  } catch (e) { aviso.value = e.message }
}

// ---------- pacientes (agrupados a partir dos agendamentos, pelo CPF) ----------
const ordPac = reactive({ campo: 'nome', dir: 'asc' })
const buscaPac = ref('')
const pagPac = ref(1)

const pacientes = computed(() => {
  const mapa = new Map()
  for (const a of agend.value) {
    const chave = String(a.cpf || '').replace(/\D/g, '') || a.codigo
    const p = mapa.get(chave) || { cpf: a.cpf, nome: a.nomePaciente, telefone: a.telefone, convenio: a.convenio, total: 0, ultima: '' }
    p.total++
    if ((a.dataHora || '') >= p.ultima) { p.ultima = a.dataHora || p.ultima; p.nome = a.nomePaciente; p.telefone = a.telefone; p.convenio = a.convenio }
    mapa.set(chave, p)
  }
  return [...mapa.values()]
})
const pacFiltrados = computed(() => {
  const q = buscaPac.value.trim().toLowerCase()
  const lista = pacientes.value.filter(p => !q || [p.nome, p.cpf, p.telefone].some(v => String(v ?? '').toLowerCase().includes(q)))
  return ordenar(lista, ordPac)
})
const pacPagina = computed(() => paginar(pacFiltrados.value, pagPac.value))

// O painel só carrega os dados depois do login (a API exige o token).
const { logado, nome, admin, sair } = useAuth()
const loginAberto = ref(false)
watch(logado, ok => { if (ok) { carregarAgendamentos(); carregarMedicos() } }, { immediate: true })
</script>

<template>
  <div class="adm">
    <header class="adm-top">
      <a class="logo" href="#">Hospital Vida Plena · Painel</a>
      <span class="adm-user">
        <template v-if="logado">Olá, {{ nome }} <button class="btn ghost" @click="sair">Sair</button></template>
        <a class="btn ghost" href="#">Voltar ao site</a>
      </span>
    </header>

    <LoginModal :aberto="loginAberto" @fechar="loginAberto = false" />

    <div class="gate card" v-if="!logado">
      <h3>Acesso restrito</h3>
      <p class="sub tight">Entre com sua conta da equipe para ver os agendamentos e os médicos.</p>
      <div class="nav start"><button class="btn" @click="loginAberto = true">Entrar</button></div>
    </div>

    <div class="adm-body" v-else>
      <nav class="adm-menu" aria-label="Painel">
        <button :class="{ on: aba === 'agendamentos' }" @click="aba = 'agendamentos'">Agendamentos</button>
        <button :class="{ on: aba === 'medicos' }" @click="aba = 'medicos'">Médicos</button>
        <button :class="{ on: aba === 'pacientes' }" @click="aba = 'pacientes'">Pacientes</button>
      </nav>

      <main class="adm-main">
        <p class="aviso" v-if="aviso" @click="aviso = ''">{{ aviso }} (clique para fechar)</p>

        <!-- AGENDAMENTOS -->
        <section v-if="aba === 'agendamentos'">
          <h2>Agendamentos</h2>
          <div class="filtros">
            <input v-model="fAg.busca" placeholder="Buscar por nome, CPF ou código" @input="pagAg = 1">
            <select v-model="fAg.medico" @change="pagAg = 1">
              <option value="">Todos os médicos</option>
              <option v-for="m in medicosAg" :key="m">{{ m }}</option>
            </select>
            <select v-model="fAg.status" @change="pagAg = 1">
              <option value="">Todos os status</option>
              <option v-for="(s, k) in STATUS_AG" :key="k" :value="k">{{ s.rotulo }}</option>
            </select>
            <input type="date" v-model="fAg.data" @change="pagAg = 1">
          </div>

          <div class="tabela">
            <table>
              <thead>
                <tr>
                  <th><button @click="alternar(ordAg, 'nomePaciente')">Paciente{{ seta(ordAg, 'nomePaciente') }}</button></th>
                  <th><button @click="alternar(ordAg, 'codigo')">Código{{ seta(ordAg, 'codigo') }}</button></th>
                  <th><button @click="alternar(ordAg, 'medico')">Médico{{ seta(ordAg, 'medico') }}</button></th>
                  <th><button @click="alternar(ordAg, 'especialidade')">Especialidade{{ seta(ordAg, 'especialidade') }}</button></th>
                  <th><button @click="alternar(ordAg, 'dataHora')">Data / hora{{ seta(ordAg, 'dataHora') }}</button></th>
                  <th><button @click="alternar(ordAg, 'status')">Status{{ seta(ordAg, 'status') }}</button></th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="a in agPagina" :key="a.codigo">
                  <td>{{ a.nomePaciente }}<small>{{ a.telefone }}</small></td>
                  <td><b>{{ a.codigo }}</b></td>
                  <td>{{ a.medico }}</td>
                  <td>{{ a.especialidade }}<small>{{ a.tipo }}</small></td>
                  <td>{{ dataHoraBR(a.dataHora) }}</td>
                  <td><span class="tag" :class="STATUS_AG[a.status]?.cor">{{ STATUS_AG[a.status]?.rotulo || a.status }}</span></td>
                  <td class="acoes-col">
                    <button class="btn ghost" :disabled="a.status !== 'AGENDADO' || !ehHoje(a.dataHora)" :title="a.status === 'AGENDADO' && !ehHoje(a.dataHora) ? 'O check-in só vale no dia da consulta' : ''" @click="acaoAg(apiAdmin.checkin, a.codigo)">Check-in</button>
                    <button class="btn ghost" :disabled="a.status === 'CANCELADO' || a.status === 'CHECKIN_FEITO'" @click="acaoAg(api.cancelar, a.codigo)">Cancelar</button>
                  </td>
                </tr>
                <tr v-if="!agPagina.length"><td colspan="7" class="vazio">Nenhum agendamento encontrado.</td></tr>
              </tbody>
            </table>
          </div>
          <div class="pag">
            <span>{{ agFiltrados.length }} resultado(s)</span>
            <span>
              <button class="btn ghost" :disabled="pagAg === 1" @click="pagAg--">‹</button>
              Página {{ pagAg }} de {{ totalPag(agFiltrados) }}
              <button class="btn ghost" :disabled="pagAg >= totalPag(agFiltrados)" @click="pagAg++">›</button>
            </span>
          </div>
        </section>

        <!-- MÉDICOS -->
        <section v-else-if="aba === 'medicos'">
          <h2>Médicos</h2>
          <p class="aviso info" v-if="medicosLocais">
            Mostrando dados de exemplo: a rota <b>/api/medicos</b> ainda não existe no backend. Alterações aqui não são salvas no banco.
          </p>
          <div class="filtros">
            <input v-model="buscaMed" placeholder="Buscar por nome, CRM ou especialidade" @input="pagMed = 1">
            <button v-if="admin" class="btn" @click="novoMedico()">+ Novo médico</button>
          </div>

          <div class="tabela">
            <table>
              <thead>
                <tr>
                  <th><button @click="alternar(ordMed, 'nome')">Nome{{ seta(ordMed, 'nome') }}</button></th>
                  <th><button @click="alternar(ordMed, 'crm')">CRM{{ seta(ordMed, 'crm') }}</button></th>
                  <th><button @click="alternar(ordMed, 'especialidade')">Especialidade{{ seta(ordMed, 'especialidade') }}</button></th>
                  <th>Contato</th>
                  <th><button @click="alternar(ordMed, 'tipo')">Atende{{ seta(ordMed, 'tipo') }}</button></th>
                  <th><button @click="alternar(ordMed, 'status')">Status{{ seta(ordMed, 'status') }}</button></th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="m in medPagina" :key="m.id">
                  <td><b>{{ m.nome }}</b></td>
                  <td>{{ m.crm }}</td>
                  <td>{{ m.especialidade }}</td>
                  <td>{{ m.telefone || '—' }}<small>{{ m.email }}</small></td>
                  <td>{{ m.tipo }}</td>
                  <td>
                    <select class="sel-status" :disabled="!admin" :class="STATUS_MED[m.status]?.cor" :value="m.status" @change="trocarStatus(m, $event.target.value)">
                      <option v-for="(s, k) in STATUS_MED" :key="k" :value="k">{{ s.rotulo }}</option>
                    </select>
                  </td>
                  <td class="acoes-col"><button v-if="admin" class="btn ghost" @click="novoMedico(m)">Editar</button></td>
                </tr>
                <tr v-if="!medPagina.length"><td colspan="7" class="vazio">Nenhum médico encontrado.</td></tr>
              </tbody>
            </table>
          </div>
          <div class="pag">
            <span>{{ medFiltrados.length }} médico(s)</span>
            <span>
              <button class="btn ghost" :disabled="pagMed === 1" @click="pagMed--">‹</button>
              Página {{ pagMed }} de {{ totalPag(medFiltrados) }}
              <button class="btn ghost" :disabled="pagMed >= totalPag(medFiltrados)" @click="pagMed++">›</button>
            </span>
          </div>
        </section>

        <!-- PACIENTES -->
        <section v-else>
          <h2>Pacientes</h2>
          <p class="sub tight">Pacientes que já agendaram, agrupados pelo CPF.</p>
          <div class="filtros">
            <input v-model="buscaPac" placeholder="Buscar por nome, CPF ou telefone" @input="pagPac = 1">
          </div>

          <div class="tabela">
            <table>
              <thead>
                <tr>
                  <th><button @click="alternar(ordPac, 'nome')">Nome{{ seta(ordPac, 'nome') }}</button></th>
                  <th><button @click="alternar(ordPac, 'cpf')">CPF{{ seta(ordPac, 'cpf') }}</button></th>
                  <th>Telefone</th>
                  <th><button @click="alternar(ordPac, 'convenio')">Convênio{{ seta(ordPac, 'convenio') }}</button></th>
                  <th><button @click="alternar(ordPac, 'total')">Consultas{{ seta(ordPac, 'total') }}</button></th>
                  <th><button @click="alternar(ordPac, 'ultima')">Última consulta{{ seta(ordPac, 'ultima') }}</button></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="p in pacPagina" :key="p.cpf || p.nome">
                  <td><b>{{ p.nome }}</b></td>
                  <td>{{ p.cpf }}</td>
                  <td>{{ p.telefone }}</td>
                  <td>{{ p.convenio || '—' }}</td>
                  <td>{{ p.total }}</td>
                  <td>{{ dataHoraBR(p.ultima) }}</td>
                </tr>
                <tr v-if="!pacPagina.length"><td colspan="6" class="vazio">Nenhum paciente encontrado.</td></tr>
              </tbody>
            </table>
          </div>
          <div class="pag">
            <span>{{ pacFiltrados.length }} paciente(s)</span>
            <span>
              <button class="btn ghost" :disabled="pagPac === 1" @click="pagPac--">‹</button>
              Página {{ pagPac }} de {{ totalPag(pacFiltrados) }}
              <button class="btn ghost" :disabled="pagPac >= totalPag(pacFiltrados)" @click="pagPac++">›</button>
            </span>
          </div>
        </section>
      </main>
    </div>

    <!-- Formulário de médico -->
    <div class="modal" v-if="form.aberto" @click.self="form.aberto = false">
      <div class="card caixa" role="dialog" aria-modal="true">
        <h3>{{ form.id ? 'Editar médico' : 'Novo médico' }}</h3>
        <label class="lbl" for="m-nome">Nome</label>
        <input id="m-nome" v-model="form.nome">
        <label class="lbl" for="m-crm">CRM</label>
        <input id="m-crm" v-model="form.crm" placeholder="CRM-MA 0000">
        <label class="lbl" for="m-esp">Especialidade</label>
        <select id="m-esp" v-model="form.especialidade">
          <option value="" disabled>Escolha...</option>
          <option v-for="e in ESP" :key="e.id">{{ e.nome }}</option>
        </select>
        <label class="lbl" for="m-tel">Telefone</label>
        <input id="m-tel" :value="form.telefone" inputmode="tel" @input="form.telefone = mascaraTel($event.target.value)">
        <label class="lbl" for="m-mail">E-mail</label>
        <input id="m-mail" type="email" v-model="form.email">
        <label class="lbl" for="m-tipo">Tipo de atendimento</label>
        <select id="m-tipo" v-model="form.tipo"><option>Presencial</option><option>Teleconsulta</option><option>Ambos</option></select>
        <p class="err" v-if="form.erro">{{ form.erro }}</p>
        <div class="nav">
          <button class="btn ghost" @click="form.aberto = false">Cancelar</button>
          <button class="btn" @click="salvarMedico">Salvar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.adm{min-height:100vh;background:var(--paper)}
.adm-top{display:flex;justify-content:space-between;align-items:center;padding:1rem 1.25rem;background:#fff;border-bottom:1.5px solid var(--line)}
.adm-user{display:flex;align-items:center;gap:.6rem;flex-wrap:wrap;font-weight:500}
.gate{max-width:440px;margin:3rem auto}
.adm-body{display:grid;grid-template-columns:200px 1fr;min-height:calc(100vh - 70px)}
.adm-menu{display:flex;flex-direction:column;gap:.25rem;padding:1rem;border-right:1.5px solid var(--line);background:#fff}
.adm-menu button{text-align:left;border:0;background:transparent;padding:.7rem .9rem;border-radius:var(--r);font:500 1rem Figtree,sans-serif;cursor:pointer;color:var(--ink)}
.adm-menu button:hover{background:var(--mist)}
.adm-menu button.on{background:var(--sea);color:#fff}
.adm-main{padding:1.5rem;min-width:0}
.filtros{display:flex;flex-wrap:wrap;gap:.6rem;margin:1rem 0}
.filtros input,.filtros select{width:auto;flex:1 1 180px}
.tabela{overflow-x:auto;background:#fff;border:1.5px solid var(--line);border-radius:14px}
table{width:100%;border-collapse:collapse;font-size:.92rem}
th,td{padding:.7rem .8rem;text-align:left;border-bottom:1px solid var(--line);vertical-align:top}
th{background:var(--mist);white-space:nowrap}
th button{border:0;background:transparent;font:700 .88rem Figtree,sans-serif;color:var(--sea-d);cursor:pointer;padding:0}
td small{display:block;color:var(--mute)}
tbody tr:hover{background:var(--paper)}
.vazio{text-align:center;color:var(--mute);padding:2rem}
.acoes-col{white-space:nowrap}
.acoes-col .btn{padding:.35rem .7rem;margin-right:.3rem;font-size:.85rem}
.tag.azul{background:#dbeafe;color:#1e40af}
.tag.verde{background:#d9f2e3;color:#166534}
.tag.vermelho{background:#f6dcd6;color:#8a2e1c}
.tag.amarelo{background:#fdf0c4;color:#7a5b00}
.sel-status{width:auto;padding:.25rem .5rem;border-radius:99px;font-size:.82rem}
.sel-status.verde{background:#d9f2e3}.sel-status.amarelo{background:#fdf0c4}.sel-status.vermelho{background:#f6dcd6}
.pag{display:flex;justify-content:space-between;align-items:center;gap:1rem;margin-top:.8rem;color:var(--mute);flex-wrap:wrap}
.pag .btn{padding:.3rem .7rem}
.aviso{background:#f6dcd6;color:#8a2e1c;padding:.7rem 1rem;border-radius:var(--r);margin-bottom:1rem;cursor:pointer}
.aviso.info{background:#fdf0c4;color:#7a5b00;cursor:default}
.modal{position:fixed;inset:0;background:rgba(14,42,47,.5);display:grid;place-items:center;padding:1rem;z-index:10}
.caixa{width:100%;max-width:460px;max-height:90vh;overflow:auto}
@media(max-width:800px){.adm-body{grid-template-columns:1fr}.adm-menu{flex-direction:row;border-right:0;border-bottom:1.5px solid var(--line)}}
</style>
















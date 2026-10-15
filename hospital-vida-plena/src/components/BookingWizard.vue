<script setup>
import { reactive, ref, computed, watch } from 'vue'
import { ESP, HORAS } from '../data/dados.js'
import { pad, mascaraCpf, mascaraTel, dataBR, linkAgenda } from '../utils/formatadores.js'
import { useAgendamentos } from '../composables/useAgendamentos.js'
import { api } from '../service/api.js'   // confira: pasta "services"

const props = defineProps({ pacoteEscolhido: { type: Number, default: 0 } })
const { adicionar, horarioOcupado } = useAgendamentos()

const etapas = ['Especialidade', 'Horário', 'Seus dados', 'Pronto']
const step = ref(1)
const erro = ref('')
const ultimo = ref(null)
const salvando = ref(false)
const f = reactive({
  esp: '', tipo: 'Presencial', medico: '', data: '', hora: '',
  nome: '', cpf: '', tel: '', convenio: 'Particular', lgpd: false, espera: false
})

const esp = computed(() => ESP.find(e => e.id === f.esp) || { medicos: [] })

const dias = computed(() => {
  const out = []
  const d = new Date()
  while (out.length < 7) {
    d.setDate(d.getDate() + 1)
    if (d.getDay() === 0) continue
    out.push({
      iso: `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`,
      rotulo: d.toLocaleDateString('pt-BR', { weekday: 'short', day: '2-digit' }).replace('.', '')
    })
  }
  return out
})

// Horários já tomados, vindos do banco (GET /ocupados).
const ocupados = ref([])
watch(() => [f.medico, f.data], async ([m, d]) => {
  f.hora = ''
  ocupados.value = []
  if (!m || !d) return
  try {
    const nome = esp.value.medicos.find(x => x.id === m)?.nome
    ocupados.value = await api.ocupados(nome, d)
  } catch (e) { console.error(e) }
})

const slots = computed(() => HORAS.map(h => ({
  h,
  busy: ocupados.value.includes(h) || horarioOcupado(f.medico, f.data, h)
})))

watch(() => props.pacoteEscolhido, () => {
  escolherEsp('checkup')
  f.tipo = 'Presencial'
  step.value = 2
  document.getElementById('agendar')?.scrollIntoView()
})

function escolherEsp(id) {
  f.esp = id
  f.medico = esp.value.medicos.length === 1 ? esp.value.medicos[0].id : ''
  f.hora = ''
}

function entrarEspera() {
  f.espera = true
  f.hora = ''
  f.data = ''
  erro.value = ''
  step.value = 3
}

function avancar() {
  erro.value = ''
  if (step.value === 1) {
    if (!f.esp) return (erro.value = 'Escolha uma especialidade.')
    step.value = 2
  } else if (step.value === 2) {
    if (!f.medico) return (erro.value = 'Escolha um médico.')
    if (!f.data || !f.hora) return (erro.value = 'Escolha o dia e o horário.')
    f.espera = false
    step.value = 3
  } else if (step.value === 3) {
    if (f.nome.length < 5) return (erro.value = 'Digite seu nome completo.')
    if (f.cpf.replace(/\D/g, '').length !== 11) return (erro.value = 'Digite um CPF com 11 números.')
    if (f.tel.replace(/\D/g, '').length < 10) return (erro.value = 'Digite um WhatsApp com DDD.')
    if (!f.lgpd) return (erro.value = 'Marque a autorização para continuar.')
    confirmar()
  }
}

async function confirmar() {
  const medico = esp.value.medicos.find(x => x.id === f.medico)
  salvando.value = true
  erro.value = ''
  try {
    const salvo = await api.criar({
      nomePaciente: f.nome,
      cpf: f.cpf,
      telefone: f.tel,
      convenio: f.convenio,
      especialidade: esp.value.nome,
      medico: medico.nome,                 // nome, não o id
      tipo: f.tipo,
      espera: f.espera,
      dataHora: f.espera ? null : `${f.data}T${f.hora}:00`
    })
    const code = salvo.codigo
    ultimo.value = {
      code, especialidade: esp.value.nome, tipo: f.tipo,
      medico: f.medico, medicoNome: medico.nome,
      data: f.data, hora: f.hora,
      nome: f.nome, cpf: f.cpf, tel: f.tel, convenio: f.convenio,
      status: f.espera ? 'espera' : 'agendado'
    }
    adicionar(ultimo.value)   // provisório: mantém "Meus agendamentos" e o check-in funcionando
    step.value = 4
  } catch (e) {
    erro.value = e.message.includes('409')
      ? 'Esse horário acabou de ser ocupado. Escolha outro.'
      : 'Não foi possível salvar. Tente novamente.'
    console.error(e)
  } finally {
    salvando.value = false
  }
}

function reiniciar() {
  Object.assign(f, { esp: '', medico: '', data: '', hora: '', espera: false })
  step.value = 1
}
</script>

<template>

      <div class="card">
        <ol class="steps">
          <li v-for="(e, i) in etapas" :key="e" :class="{ on: step >= i + 1 }">{{ e }}</li>
        </ol>

        <!-- 1. Especialidade -->
        <div v-if="step === 1">
          <h3>Qual especialidade?</h3>
          <div class="grid">
            <button v-for="e in ESP" :key="e.id" class="opt" :aria-pressed="f.esp === e.id" @click="escolherEsp(e.id)">{{ e.nome }}</button>
          </div>
          <label class="lbl">Tipo de consulta</label>
          <div class="seg">
            <button v-for="t in ['Presencial', 'Teleconsulta']" :key="t" class="opt" :aria-pressed="f.tipo === t" @click="f.tipo = t">{{ t }}</button>
          </div>
        </div>

        <!-- 2. Médico, dia e horário -->
        <div v-else-if="step === 2">
          <h3>Médico e horário</h3>
          <div class="lista">
            <button v-for="m in esp.medicos" :key="m.id" class="opt doc" :aria-pressed="f.medico === m.id" @click="f.medico = m.id">
              {{ m.nome }}<small>{{ m.crm }}</small>
            </button>
          </div>
          <template v-if="f.medico">
            <label class="lbl">Dia</label>
            <div class="grid dias">
              <button v-for="d in dias" :key="d.iso" class="opt" :aria-pressed="f.data === d.iso" @click="f.data = d.iso">{{ d.rotulo }}</button>
            </div>
          </template>
          <template v-if="f.data">
            <label class="lbl">Horário disponível</label>
            <div class="grid horas">
              <button v-for="s in slots" :key="s.h" class="opt" :disabled="s.busy" :aria-pressed="f.hora === s.h" @click="f.hora = s.h">{{ s.h }}</button>
            </div>
            <p class="sub tight" style="margin-top:.75rem">Não achou um horário bom?
              <a href="#agendar" @click.prevent="entrarEspera">Entrar na lista de espera</a>
            </p>
          </template>
        </div>

        <!-- 3. Dados -->
        <div v-else-if="step === 3">
          <h3>{{ f.espera ? 'Lista de espera: seus dados' : 'Seus dados' }}</h3>
          <label class="lbl" for="nome">Nome completo</label>
          <input id="nome" v-model="f.nome" autocomplete="name">
          <label class="lbl" for="cpf">CPF</label>
          <input id="cpf" :value="f.cpf" inputmode="numeric" placeholder="000.000.000-00" @input="f.cpf = mascaraCpf($event.target.value)">
          <label class="lbl" for="tel">WhatsApp</label>
          <input id="tel" :value="f.tel" inputmode="tel" placeholder="(98) 99999-9999" @input="f.tel = mascaraTel($event.target.value)">
          <label class="lbl" for="conv">Convênio</label>
          <select id="conv" v-model="f.convenio">
            <option>Particular</option><option>Unimed</option><option>Bradesco Saúde</option><option>SulAmérica</option><option>Outro</option>
          </select>
          <label class="lbl check"><input type="checkbox" v-model="f.lgpd"> Autorizo o uso dos meus dados para o atendimento (LGPD).</label>
        </div>

        <!-- 4. Pronto -->
        <div v-else-if="ultimo">
          <h3>{{ ultimo.status === 'espera' ? 'Você está na lista de espera' : 'Consulta agendada!' }}</h3>
          <div class="code">{{ ultimo.code }}</div>
          <div class="sum">
            <span><b>{{ ultimo.especialidade }}</b> · {{ ultimo.tipo }}</span>
            <span>{{ ultimo.medicoNome }}</span>
            <span v-if="ultimo.status !== 'espera'">{{ dataBR(ultimo.data) }} às {{ ultimo.hora }}</span>
            <span>{{ ultimo.nome }} · {{ ultimo.tel }}</span>
          </div>
          <p class="sub tight">Guarde o código: ele é usado no check-in.</p>
          <div class="nav start wrap-row">
            <a v-if="ultimo.status !== 'espera'" class="btn ghost" :href="linkAgenda(ultimo)" target="_blank" rel="noopener">Adicionar à agenda</a>
            <button class="btn" @click="reiniciar">Novo agendamento</button>
          </div>
        </div>

        <p class="err" v-if="erro">{{ erro }}</p>

        <div class="nav" v-if="step < 4">
          <button class="btn ghost" :disabled="step === 1" @click="step--; erro = ''">Voltar</button>
          <button class="btn" :disabled="salvando" @click="avancar">
            {{ step === 3 ? (salvando ? 'Salvando...' : 'Confirmar') : 'Continuar' }}
          </button>
        </div>
      </div>
</template>

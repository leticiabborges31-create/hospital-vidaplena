<script setup>
import { dataBR } from '../utils/formatadores.js'
import { useAgendamentos } from '../composables/useAgendamentos.js'

import { ref } from 'vue'
import { apiPaciente } from '../service/api.js'

const { agendamentos, cancelar } = useAgendamentos()
const erro = ref('')

// Cancela no banco (código + CPF) e depois neste aparelho.
async function cancelarAg(a) {
  erro.value = ''
  try {
    await apiPaciente.cancelar(a.code, a.cpf)
  } catch (e) {
    // 404: agendamento antigo que só existe neste aparelho, pode limpar só aqui.
    if (!e.message.includes('404')) return (erro.value = e.message.includes('409') ? 'Não é possível cancelar após o check-in.' : 'Não foi possível cancelar agora. Tente novamente.')
  }
  cancelar(a.code)
}
</script>

<template>
  <div>
    <h2 v-fade>Meus agendamentos</h2>
    <p class="err" v-if="erro">{{ erro }}</p>
    <p class="sub" v-if="!agendamentos.length">Você ainda não tem agendamentos neste aparelho.</p>

    <div class="ag" v-for="a in agendamentos" :key="a.code">
      <div>
        <strong>{{ a.especialidade }}</strong> · {{ a.tipo }}<br>
        
        <!-- 1. EXIBINDO NOME E TELEFONE DO PACIENTE -->
        <div class="paciente-info" v-if="a.nome || a.paciente?.nome">
          <small>
            👤 <strong>Paciente:</strong> {{ a.nome || a.paciente?.nome }} 
            <span v-if="a.telefone || a.paciente?.telefone">
              ({{ a.telefone || a.paciente?.telefone }})
            </span>
          </small>
        </div>

        <small>{{ a.status === 'espera' ? 'Lista de espera' : dataBR(a.data) + ' às ' + a.hora }} · {{ a.code }}</small>
      </div>

      <span class="acoes">
        <span class="tag" :class="a.status">{{ a.status }}</span>
        <button v-if="a.status === 'agendado' || a.status === 'espera'" class="btn ghost" @click="cancelarAg(a)">Cancelar</button>
      </span>
    </div>
  </div>
</template>
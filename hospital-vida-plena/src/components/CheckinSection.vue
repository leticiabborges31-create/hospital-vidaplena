<script setup>
import { reactive, ref } from 'vue'
import { useAgendamentos } from '../composables/useAgendamentos.js'
import { apiPaciente } from '../service/api.js'
import { mascaraCpf, dataBR } from '../utils/formatadores.js'
import MeusAgendamentos from './MeusAgendamentos.vue'

const { buscar, atualizar } = useAgendamentos()
const ci = reactive({ code: '', cpf: '', erro: '', ok: null })
const enviando = ref(false)

async function fazerCheckin() {
  ci.erro = ''
  ci.ok = null
  const code = ci.code.trim().toUpperCase()
  if (!code) return (ci.erro = 'Digite o código do agendamento.')
  if (ci.cpf.replace(/\D/g, '').length !== 11) return (ci.erro = 'Digite o CPF usado no agendamento.')

  enviando.value = true
  try {
    const r = await apiPaciente.checkin(code, ci.cpf)
    // Mantém "Meus agendamentos" neste aparelho em dia com o banco.
    if (buscar(code)) atualizar(code, { status: 'check-in feito' })
    ci.ok = { code: r.codigo, medico: r.medico, data: r.dataHora.slice(0, 10), hora: r.dataHora.slice(11, 16) }
  } catch (e) {
    if (e.message.includes('404')) ci.erro = 'Código ou CPF não conferem. Confira e tente de novo.'
    else if (e.message.includes('409')) ci.erro = 'O check-in só é possível no dia da consulta, para consultas agendadas (não canceladas nem já confirmadas).'
    else ci.erro = 'Não foi possível fazer o check-in agora. Tente novamente.'
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <section class="sec alt" id="checkin">
    <div class="wrap two">
      <div>
        <h2 v-fade>Check-in online</h2>
        <p class="sub" v-fade="150">No dia da consulta, informe o código e o CPF do agendamento e apresente o código na recepção. Sem fila e sem papel.</p>
        <label class="lbl" for="cod">Código do agendamento</label>
        <input id="cod" v-model="ci.code" placeholder="VP-XXXXX" style="text-transform:uppercase" @keyup.enter="fazerCheckin">
        <label class="lbl" for="cpf-ci">CPF do paciente</label>
        <input id="cpf-ci" :value="ci.cpf" inputmode="numeric" placeholder="000.000.000-00" @input="ci.cpf = mascaraCpf($event.target.value)" @keyup.enter="fazerCheckin">
        <p class="err" v-if="ci.erro">{{ ci.erro }}</p>
        <div class="nav start"><button class="btn" :disabled="enviando" @click="fazerCheckin">{{ enviando ? 'Verificando...' : 'Fazer check-in' }}</button></div>
        <div v-if="ci.ok" class="card" style="margin-top:1.25rem">
          <h3>Check-in realizado</h3>
          <p>{{ ci.ok.medico }} · {{ dataBR(ci.ok.data) }} às {{ ci.ok.hora }}</p>
          <div class="code">{{ ci.ok.code }}</div>
          <p class="sub tight">Informe este código na recepção.</p>
        </div>
      </div>
      <MeusAgendamentos />
    </div>
  </section>
</template>

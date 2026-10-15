<script setup>
import BookingWizard from './BookingWizard.vue'

defineProps({ pacoteEscolhido: { type: Number, default: 0 } })
</script>

<template>
  <section class="hero" id="agendar">
    <div>
      <h1 v-fade>Sua consulta marcada em menos de um minuto</h1>
      <p class="lead" v-fade="150">Escolha a especialidade, o horário e receba a confirmação no WhatsApp. No dia, faça o check-in pelo celular e vá direto ao atendimento.</p>
      <ul class="facts">
        <li v-fade="300">Consulta presencial ou teleconsulta por vídeo</li>
        <li v-fade="450">Lembretes automáticos 24 horas e 2 horas antes</li>
        <li v-fade="600">Check-in online, sem fila na recepção</li>
      </ul>
    </div>
    <BookingWizard :pacote-escolhido="pacoteEscolhido" />
  </section>
</template>

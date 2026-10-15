<script setup>
import { PACOTES } from '../data/dados.js'

defineEmits(['escolher'])
</script>

<template>
  <section class="sec" id="checkup">
    <div class="wrap">
      <h2 v-fade>Pacotes de check-up</h2>
      <p class="sub" v-fade="150">Exames e consultas reunidos em um único dia, com resultado online.</p>
      <div class="pk">
        <div class="card" v-fade v-for="p in PACOTES" :key="p.nome" :class="{ hl: p.destaque }">
          <h3>{{ p.nome }}</h3>
          <div class="price">R$ {{ p.preco }}</div>
          <ul><li v-for="i in p.itens" :key="i">{{ i }}</li></ul>
          <button class="btn" :class="{ ghost: !p.destaque }" @click="$emit('escolher')">Agendar este pacote</button>
        </div>
      </div>
    </div>
  </section>
</template>

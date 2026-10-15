<script setup>
import { FAQ } from '../data/dados.js'
</script>

<template>
  <section class="sec alt" id="faq">
    <div class="wrap faq-wrap">
      <h2 v-fade>Perguntas frequentes</h2>
      <details v-fade v-for="q in FAQ" :key="q.p"><summary>{{ q.p }}</summary><p>{{ q.r }}</p></details>
    </div>
  </section>
</template>

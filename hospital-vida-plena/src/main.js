import { createApp } from 'vue'
import App from './App.vue'
import { fade } from './utils/fade.js'
import './assets/styles.css'

createApp(App).directive('fade', fade).mount('#app')

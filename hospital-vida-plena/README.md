# Front-end do Hospital Vida Plena (Vue 3 + Vite)

Instruções completas, acessos e API: veja o `README.md` na pasta acima.

```bash
npm install
npm run dev      # http://localhost:5173
npm run build    # gera a pasta dist/ para publicar
```

Estrutura de `src/`:
- `components/`: uma seção da página por componente; `BookingWizard.vue` é o card de agendamento,
  `Admin.vue` é o painel da equipe, `LoginModal.vue` é o login/cadastro
- `service/api.js`: todas as chamadas ao backend (envia o token JWT quando há login)
- `composables/useAuth.js`: sessão do login; `useAgendamentos.js`: "Meus agendamentos" neste aparelho
- `utils/fade.js`: efeito de fade-in/out dos textos (`v-fade`)
- `data/dados.js`: especialidades, médicos, pacotes e FAQ do site

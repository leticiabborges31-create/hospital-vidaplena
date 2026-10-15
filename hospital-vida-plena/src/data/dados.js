export const ESP = [
  { id: 'clinica', nome: 'Clínica geral', medicos: [{ id: 'c1', nome: 'Dr. Paulo Ribeiro', crm: 'CRM-MA 3120' }, { id: 'c2', nome: 'Dra. Ana Beatriz Lima', crm: 'CRM-MA 4455' }] },
  { id: 'cardio', nome: 'Cardiologia', medicos: [{ id: 'k1', nome: 'Dra. Helena Moraes', crm: 'CRM-MA 4821' }, { id: 'k2', nome: 'Dr. Marcos Almeida', crm: 'CRM-MA 2987' }] },
  { id: 'pediatria', nome: 'Pediatria', medicos: [{ id: 'p1', nome: 'Dra. Camila Sousa', crm: 'CRM-MA 5102' }, { id: 'p2', nome: 'Dr. Rafael Costa', crm: 'CRM-MA 3876' }] },
  { id: 'orto', nome: 'Ortopedia', medicos: [{ id: 'o1', nome: 'Dr. Eduardo Nunes', crm: 'CRM-MA 2644' }, { id: 'o2', nome: 'Dra. Luciana Melo', crm: 'CRM-MA 4010' }] },
  { id: 'derma', nome: 'Dermatologia', medicos: [{ id: 'd1', nome: 'Dra. Patrícia Gomes', crm: 'CRM-MA 3791' }, { id: 'd2', nome: 'Dr. Felipe Barros', crm: 'CRM-MA 5233' }] },
  { id: 'checkup', nome: 'Check-up', medicos: [{ id: 'u1', nome: 'Equipe de Check-up', crm: 'Coordenação médica' }] }
]

export const HORAS = ['08:00', '08:30', '09:00', '09:30', '10:00', '10:30', '14:00', '14:30', '15:00', '15:30', '16:00', '16:30']

export const PACOTES = [
  { nome: 'Essencial', preco: '290', itens: ['Consulta com clínico geral', 'Hemograma e glicemia', 'Colesterol e triglicerídeos'] },
  { nome: 'Cardiológico', preco: '690', itens: ['Consulta com cardiologista', 'Eletrocardiograma', 'Teste ergométrico', 'Exames de sangue'], destaque: true },
  { nome: 'Completo', preco: '1.190', itens: ['Clínico e cardiologista', 'Ultrassom abdominal', 'Raio-X de tórax', 'Painel completo de exames'] }
]

export const FAQ = [
  { p: 'Como funciona a teleconsulta?', r: 'Você recebe um link por WhatsApp e e-mail 10 minutos antes. Basta abrir no celular ou computador, sem instalar nada.' },
  { p: 'Posso remarcar ou cancelar?', r: 'Sim. Na seção Meus agendamentos você cancela pelo site, e pode marcar outro horário em seguida.' },
  { p: 'Quais convênios são aceitos?', r: 'Aceitamos os principais convênios da região. Escolha o seu na etapa Seus dados ou fale com a central.' },
  { p: 'Meus dados estão seguros?', r: 'Os dados são usados apenas para o atendimento e tratados conforme a Lei Geral de Proteção de Dados (LGPD).' }
]

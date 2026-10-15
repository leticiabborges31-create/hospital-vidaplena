# Hospital Vida Plena

Site de agendamento de consultas com painel para a equipe.

- **Front-end:** Vue 3 + Vite (pasta `hospital-vida-plena/`)
- **Back-end:** Spring Boot 4 + PostgreSQL + JWT (pasta `backend/`)

## Como rodar

### 1. Banco de dados
Instale o PostgreSQL e crie o banco (uma vez só):

```sql
CREATE DATABASE vidaplena;
```

Usuário e senha ficam em `backend/src/main/resources/application.properties`
(padrão: `postgres` / `1234`). As tabelas são criadas sozinhas.

### 2. Back-end (porta 8080)
```powershell
cd backend
.\mvnw spring-boot:run
```
Precisa do Java 17 ou mais novo. O `mvnw` baixa o Maven sozinho.
Se aparecer "Port 8080 was already in use", há outro backend rodando: pare-o primeiro.

### 3. Front-end (porta 5173)
```powershell
cd hospital-vida-plena
npm install      # só na primeira vez
npm run dev
```
Abra http://localhost:5173

## Acessos

| O quê | Onde |
|---|---|
| Site público (paciente) | http://localhost:5173 |
| Painel da equipe | http://localhost:5173/#/admin (ou link "Painel da equipe" no rodapé) |

**Administrador inicial** (criado na primeira vez que a API sobe):
usuário `admin`, senha `vidaplena123`.

Quem se cadastra pelo botão **Entrar → Cadastrar** (nome, CRM-MA, telefone, e-mail, senha)
recebe o perfil **EQUIPE**.

| Ação | Admin | Equipe | Sem login |
|---|---|---|---|
| Agendar, ver horários, check-in e cancelar (paciente) | sim | sim | sim |
| Ver agendamentos, check-in e cancelar no painel | sim | sim | não |
| Ver médicos | sim | sim | não |
| Cadastrar / editar médicos | sim | não | não |

## O que o sistema faz

- **Paciente (sem conta):** escolhe especialidade, tipo (presencial/teleconsulta), médico, dia e horário,
  informa os dados e recebe um código `VP-XXXXX`. Horários já ocupados aparecem bloqueados.
  Pode entrar na lista de espera quando não há horário bom.
- **Check-in online:** no dia da consulta, com código + CPF.
- **Cancelar:** em "Meus agendamentos", com confirmação por código + CPF (feito automaticamente).
- **Painel da equipe:** tabela de agendamentos e de médicos em ordem alfabética (clique no título da coluna
  para inverter), busca, filtros, paginação, check-in, cancelamento, cadastro de médicos.

## API

Base: `http://localhost:8080/api`

| Método | Rota | Acesso | Para quê |
|---|---|---|---|
| POST | `/auth/cadastro` | público | Cadastrar membro da equipe |
| POST | `/auth/login` | público | Entrar; devolve o token JWT |
| POST | `/agendamentos` | público | Criar agendamento (ou lista de espera com `espera: true`) |
| GET | `/agendamentos/ocupados?medico=&data=` | público | Horários já ocupados |
| POST | `/paciente/checkin` | público | Check-in do paciente (`codigo` + `cpf`) |
| POST | `/paciente/cancelar` | público | Cancelamento do paciente (`codigo` + `cpf`) |
| GET | `/agendamentos` | login | Listar todos |
| GET | `/agendamentos/{codigo}` | login | Buscar um |
| PATCH | `/agendamentos/{codigo}/cancelar` | login | Cancelar |
| POST | `/agendamentos/{codigo}/checkin` | login | Check-in pela recepção |
| GET | `/medicos` | login | Listar médicos |
| POST, PUT, PATCH | `/medicos`, `/medicos/{id}`, `/medicos/{id}/status` | **admin** | Gerenciar médicos |

O token JWT vai no cabeçalho `Authorization: Bearer <token>` e vale 8 horas.

## Segurança

- Senhas guardadas com **BCrypt** (nunca em texto puro).
- Login devolve a mesma mensagem para e-mail inexistente e senha errada.
- Rotas do paciente exigem código **e** CPF; erro de código ou CPF devolve sempre 404.

## Antes de publicar na internet (importante)

1. Defina a variável **`JWT_SECRET`** com uma chave longa e secreta (mín. 32 caracteres).
   Quem souber a chave pode fabricar tokens.
2. Defina **`ADMIN_SENHA`** com uma senha forte. A senha padrão `vidaplena123` é só para testes.
   Ela só é lida quando o admin é criado; para trocar depois, apague a linha `admin` da tabela
   `usuario` e suba a API de novo.
3. Troque a senha do banco (`DB` no `application.properties`) e não a envie ao Git.
4. Use HTTPS e troque os endereços `http://localhost:8080` em
   `hospital-vida-plena/src/service/api.js` e a origem liberada em `CorsConfig.java`.
5. Troque `spring.jpa.hibernate.ddl-auto=update` por migrações (Flyway).

## Limitações conhecidas

- Quem se cadastra como EQUIPE entra no painel sem aprovação de um admin.
- Não há limite de tentativas de login (proteção contra força bruta).
- Não há "esqueci minha senha", nem envio de e-mail/WhatsApp (o site promete lembretes, mas eles não existem).
- "Meus agendamentos" lista só o que foi feito **neste aparelho** (guardado no navegador).
- Dois pacientes confirmando o mesmo horário no mesmo instante: a checagem é feita no código, não por
  restrição no banco.
- Não há testes automatizados.

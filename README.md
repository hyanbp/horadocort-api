# HoraDoCort — API

API multi-tenant do **HoraDoCort**, SaaS de agendamento para barbearias. Cada barbearia é um **tenant** isolado, identificado por slug no path. Mensageria WhatsApp centralizada via **RelayOS**.
API multi-tenant para SaaS de agendamento de barbearias. Cada barbearia é um **tenant** isolado, identificado por slug no path. Mensageria WhatsApp centralizada via **RelayOS**.

## Stack

- Java 21 + Spring Boot 3.3
- Spring Web + WebFlux (WebClient) + Security + AOP
- Spring Data JPA + PostgreSQL + Flyway
- JWT próprio (HMAC-SHA256, sem libs externas)
- Multi-tenancy: shared DB + `tenant_id` + Hibernate `@Filter` automático

## Como o multi-tenant funciona

1. **Resolução do tenant** vem de duas fontes:
   - **Cliente final** (agendamento): slug no path `/api/v1/t/{slug}/public/...` → `TenantFilter` resolve.
   - **Admin** (painel): `tenantId` dentro do JWT → `JwtAuthFilter` resolve.
   Ambos populam o mesmo `TenantContext` (ThreadLocal).

2. **Isolamento automático**: `TenantFilterAspect` intercepta todo método `@Transactional` e ativa o filtro Hibernate `tenantFilter`, que injeta `WHERE tenant_id = :tenantId` em todas as queries das entidades anotadas (`@Filter`). Você nunca precisa filtrar manualmente — esquecer = vazamento de dados.

3. **Exceções ao filtro**: o `ReminderScheduler` roda cross-tenant (precisa varrer agendamentos de todas barbearias), então usa native queries que ignoram o filtro de propósito.

## Fluxos principais

### Onboarding (self-service)
`POST /api/v1/onboarding` cria tenant + usuário owner + 3 serviços padrão + retorna JWT já logado. Trial de 14 dias.

### Login admin
`POST /api/v1/auth/login` com `{ tenantSlug, email, password }` → retorna JWT.

### Cliente final agenda
Tudo sob `/api/v1/t/{slug}/public/` — sem auth, identificado pelo slug.

## Endpoints

### Públicos
| Método | URL                                              | Descrição                  |
|--------|--------------------------------------------------|----------------------------|
| POST   | /api/v1/onboarding                               | Cadastra nova barbearia    |
| POST   | /api/v1/auth/login                               | Login do admin             |
| GET    | /api/v1/t/{slug}/public/info                     | Info pública da barbearia  |
| GET    | /api/v1/t/{slug}/public/barbers                  | Barbeiros                  |
| GET    | /api/v1/t/{slug}/public/services                 | Serviços                   |
| GET    | /api/v1/t/{slug}/public/availability             | Slots disponíveis          |
| POST   | /api/v1/t/{slug}/public/bookings                 | Cria agendamento           |

### Admin (Bearer token)
| Método | URL                              | Descrição                |
|--------|----------------------------------|--------------------------|
| GET    | /api/v1/admin/barbers            | Lista barbeiros          |
| POST   | /api/v1/admin/barbers            | Cadastra barbeiro        |
| GET    | /api/v1/admin/services           | Lista serviços           |
| POST   | /api/v1/admin/services           | Cadastra serviço         |
| GET    | /api/v1/admin/bookings           | Agenda por barbeiro/dia  |
| DELETE | /api/v1/admin/bookings/{id}      | Cancela agendamento      |

## Variáveis de ambiente

```
DB_URL=jdbc:postgresql://...
DB_USER=...
DB_PASSWORD=...
JWT_SECRET=<min 32 caracteres>
RELAYOS_BASE_URL=https://api.relay-os.lovable.app
RELAYOS_API_KEY=<token>
RELAYOS_WEBHOOK_CONFIRMED=booking-confirmed
RELAYOS_WEBHOOK_REMINDER=booking-reminder
RELAYOS_WEBHOOK_BARBER=barber-new-booking
```

## Payload enviado ao RelayOS

Agora inclui `barbershopName` pro template identificar de qual barbearia é a mensagem:

```json
{
  "tenantId": "uuid",
  "barbershopName": "Barbearia do Carlos",
  "customerName": "João",
  "customerPhone": "+5551999999999",
  "barberName": "Carlos",
  "serviceName": "Corte + barba",
  "date": "28/05/2026",
  "time": "14:30",
  "recipientPhone": "+5551999999999"
}
```

## Teste rápido (curl)

```bash
# 1. Cadastra barbearia
curl -X POST localhost:8080/api/v1/onboarding -H "Content-Type: application/json" -d '{
  "slug": "barbearia-do-carlos",
  "barbershopName": "Barbearia do Carlos",
  "ownerEmail": "carlos@email.com",
  "ownerPassword": "senha12345",
  "ownerName": "Carlos Silva",
  "ownerPhone": "+5551999990000"
}'
# retorna { tenantId, slug, bookingUrl, token }

# 2. Cadastra um barbeiro (usa o token do passo 1)
curl -X POST localhost:8080/api/v1/admin/barbers \
  -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" \
  -d '{"name":"Carlos","phone":"+5551999990000"}'

# 3. Cliente vê barbeiros (público, sem token)
curl localhost:8080/api/v1/t/barbearia-do-carlos/public/barbers
```

## Run local

```bash
docker run --name barber-pg -e POSTGRES_DB=horadocort \
  -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:16
./gradlew bootRun
```

## Próximos passos (TODO)

- Billing real (Stripe ou Asaas) — tabela `subscriptions` já existe
- Verificação de trial expirado (bloquear tenant após 14 dias)
- Rate limiting por tenant
- RLS no Postgres como segunda camada de isolamento
- Painel super-admin (você) pra ver todos os tenants

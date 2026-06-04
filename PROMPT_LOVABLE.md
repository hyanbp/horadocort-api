# Prompt Lovable — HoraDoCort

Cole no Lovable. Monta o frontend completo do SaaS multi-tenant consumindo a `horadocort-api`.

---

Crie uma aplicação Next.js (App Router) com TypeScript, Tailwind e shadcn/ui para o **HoraDoCort**, um SaaS de agendamento de barbearias. O nome da marca é "HoraDoCort" (trocadilho com "hora do corte", o T final é mudo) — use no logo/header. São três áreas:

1. **Landing + Onboarding** (`/`) — vende o produto e cadastra novas barbearias.
2. **Página da barbearia** (`/[slug]`) — onde o cliente final agenda. O slug identifica a barbearia.
3. **Painel admin** (`/[slug]/admin`) — barbeiro/dono gerencia agenda e cadastros, protegido por login.

## Identidade visual

- Minimalista, sofisticada. Inspiração Linear/Vercel.
- Paleta: fundo `#0A0A0A`, superfícies `#141414`, texto `#F5F5F5` / secundário `#A1A1A1`, accent caramelo `#D4A574`, bordas `rgba(255,255,255,0.08)`.
- Inter na UI, Fraunces nas headlines.
- Espaçamento generoso, `rounded-2xl` em cards, transições de 200ms, sem gradientes pesados.

## Config API

`lib/api.ts` com `NEXT_PUBLIC_API_URL` (default `http://localhost:8080`). Helper que injeta `Authorization: Bearer` quando há token no estado.

Endpoints:
- `POST /api/v1/onboarding` → `{ tenantId, slug, bookingUrl, token }`
- `POST /api/v1/auth/login` body `{ tenantSlug, email, password }` → `{ token, tenantName, userName, role }`
- `GET /api/v1/t/{slug}/public/info` → `{ slug, name, openingHour, closingHour }`
- `GET /api/v1/t/{slug}/public/barbers` | `/services` | `/availability?barberId&serviceId&date`
- `POST /api/v1/t/{slug}/public/bookings`
- Admin (com Bearer): `GET/POST /api/v1/admin/barbers`, `/services`, `GET /api/v1/admin/bookings?barberId&date`, `DELETE /api/v1/admin/bookings/{id}`

## 1. Landing + Onboarding (/)

- **Hero:** headline Fraunces "Sua barbearia, agendando sozinha." subtítulo sobre transformar agenda em link + WhatsApp automático. CTA "Começar grátis" (14 dias).
- **Seção de benefícios:** 3 cards — "Link próprio pra seus clientes", "Confirmação e lembrete no WhatsApp", "Painel simples pra sua equipe".
- **Pricing:** 1 plano destacado, ex. "R$ 79/mês — após 14 dias grátis".
- **Onboarding (modal ou /signup):** formulário com nome da barbearia (gera slug automático em tempo real, editável, valida minúsculas/hífens), e-mail, senha, nome do dono, telefone. Ao submeter → POST onboarding → salva token → redireciona pra `/{slug}/admin`. Mostra a URL pública gerada com destaque ("seus clientes vão agendar em barbeariahp.com.br/{slug}").

## 2. Página da barbearia (/[slug])

Carrega `GET /t/{slug}/public/info` no server. Se 404, mostra "Barbearia não encontrada".

**Wizard de agendamento em 5 passos** (uma pergunta por tela, barra de progresso fina):
1. Boas-vindas com nome da barbearia em Fraunces.
2. Escolher serviço (cards com nome, duração, preço R$).
3. Escolher barbeiro (grid de avatares).
4. Data (Calendar, seg-sáb, domingo off, hoje até +30 dias) + horários (grid 3 col, indisponíveis riscados, skeleton no load).
5. Nome + telefone (máscara, envia +55). Card resumo no topo.

Sucesso: checkmark animado, "Pronto, te esperamos." + toast "Confirmação chegou no seu WhatsApp".

## 3. Painel admin (/[slug]/admin)

- **Guard:** sem token → tela de login (`tenantSlug` preenchido do path, pede e-mail + senha). Salva token em estado/cookie httpOnly.
- **Sidebar:** lista de barbeiros (clicar troca a agenda) + link "Configurações".
- **Header:** date picker + contagem do dia.
- **Timeline** vertical 09h-19h, slots de 30 min. Ocupados mostram cliente/serviço/telefone. Clicar abre Sheet com detalhes + "Cancelar" (Dialog de confirmação).
- **Configurações:** abas "Barbeiros" e "Serviços" com listagem + formulário de cadastro (POST nos endpoints admin).

## Componentes

- `lib/api.ts`, `lib/auth.ts` (gerencia token), `lib/format.ts` (BRL, telefone, data pt-BR), `lib/slug.ts` (gera slug a partir do nome).
- `components/onboarding/SignupForm.tsx`
- `components/wizard/*` (ProgressBar, ServiceCard, BarberCard, TimeSlotGrid)
- `components/admin/*` (LoginForm, Timeline, BookingDetailSheet, BarberManager, ServiceManager)

## Estados e detalhes

- Loading: skeletons `#1F1F1F` com shimmer.
- Erros: toast vermelho discreto, linguagem humana.
- Tudo em português brasileiro.
- Preço "R$ 50,00", telefone exibe "(51) 99999-9999" envia "+5551999999999".
- Data "qui, 28 mai" no resumo, "28/05/2026" em campos.
- Mobile-first, botões fixos no rodapé com safe-area no wizard.
- Acessibilidade AA, foco visível, labels em inputs.

`.env.example` com `NEXT_PUBLIC_API_URL=http://localhost:8080`.

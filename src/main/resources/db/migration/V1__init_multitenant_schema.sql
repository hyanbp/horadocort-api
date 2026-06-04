-- Cada barbearia é um tenant
CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    slug VARCHAR(60) UNIQUE NOT NULL,
    name VARCHAR(120) NOT NULL,
    owner_email VARCHAR(180) NOT NULL,
    owner_phone VARCHAR(20),
    plan VARCHAR(20) NOT NULL DEFAULT 'TRIAL',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    opening_hour INT NOT NULL DEFAULT 9,
    closing_hour INT NOT NULL DEFAULT 19,
    timezone VARCHAR(60) NOT NULL DEFAULT 'America/Sao_Paulo',
    trial_ends_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Usuários admin de cada barbearia (login no painel)
CREATE TABLE users (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    email VARCHAR(180) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(120) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'OWNER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, email)
);

CREATE TABLE barbers (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    avatar_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE services (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    duration_minutes INT NOT NULL,
    price_cents INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE bookings (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    barber_id UUID NOT NULL REFERENCES barbers(id),
    service_id UUID NOT NULL REFERENCES services(id),
    customer_name VARCHAR(120) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    start_at TIMESTAMP WITH TIME ZONE NOT NULL,
    end_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL,
    reminder_sent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Cobrança SaaS
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    plan VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    external_customer_id VARCHAR(120),
    external_subscription_id VARCHAR(120),
    current_period_start TIMESTAMP WITH TIME ZONE,
    current_period_end TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Índices críticos pra multi-tenant: tenant_id sempre primeiro
CREATE INDEX idx_users_tenant ON users(tenant_id);
CREATE INDEX idx_barbers_tenant_active ON barbers(tenant_id, active);
CREATE INDEX idx_services_tenant_active ON services(tenant_id, active);
CREATE INDEX idx_bookings_tenant_barber_start ON bookings(tenant_id, barber_id, start_at);
CREATE INDEX idx_bookings_tenant_start_status ON bookings(tenant_id, start_at, status, reminder_sent);
CREATE INDEX idx_subscriptions_tenant ON subscriptions(tenant_id);

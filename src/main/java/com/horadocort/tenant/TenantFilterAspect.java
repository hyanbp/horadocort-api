package com.horadocort.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class TenantFilterAspect {

    public static final String FILTER_NAME = "tenantFilter";
    public static final String FILTER_PARAM = "tenantId";

    @PersistenceContext
    private EntityManager entityManager;

    @Around("@within(org.springframework.transaction.annotation.Transactional) || @annotation(org.springframework.transaction.annotation.Transactional)")
    public Object enableTenantFilter(ProceedingJoinPoint pjp) throws Throwable {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) {
            return pjp.proceed();
        }

        Session session = entityManager.unwrap(Session.class);
        session.enableFilter(FILTER_NAME).setParameter(FILTER_PARAM, tenantId);
        try {
            return pjp.proceed();
        } finally {
            try {
                session.disableFilter(FILTER_NAME);
            } catch (Exception ignored) {
            }
        }
    }
}

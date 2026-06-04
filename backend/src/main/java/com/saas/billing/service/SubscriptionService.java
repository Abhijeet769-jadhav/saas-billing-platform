package com.saas.billing.service;

import com.saas.billing.dto.*;

import java.util.UUID;

public interface SubscriptionService {
    SubscriptionDto getSubscription(UUID organizationId);
    SubscriptionDto subscribe(UUID organizationId, SubscriptionChangeRequest request);
    SubscriptionDto upgrade(UUID organizationId, SubscriptionChangeRequest request);
    SubscriptionDto downgrade(UUID organizationId, SubscriptionChangeRequest request);
    void cancel(UUID organizationId);
    void pause(UUID organizationId);
    void resume(UUID organizationId);
    void renew(UUID organizationId);
    boolean verifyAccess(UUID organizationId, String featureKey, int currentUsage);
}

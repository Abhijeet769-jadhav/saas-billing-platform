package com.saas.billing.service;

import com.saas.billing.dto.PlanDto;

import java.util.List;
import java.util.UUID;

public interface PlanService {
    List<PlanDto> getAllPlans();
    PlanDto getPlanById(UUID planId);
    PlanDto createPlan(PlanDto planDto);
    PlanDto updatePlan(UUID planId, PlanDto planDto);
    void deletePlan(UUID planId);
    void togglePlanStatus(UUID planId, boolean active);
}

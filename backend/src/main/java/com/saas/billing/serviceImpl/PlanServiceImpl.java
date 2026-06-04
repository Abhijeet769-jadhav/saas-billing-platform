package com.saas.billing.serviceImpl;

import com.saas.billing.dto.PlanDto;
import com.saas.billing.dto.PlanFeatureDto;
import com.saas.billing.entity.Plan;
import com.saas.billing.entity.PlanFeature;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.mapper.DtoMapper;
import com.saas.billing.repository.PlanRepository;
import com.saas.billing.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    @Override
    public List<PlanDto> getAllPlans() {
        return planRepository.findByIsActiveTrue().stream()
                .map(DtoMapper::toPlanDto)
                .collect(Collectors.toList());
    }

    @Override
    public PlanDto getPlanById(UUID planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        return DtoMapper.toPlanDto(plan);
    }

    @Override
    @Transactional
    public PlanDto createPlan(PlanDto planDto) {
        Plan plan = Plan.builder()
                .name(planDto.getName())
                .description(planDto.getDescription())
                .amount(planDto.getAmount())
                .currency(planDto.getCurrency() != null ? planDto.getCurrency() : "USD")
                .billingInterval(planDto.getBillingInterval())
                .trialPeriodDays(planDto.getTrialPeriodDays() != null ? planDto.getTrialPeriodDays() : 0)
                .isActive(true)
                .build();

        if (planDto.getFeatures() != null) {
            List<PlanFeature> features = planDto.getFeatures().stream()
                    .map(f -> PlanFeature.builder()
                            .plan(plan)
                            .featureKey(f.getFeatureKey())
                            .featureValue(f.getFeatureValue())
                            .build())
                    .collect(Collectors.toList());
            plan.setFeatures(features);
        }

        Plan saved = planRepository.save(plan);
        return DtoMapper.toPlanDto(saved);
    }

    @Override
    @Transactional
    public PlanDto updatePlan(UUID planId, PlanDto planDto) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        plan.setName(planDto.getName());
        plan.setDescription(planDto.getDescription());
        plan.setAmount(planDto.getAmount());
        plan.setBillingInterval(planDto.getBillingInterval());
        plan.setTrialPeriodDays(planDto.getTrialPeriodDays());

        Plan saved = planRepository.save(plan);
        return DtoMapper.toPlanDto(saved);
    }

    @Override
    @Transactional
    public void deletePlan(UUID planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        planRepository.delete(plan);
    }

    @Override
    @Transactional
    public void togglePlanStatus(UUID planId, boolean active) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        plan.setIsActive(active);
        planRepository.save(plan);
    }
}

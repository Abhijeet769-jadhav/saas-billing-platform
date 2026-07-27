package com.saas.billing.service;

import com.saas.billing.dto.PlanDto;
import com.saas.billing.dto.PlanFeatureDto;
import com.saas.billing.entity.Plan;
import com.saas.billing.entity.PlanFeature;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.repository.PlanRepository;
import com.saas.billing.serviceImpl.PlanServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PlanServiceImpl planService;


    private UUID planId;
    private Plan plan;
    private PlanDto planDto;


    @BeforeEach
    void setUp() {

        planId = UUID.randomUUID();

        PlanFeature feature = PlanFeature.builder()
                .featureKey("users")
                .featureValue("10")
                .build();


        plan = Plan.builder()
                .id(planId)
                .name("Basic Plan")
                .description("Basic subscription")
                .amount(BigDecimal.ZERO)
                .currency("USD")
                .billingInterval("monthly")
                .trialPeriodDays(14)
                .isActive(true)
                .features(new ArrayList<>())
                .build();


        feature.setPlan(plan);
        plan.getFeatures().add(feature);


        PlanFeatureDto featureDto = new PlanFeatureDto();
        featureDto.setFeatureKey("users");
        featureDto.setFeatureValue("10");


        planDto = new PlanDto();
        planDto.setId(planId);
        planDto.setName("Basic Plan");
        planDto.setDescription("Basic subscription");
        planDto.setAmount(BigDecimal.ZERO);
        planDto.setCurrency("USD");
        planDto.setBillingInterval("monthly");
        planDto.setTrialPeriodDays(14);
        planDto.setFeatures(List.of(featureDto));
    }


    @Test
    void shouldGetAllPlansSuccessfully() {

        when(planRepository.findByIsActiveTrue())
                .thenReturn(List.of(plan));


        List<PlanDto> result = planService.getAllPlans();


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Basic Plan", result.get(0).getName());


        verify(planRepository)
                .findByIsActiveTrue();
    }


    @Test
    void shouldReturnEmptyListWhenNoActivePlansExist() {

        when(planRepository.findByIsActiveTrue())
                .thenReturn(new ArrayList<>());


        List<PlanDto> result = planService.getAllPlans();


        assertNotNull(result);
        assertTrue(result.isEmpty());


        verify(planRepository)
                .findByIsActiveTrue();
    }



    @Test
    void shouldGetPlanByIdSuccessfully() {

        when(planRepository.findById(planId))
                .thenReturn(Optional.of(plan));


        PlanDto result = planService.getPlanById(planId);


        assertNotNull(result);
        assertEquals(planId,result.getId());
        assertEquals("Basic Plan",result.getName());


        verify(planRepository)
                .findById(planId);
    }



    @Test
    void shouldThrowExceptionWhenPlanNotFound() {

        when(planRepository.findById(planId))
                .thenReturn(Optional.empty());


        assertThrows(ResourceNotFoundException.class,
                () -> planService.getPlanById(planId));


        verify(planRepository)
                .findById(planId);
    }



    @Test
    void shouldCreatePlanSuccessfully() {

        when(planRepository.save(any(Plan.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));


        PlanDto result =
                planService.createPlan(planDto);


        assertNotNull(result);
        assertEquals("Basic Plan",result.getName());
        assertEquals("USD",result.getCurrency());
        assertEquals(BigDecimal.ZERO,result.getAmount());
        assertEquals(1,result.getFeatures().size());


        verify(planRepository)
                .save(any(Plan.class));
    }



    @Test
    void shouldUpdatePlanSuccessfully() {


        when(planRepository.findById(planId))
                .thenReturn(Optional.of(plan));


        when(planRepository.save(any(Plan.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));


        planDto.setName("Professional Plan");
        planDto.setDescription("Updated Plan");


        PlanDto result =
                planService.updatePlan(planId,planDto);



        assertNotNull(result);
        assertEquals("Professional Plan",result.getName());
        assertEquals("Updated Plan",result.getDescription());


        verify(planRepository)
                .findById(planId);

        verify(planRepository)
                .save(any(Plan.class));
    }



    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingPlan() {


        when(planRepository.findById(planId))
                .thenReturn(Optional.empty());


        assertThrows(ResourceNotFoundException.class,
                () -> planService.updatePlan(planId,planDto));


        verify(planRepository)
                .findById(planId);


        verify(planRepository,never())
                .save(any());
    }



    @Test
    void shouldDeletePlanSuccessfully() {


        when(planRepository.findById(planId))
                .thenReturn(Optional.of(plan));


        planService.deletePlan(planId);


        verify(planRepository)
                .findById(planId);


        verify(planRepository)
                .delete(plan);
    }



    @Test
    void shouldThrowExceptionWhenDeletingNonExistingPlan() {


        when(planRepository.findById(planId))
                .thenReturn(Optional.empty());


        assertThrows(ResourceNotFoundException.class,
                () -> planService.deletePlan(planId));


        verify(planRepository)
                .findById(planId);


        verify(planRepository,never())
                .delete(any());
    }



    @Test
    void shouldActivatePlanSuccessfully() {


        plan.setIsActive(false);


        when(planRepository.findById(planId))
                .thenReturn(Optional.of(plan));


        planService.togglePlanStatus(planId,true);


        assertTrue(plan.getIsActive());


        verify(planRepository)
                .save(plan);
    }



    @Test
    void shouldDeactivatePlanSuccessfully() {


        plan.setIsActive(true);


        when(planRepository.findById(planId))
                .thenReturn(Optional.of(plan));


        planService.togglePlanStatus(planId,false);


        assertFalse(plan.getIsActive());


        verify(planRepository)
                .save(plan);
    }



    @Test
    void shouldThrowExceptionWhenTogglingMissingPlan() {


        when(planRepository.findById(planId))
                .thenReturn(Optional.empty());


        assertThrows(ResourceNotFoundException.class,
                () -> planService.togglePlanStatus(planId,true));


        verify(planRepository,never())
                .save(any());
    }

}
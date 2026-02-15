package com.vehicle.diagnostic.monitoring.service;

import com.vehicle.diagnostic.monitoring.dto.SessionDTO;
import com.vehicle.diagnostic.monitoring.model.DiagnosticSession;
import com.vehicle.diagnostic.monitoring.repository.DiagnosticSessionRepository;
import com.vehicle.diagnostic.monitoring.repository.WorkerEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SessionService
 */
@ExtendWith(MockitoExtension.class)
class SessionServiceTest {
    
    @Mock
    private DiagnosticSessionRepository sessionRepository;
    
    @Mock
    private WorkerEventRepository workerEventRepository;
    
    @Mock
    private ApplicationInsightsService appInsightsService;
    
    @InjectMocks
    private SessionService sessionService;
    
    private DiagnosticSession testSession;
    
    @BeforeEach
    void setUp() {
        testSession = DiagnosticSession.builder()
                .id("1")
                .sessionId("TEST-SESSION-001")
                .vehicleId("VEHICLE-123")
                .vehicleVin("1HGBH41JXMN109186")
                .diagnosticType("FULL_DIAGNOSTIC")
                .status(DiagnosticSession.SessionStatus.COMPLETED)
                .startTime(LocalDateTime.now().minusMinutes(30))
                .endTime(LocalDateTime.now())
                .durationMs(1800000L)
                .completedSteps(6)
                .totalSteps(6)
                .build();
    }
    
    @Test
    void testGetSession_WhenSessionExists() {
        // Arrange
        when(sessionRepository.findBySessionId(anyString())).thenReturn(Optional.of(testSession));
        when(workerEventRepository.findBySessionIdOrderByStepNumberAsc(anyString())).thenReturn(java.util.Collections.emptyList());
        
        // Act
        SessionDTO result = sessionService.getSession("TEST-SESSION-001");
        
        // Assert
        assertNotNull(result);
        assertEquals("TEST-SESSION-001", result.getSessionId());
        assertEquals("VEHICLE-123", result.getVehicleId());
        assertEquals("COMPLETED", result.getStatus());
        
        verify(sessionRepository, times(1)).findBySessionId("TEST-SESSION-001");
    }
    
    @Test
    void testGetSession_WhenSessionNotInLocalDb() {
        // Arrange
        when(sessionRepository.findBySessionId(anyString())).thenReturn(Optional.empty());
        when(appInsightsService.querySessionData(anyString())).thenReturn(java.util.Collections.emptyMap());
        
        // Act
        SessionDTO result = sessionService.getSession("NON-EXISTENT-SESSION");
        
        // Assert
        assertNotNull(result);
        assertEquals("NON-EXISTENT-SESSION", result.getSessionId());
        
        verify(appInsightsService, times(1)).querySessionData("NON-EXISTENT-SESSION");
    }
    
    @Test
    void testSaveSession() {
        // Arrange
        when(sessionRepository.save(any(DiagnosticSession.class))).thenReturn(testSession);
        
        // Act
        DiagnosticSession result = sessionService.saveSession(testSession);
        
        // Assert
        assertNotNull(result);
        assertEquals("TEST-SESSION-001", result.getSessionId());
        
        verify(sessionRepository, times(1)).save(testSession);
    }
    
    @Test
    void testCalculateProgressPercentage() {
        // Arrange
        when(sessionRepository.findBySessionId(anyString())).thenReturn(Optional.of(testSession));
        when(workerEventRepository.findBySessionIdOrderByStepNumberAsc(anyString())).thenReturn(java.util.Collections.emptyList());
        
        // Act
        SessionDTO result = sessionService.getSession("TEST-SESSION-001");
        
        // Assert
        assertNotNull(result.getProgressPercentage());
        assertEquals(100.0, result.getProgressPercentage(), 0.01);
    }
}

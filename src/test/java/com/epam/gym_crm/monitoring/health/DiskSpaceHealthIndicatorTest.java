package com.epam.gym_crm.monitoring.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiskSpaceHealthIndicatorTest {

    @Mock
    private File mockPath;

    private DiskSpaceHealthIndicator diskSpaceHealthIndicator;
    
    private static final long GIGABYTE = 1024L * 1024 * 1024;
    private static final long THRESHOLD = 1 * GIGABYTE;

    @Test
    void testHealth_whenFreeSpaceIsAboveThreshold_shouldReturnUp() {
        
        when(mockPath.getFreeSpace()).thenReturn(THRESHOLD + 1); 
        
        when(mockPath.getTotalSpace()).thenReturn(10 * GIGABYTE);

        diskSpaceHealthIndicator = new DiskSpaceHealthIndicator(mockPath);

        
        // 3. health() metodunu çağırıyoruz.
        Health health = diskSpaceHealthIndicator.health();

        // --- ASSERT (Doğrulama) ---

        // 4. Sonuçları kontrol ediyoruz.
        assertNotNull(health);
        assertEquals(Status.UP, health.getStatus(), "Health status should be UP when there is enough disk space");
        assertTrue(health.getDetails().containsKey("free_space"), "Details should include free_space info");
    }

    @Test
    void testHealth_whenFreeSpaceIsBelowThreshold_shouldReturnDown() {

        when(mockPath.getFreeSpace()).thenReturn(THRESHOLD - 1);
        when(mockPath.getTotalSpace()).thenReturn(10 * GIGABYTE);
        
        // 2. Test edilecek sınıfı, bu sahte nesneyle oluşturuyoruz.
        diskSpaceHealthIndicator = new DiskSpaceHealthIndicator(mockPath);


        // 3. health() metodunu çağırıyoruz.
        Health health = diskSpaceHealthIndicator.health();

        // --- ASSERT (Doğrulama) ---

        // 4. Sonuçları kontrol ediyoruz.
        assertNotNull(health);
        assertEquals(Status.DOWN, health.getStatus(), "Health status should be DOWN when disk space is low");
        assertTrue(health.getDetails().containsKey("error"), "Details should include an error message");
        assertEquals("Disk space is critically low!", health.getDetails().get("error"));
    }
}
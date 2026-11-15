package com.renewcommunity.renewcommunity.service.impl;

import com.renewcommunity.renewcommunity.model.vo.ServerStatusVO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SystemMonitorServiceImpl} 单元测试。
 */
class SystemMonitorServiceImplTest {

    @Test
    void shouldReturnServerStatus() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.application.name", "RenewCommunity");

        SystemMonitorServiceImpl service = new SystemMonitorServiceImpl(environment);
        ServerStatusVO status = service.getServerStatus();

        assertNotNull(status.os());
        assertNotNull(status.memory());
        assertNotNull(status.jvm());
        assertNotNull(status.app());
        assertEquals("RenewCommunity", status.app().name());

        assertTrue(status.os().availableProcessors() > 0);
        assertTrue(status.os().cpuUsage() >= -1 && status.os().cpuUsage() <= 100);

        assertTrue(status.memory().heapUsed() >= 0);
        assertTrue(status.memory().heapMax() > 0);
        assertTrue(status.memory().heapUsage() >= 0 && status.memory().heapUsage() <= 100);

        assertTrue(status.jvm().uptimeSeconds() >= 0);
        assertTrue(status.jvm().threadCount() > 0);
        assertTrue(status.jvm().javaVersion() != null);
    }
}

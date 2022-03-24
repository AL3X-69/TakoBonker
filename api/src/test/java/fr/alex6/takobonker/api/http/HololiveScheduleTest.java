/*
 * Copyright 2022 Alex6
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.alex6.takobonker.api.http;import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.alex6.takobonker.api.http.HololiveSchedule;
import fr.alex6.takobonker.api.utils.CacheManager;
import fr.alex6.takobonker.api.utils.Commons;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class HololiveScheduleTest {
    public static HololiveSchedule schedule;
    public static CacheManager cacheManager;

    @BeforeAll
    static void init() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModules(Commons.getTakoModule(), new JavaTimeModule());
        cacheManager = new CacheManager(objectMapper, "test-cache");
        schedule = new HololiveSchedule(cacheManager);
    }

    @Test
    public void scheduleChannels_notNull() {
        Assertions.assertNotNull(schedule.getChannels());
    }

    @Test
    public void scheduleChannels_notEmpty() {
        Assertions.assertTrue(schedule.getChannels().length > 0);
    }

    @Test
    public void upcomingStreams_doesNotThrow_IOException() {
        Assertions.assertDoesNotThrow(() -> schedule.getUpcomingStreams(""));
        Assertions.assertDoesNotThrow(() -> schedule.getUpcomingStreams("/china"));
        Assertions.assertDoesNotThrow(() -> schedule.getUpcomingStreams("/stars"));
        Assertions.assertDoesNotThrow(() -> schedule.getUpcomingStreams("/hololive"));
        Assertions.assertDoesNotThrow(() -> schedule.getUpcomingStreams("/holostars"));
        Assertions.assertDoesNotThrow(() -> schedule.getUpcomingStreams("/english"));
        Assertions.assertDoesNotThrow(() -> schedule.getUpcomingStreams("/indonesia"));
    }

    @AfterAll
    static void clean() {
        cacheManager.close();
    }
}

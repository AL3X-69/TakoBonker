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

package fr.alex6.discord.takobonker.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class CachedResource<T> {
    private final T resource;
    private final long timestamp;

    protected CachedResource(T resource, long timestamp) {
        this.resource = resource;
        this.timestamp = timestamp;
    }

    public T getCachedResource() {
        return resource;
    }

    public long getCacheTimestamp() {
        return timestamp;
    }

    public LocalDateTime getCacheDateTime() {
        return LocalDateTime.ofEpochSecond(getCacheTimestamp(), 0, ZoneOffset.UTC);
    }

}
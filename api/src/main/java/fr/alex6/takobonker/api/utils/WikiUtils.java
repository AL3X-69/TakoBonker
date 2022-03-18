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

package fr.alex6.takobonker.api.utils;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class WikiUtils {
    public static final String WIKI_THUMB_TEMPLATE = "https://hololive.wiki/w/thumb.php?f=%s&w=500";
    private static final Logger logger = LoggerFactory.getLogger(WikiUtils.class);

    public static @NotNull String wikiTextToMarkdown(@NotNull String text) {
        // TODO finish wikitext parsing
        String[] refs = StringUtils.substringsBetween(text, "<ref>", "</ref>");
        if (refs != null) {
            for (String s : refs) {
                text = text.replace(s, "");
            }
        }
        String[] galleries = StringUtils.substringsBetween(text, "<ref>", "</ref>");
        if (galleries != null) {
            for (String s : galleries) {
                text = text.replace(s, "");
            }
        }
        String[] links = StringUtils.substringsBetween(text, " [", "] ");
        if (links != null) {
            for (String link : links) {
                String[] parts = link.split(" ", 1);
                text = text.substring(0, text.indexOf(link))+String.format("[%s](%s)", parts[1], parts[0])+text.substring(text.indexOf(link)+link.length());
            }
        }
        return text
                .replaceAll("<ref>", "")
                .replaceAll("</ref>", "")
                .replaceAll("<gallery>", "")
                .replaceAll("</gallery>", "")
                .replaceAll("=== ", "**")
                .replaceAll(" ===", "**")
                .replaceAll("'''", "``")
                .replaceAll("::::", "> ")
                .replaceAll("== ", "__**")
                .replaceAll(" ==", "**__")
                .replaceAll("\\[\\[", "__")
                .replaceAll("]]", "__")
                .replaceAll("<big>", "**")
                .replaceAll("</big>", "**")
                .replaceAll("<br/>", "\n")
                .trim();
    }

    public static @Nullable String getPortraitFile(@NotNull String member) {
        String template = "%s_-_Portrait_01.png";
        try {
            return String.format(template, URLEncoder.encode(member.replaceAll(" ", "_"), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("UTF-8 Encoding is not supported", e);
            return null;
        }
    }

    public static String getMemberThumbnail(String member) {
        return String.format(WIKI_THUMB_TEMPLATE, getPortraitFile(member));
    }
}

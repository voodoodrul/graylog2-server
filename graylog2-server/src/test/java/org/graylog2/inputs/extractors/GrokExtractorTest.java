/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog2.inputs.extractors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.graylog2.ConfigurationException;
import org.graylog2.grok.GrokPattern;
import org.graylog2.plugin.LocalMetricRegistry;
import org.graylog2.plugin.inputs.Converter;
import org.graylog2.plugin.inputs.Extractor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GrokExtractorTest {
    private static final Set<GrokPattern> PATTERNS = ImmutableSet.of(
            GrokPattern.create("BASE10NUM", "(?<![0-9.+-])(?>[+-]?(?:(?:[0-9]+(?:\\.[0-9]+)?)|(?:\\.[0-9]+)))"),
            GrokPattern.create("NUMBER", "(?:%{BASE10NUM:UNWANTED})"),
            GrokPattern.create("DATA", ".*?"));

    @Test
    public void testDatatypeExtraction() {
        final GrokExtractor extractor = makeExtractor("%{NUMBER:number;int}");

        final Extractor.Result[] results = extractor.run("199999");
        assertEquals("NUMBER is marked as UNWANTED and does not generate a field", 1, results.length);
        assertEquals(Integer.class, results[0].getValue().getClass());
        assertEquals(199999, results[0].getValue());
    }

    @Test
    public void testDateConversion() {
        final String pattern = "<\\d+>%{DATA:timestamp;date;yyyy-MM-dd'T'HH:mm:ss.SSSZ} %{DATA:cst_hostname}";
        final String message = "<166>2015-07-31T10:05:36.773Z esxi-e1b2.local.xxxx.it Vpxa: [4A2D4B90 verbose 'VpxaHalCnxHostagent' opID=WFU-65ff5b37] [WaitForUpdatesDone] Completed callback";
        final GrokExtractor extractor = makeExtractor(pattern);
        final Extractor.Result[] results = extractor.run(message);

        assertThat(results)
                .hasSize(2)
                .contains(new Extractor.Result(new DateTime(2015, 7, 31, 10, 5, 36, 773, DateTimeZone.UTC).toDate(), "timestamp", -1, -1))
                .contains(new Extractor.Result("esxi-e1b2.local.xxxx.it", "cst_hostname", -1, -1));
    }

    private GrokExtractor makeExtractor(String pattern) {
        Map<String, Object> config = Maps.newHashMap();
        config.put("grok_pattern", pattern);

        try {
            return new GrokExtractor(new LocalMetricRegistry(),
                    PATTERNS,
                                     "id",
                                     "title",
                                     0,
                                     Extractor.CursorStrategy.COPY,
                                     "message",
                                     "message",
                                     config,
                                     "admin",
                                     Lists.<Converter>newArrayList(),
                                     Extractor.ConditionType.NONE,
                                     null);
        } catch (Extractor.ReservedFieldException | ConfigurationException e) {
            fail("Test setup is wrong: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
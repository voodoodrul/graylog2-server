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
package org.graylog2.grok;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import javax.annotation.Nullable;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonAutoDetect
public class GrokPattern {
    @Id
    @org.mongojack.ObjectId
    @Nullable
    public ObjectId id;
    public String name;
    public String pattern;
    @Nullable
    public String contentPack;

    public static GrokPattern create(@Nullable ObjectId id, @NotEmpty String name, @NotNull String pattern, @Nullable String contentPack) {
        final GrokPattern grokPattern = new GrokPattern();

        grokPattern.id = id;
        grokPattern.name = checkNotNull(name);
        grokPattern.pattern = checkNotNull(pattern);
        grokPattern.contentPack = contentPack;

        return grokPattern;
    }

    public static GrokPattern create(@NotEmpty String name, @NotNull String pattern) {
        final GrokPattern grokPattern = new GrokPattern();

        grokPattern.name = checkNotNull(name);
        grokPattern.pattern = checkNotNull(pattern);

        return grokPattern;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("pattern", pattern)
                .add("contentPack", contentPack)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final GrokPattern that = (GrokPattern) o;
        return Objects.equals(this.name, that.name) && Objects.equals(this.pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, pattern);
    }
}

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
package org.graylog2.restroutes.factories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.graylog2.restroutes.interceptors.BasicAuthInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;

import javax.inject.Inject;

public class RestAdapterFactory  {
    private final ObjectMapper objectMapper;

    @Inject
    public RestAdapterFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JodaModule());
    }

    public RestAdapterFactory() {
        this(new ObjectMapper());
    }

    private RestAdapter.Builder getBuilder(String url) {
        final RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setEndpoint(url);
        builder.setConverter(new JacksonConverter(objectMapper));
        return builder;
    }

    public RestAdapter create(String url) {
        return getBuilder(url).build();
    }

    public RestAdapter create(String url, String user, String password) {
        return getBuilder(url)
                .setRequestInterceptor(new BasicAuthInterceptor(user, password))
                .build();
    }
}

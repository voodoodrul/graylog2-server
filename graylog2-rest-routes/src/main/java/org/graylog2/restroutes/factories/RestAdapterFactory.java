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

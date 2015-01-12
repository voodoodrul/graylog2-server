package org.graylog2.restroutes.factories;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog2.restroutes.interceptors.BasicAuthInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;

import javax.inject.Inject;
import javax.inject.Provider;

public class RestAdapterFactory  {
    private final Provider<ObjectMapper> objectMapperProvider;

    @Inject
    public RestAdapterFactory(Provider<ObjectMapper> objectMapperProvider) {
        this.objectMapperProvider = objectMapperProvider;
    }

    private RestAdapter.Builder getBuilder(String url) {
        final RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setEndpoint(url);
        builder.setConverter(new JacksonConverter(objectMapperProvider.get()));
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

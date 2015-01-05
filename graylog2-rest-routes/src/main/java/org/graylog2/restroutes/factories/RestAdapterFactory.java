package org.graylog2.restroutes.factories;

import org.graylog2.restroutes.interceptors.BasicAuthInterceptor;
import org.graylog2.shared.bindings.providers.ObjectMapperProvider;
import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;

import javax.inject.Inject;

public class RestAdapterFactory  {
    private final ObjectMapperProvider objectMapperProvider;

    @Inject
    public RestAdapterFactory(ObjectMapperProvider objectMapperProvider) {
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

package org.graylog2.restroutes.interceptors;

import retrofit.RequestInterceptor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.DatatypeConverter;

public class BasicAuthInterceptor implements RequestInterceptor {
    private final String username;
    private final String password;

    @Inject
    public BasicAuthInterceptor(@Named("username") String username,
                                @Named("password") String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("Authorization", getAuthenticationHeader(username, password));
    }

    private String getAuthenticationHeader(String username, String password) {
        return "Basic " + DatatypeConverter.printBase64Binary((username + ":" + password).getBytes());
    }
}

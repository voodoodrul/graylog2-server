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

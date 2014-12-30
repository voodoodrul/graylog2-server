/**
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog2.restroutes.internal;

import com.sun.codemodel.*;
import org.graylog2.restroutes.PathMethod;
import retrofit.http.Body;
import retrofit.http.Path;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dennis Oelkers <dennis@torch.sh>
 */
public class RouteClassGenerator {
    private final JCodeModel codeModel;
    private final String packagePrefix;

    private final static Map<Class<?>, Class<?>> httpMethodClassMapper = new HashMap<Class<?>, Class<?>>() {{
        put(GET.class, retrofit.http.GET.class);
        put(PUT.class, retrofit.http.PUT.class);
        put(DELETE.class, retrofit.http.DELETE.class);
        put(POST.class, retrofit.http.POST.class);
    }};

    public RouteClassGenerator(String packagePrefix, JCodeModel codeModel) {
        this.packagePrefix = packagePrefix;
        this.codeModel = codeModel;
    }

    private Class<?> mapHttpMethodClass(Class<?> httpMethodClass) {
        return httpMethodClassMapper.get(httpMethodClass);
    }

    public JDefinedClass generate(RouteClass routeClass) {
        JDefinedClass definedClass;
        try {
            definedClass = codeModel._class(packagePrefix + "." + routeClass.getKlazz().getSimpleName(), ClassType.INTERFACE);
        } catch (JClassAlreadyExistsException e) {
            System.out.println("Class " + routeClass.getKlazz().getSimpleName() + " already exists");
            e.printStackTrace();
            return null;
        }

        for (Route route : routeClass.getRoutes()) {
            JMethod method = definedClass.method(JMod.PUBLIC, route.getReturnType(), route.getMethod().getName());
            method.annotate(codeModel.ref(mapHttpMethodClass(route.getHttpMethod()))).param("value", route.getPath());
            //String path = route.getPath();
            for (Map.Entry<PathParam, Class<?>> entry : route.getPathParams().entrySet()) {
                String fieldName = entry.getKey().value();
                method.param(entry.getValue(), fieldName).annotate(Path.class).param("value", fieldName);
                //path = path.replace("{" + fieldName + "}", "\"+"+fieldName+"+\"");

            }

            if (route.getBodyType() != null)
                method.param(route.getBodyType(), "body").annotate(Body.class);
            /*JBlock block = method.body();
            block.directStatement("return new PathMethod(\"" + route.getHttpMethod() + "\", \"" + path + "\");");*/
        }

        return definedClass;
    }
}

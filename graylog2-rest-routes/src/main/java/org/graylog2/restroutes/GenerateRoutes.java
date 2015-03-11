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
package org.graylog2.restroutes;

import com.sun.codemodel.*;
import org.graylog2.restroutes.internal.ResourceRoutesParser;
import org.graylog2.restroutes.internal.RouteClassGenerator;
import org.graylog2.restroutes.internal.RouteClass;
import org.graylog2.restroutes.internal.RouterGenerator;
import retrofit.Endpoint;
import retrofit.RestAdapter;
import org.graylog2.shared.rest.resources.RestResource;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Dennis Oelkers <dennis@torch.sh>
 */
public class GenerateRoutes {
    private static final String packagePrefix = "org.graylog2.restroutes.generated";
    private static final String sharedRouterName = packagePrefix + ".shared.NodeAPI";
    private static final String serverRouterName = packagePrefix + ".server.ServerAPI";
    private static final String radioRouterName = packagePrefix + ".radio.RadioAPI";
    private static final String sharedPackageName = "org.graylog2.shared.rest.resources";
    private static final String serverPackageName = "org.graylog2.rest.resources";
    private static final String radioPackageName = "org.graylog2.radio.rest.resources";

    public static void main(String[] argv) {
        // Just "touching" class in server jar so it gets loaded.
        RestResource resource = null;

        JCodeModel codeModel = new JCodeModel();

        final JDefinedClass sharedRouter = generateRoutes(codeModel, sharedRouterName, sharedPackageName, packagePrefix + ".shared");
        generateRoutes(codeModel, serverRouterName, serverPackageName, packagePrefix + ".server", sharedRouter);
        generateRoutes(codeModel, radioRouterName, radioPackageName, packagePrefix + ".radio", sharedRouter);

        try {
            File dest = new File(argv[0]);
            codeModel.build(dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JDefinedClass generateRoutes(JCodeModel codeModel, String dstRouterClassName, String srcPackageName, String dstPackageName) {
        return generateRoutes(codeModel, dstRouterClassName, srcPackageName, dstPackageName, null);
    }

    private static JDefinedClass generateRoutes(JCodeModel codeModel, String dstRouterClassName, String srcPackageName, String dstPackageName, JClass routerBaseClass) {
        final JDefinedClass router;
        try {
            router = generateRouterClass(codeModel, dstRouterClassName, routerBaseClass);
        } catch (JClassAlreadyExistsException e) {
            e.printStackTrace();
            System.exit(-1);
            return null;
        }

        final ResourceRoutesParser parser = new ResourceRoutesParser(srcPackageName);

        final List<RouteClass> routeClassList = parser.buildClasses();

        final RouteClassGenerator generator = new RouteClassGenerator(dstPackageName, codeModel);

        final RouterGenerator routerGenerator = new RouterGenerator(router, generator);
        routerGenerator.build(routeClassList);

        return router;
    }

    private static JDefinedClass generateRouterClass(JCodeModel codeModel, String name, JClass routerBaseClass) throws JClassAlreadyExistsException {
        final JDefinedClass routerClass;
        if (routerBaseClass == null)
            routerClass = codeModel._class(name);
        else
            routerClass = codeModel._class(name)._extends(routerBaseClass);
        final String restAdapterFieldName = "restAdapter";
        routerClass.field(JMod.PRIVATE, RestAdapter.class, restAdapterFieldName);
        JMethod constructor = routerClass.constructor(JMod.PUBLIC);
        final JVar restAdapterParam = constructor.param(RestAdapter.class, restAdapterFieldName);
        constructor.annotate(Inject.class);
        JBlock constructorBody = constructor.body();
        if (routerBaseClass != null)
            constructorBody.invoke("super").arg(restAdapterParam);
        constructorBody.assign(JExpr._this().ref(restAdapterFieldName), JExpr.ref(restAdapterFieldName));

        return routerClass;
    }
}

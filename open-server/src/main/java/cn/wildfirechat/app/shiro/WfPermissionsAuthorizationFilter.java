package cn.wildfirechat.app.shiro;

import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class WfPermissionsAuthorizationFilter extends PermissionsAuthorizationFilter {
    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws IOException {
        if (WebUtils.toHttp(request).getMethod().equalsIgnoreCase("OPTIONS")){
            return true;
        }
        return super.isAccessAllowed(request, response, mappedValue);
    }
}

package cn.jorian.jorianframework.config.jwt;

import cn.jorian.jorianframework.common.response.ResponseCode;
import cn.jorian.jorianframework.common.response.SystemResponse;
import cn.jorian.jorianframework.core.account.service.impl.LoginService;
import com.alibaba.fastjson.JSON;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Auther: jorian
 * @Date: 2019/4/17 17:01
 * @Description:
 */
public class JwtFilter extends BasicHttpAuthenticationFilter {
     private Logger log = LoggerFactory.getLogger(this.getClass());
    //
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        log.info("permission vification run...");
        HttpServletResponse response1 = (HttpServletResponse)response;
        if(!this.isLoginAttempt(request,response)){
            this.writerResponse(response1, ResponseCode.PERMISSIN_FAIL.code,ResponseCode.PERMISSIN_FAIL.msg);
            //重定向到登录页

        }
        try {
            this.executeLogin(request,response);
        } catch (Exception e) {
            e.printStackTrace();
            writerResponse(response1, ResponseCode.SIGN_IN_USERNAME_PASSWORD_FAIL.code,ResponseCode.SIGN_IN_USERNAME_PASSWORD_FAIL.msg);
        }
        Subject subject = getSubject(request, response);
        if(null != mappedValue){
            String[] value = (String[])mappedValue;
            for (String permission : value) {
                if(permission==null || "".equals(permission.trim())){
                    continue;
                }
                if(subject.isPermitted(permission)){
                    return true;
                }
            }
        }
        if (null == subject.getPrincipal()) {//无凭证示没有登录，返回登录提示
            writerResponse(response1,ResponseCode.TOKEN_EXPIRED.code,ResponseCode.TOKEN_EXPIRED.msg);
        }else{
            writerResponse(response1,ResponseCode.PERMISSIN_FAIL.code,ResponseCode.PERMISSIN_FAIL.msg);
        }
        return false;
    }

    //是否带权限标志J-Token
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        String token = this.getAuthzHeader(request);
        System.out.println(token);
        return token != null;
    }
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        return LoginService.executeLogin(request);
    }
    //
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        return false;
    }


    @Override
    protected String getAuthzHeader(ServletRequest request) {
        HttpServletRequest request1 = (HttpServletRequest) request;
        return  request1.getHeader("J-Token");
    }

    private void writerResponse(HttpServletResponse response,Integer status,String content){
        response.setHeader("Content-Type", "application/json;charset=utf-8");
        try {
            response.getWriter().write(JSON.toJSONString(SystemResponse.builder()
                    .code(status)
                    .msg(content)
                    .build()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
package hello.exception;


import java.util.List;
import hello.exception.filter.LogFilter;
import hello.exception.interceptor.LoginInterceptor;
import hello.exception.resolver.MyHandlerExceptionHandler;
import hello.exception.resolver.UserHandlerExceptionResolver;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor()).order(1).addPathPatterns("/**")
                .excludePathPatterns("/css/**", "*.ico", "/error"/*, "/error-page/**"*/); // 오류 페이지 경로 뺌
    }


    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(new MyHandlerExceptionHandler());
        resolvers.add(new UserHandlerExceptionResolver());
    }


    //@Bean
    public FilterRegistrationBean<LogFilter> logFilter() {
        FilterRegistrationBean<LogFilter> loginFilterRegistrationBean = new FilterRegistrationBean<>();
        loginFilterRegistrationBean.setFilter(new LogFilter());
        loginFilterRegistrationBean.setOrder(1);
        loginFilterRegistrationBean.addUrlPatterns("/*");
        loginFilterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR);
        //loginFilterRegistrationBean.setDispatcherTypes(DispatcherType.ERROR);

        return loginFilterRegistrationBean;
    }
}

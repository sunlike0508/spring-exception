package hello.exception;


import hello.exception.filter.LogFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
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

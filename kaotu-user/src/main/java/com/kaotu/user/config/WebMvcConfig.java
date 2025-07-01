package com.kaotu.user.config;


import com.kaotu.user.interceptor.UserIdInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置类 ！
 * <p>
 * 主要用于配置消息转换器，使用FastJson作为JSON处理工具
 * </p>
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

/*    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 自己提供一个 FastJsonHttpMessageConverter 实例
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        FastJsonConfig config = new FastJsonConfig();
        // 设置解析式日期的格式
        config.setDateFormat("yyyy-MM-dd");
        // 设置解析时编码格式为 UTF-8
        config.setCharset(StandardCharsets.UTF_8);
        config.setSerializerFeatures(
                SerializerFeature.WriteClassName,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.PrettyFormat,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteNullStringAsEmpty
        );
        converter.setFastJsonConfig(config);
        converters.add(converter);
    }*/

    @Autowired
    private UserIdInterceptor userIdInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userIdInterceptor)
                .addPathPatterns("/**") // 拦截所有请求路径
                .excludePathPatterns("/user/user/login", "/user/user/register"); // 排除登录、注册和忘记密码接口
    }
}

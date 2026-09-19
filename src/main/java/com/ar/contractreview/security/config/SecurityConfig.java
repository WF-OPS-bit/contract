package com.ar.contractreview.security.config;
import com.ar.contractreview.security.filter.LoginFilter;
import com.ar.contractreview.security.filter.MyAuthenticationFilter;
import com.ar.contractreview.security.service.IJwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;

/**
 * @author xiaobobo
 * @title: SecurityConfig
 * @projectName ar_26_springboot-parent
 * @description: SpringSecurity的配置
 * @date 2026/9/3  13:33
 */
@SpringBootConfiguration
@EnableMethodSecurity(prePostEnabled = true,securedEnabled = true)      //开启Security的全局配置
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;


    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private LogoutHandler logoutHandler;

    @Autowired
    private IJwtService jwtService;


    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    /**
     * 密码加密器：数据库密码是 BCrypt（demo 的 MD5 无法匹配）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 新版本中这个authenticationManager对象是自己创建的
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    /**
     * 核心过滤器链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 禁用CSRF（高版本推荐使用配置方式）
        http.csrf(csrf -> csrf.disable());

        // 异常处理配置
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
        );

        // 请求授权配置
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/user/login").permitAll() // 登录接口放行（进chain走LoginFilter）
                .anyRequest().authenticated() // 任何请求登录之后才能访问
        );

        // 登出配置
        http.logout(logout -> logout
                .logoutUrl("/user/logout") // 设置退出功能的地址
                .addLogoutHandler(logoutHandler) // 设置退出功能的处理类
        );

        // 添加自定义过滤器（注意：高版本中LoginFilter需继承AbstractAuthenticationProcessingFilter）
        http.addFilter(new LoginFilter(authenticationManager(),jwtService));
        http.addFilter(new MyAuthenticationFilter(authenticationManager(),jwtService));

        // 启用HTTP Basic认证（可选，通常JWT场景下可注释）
//        http.httpBasic(Customizer.withDefaults());

        return http.build();
    }

    /**
     * 配置不需要拦截的静态资源和公开路径
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                // 静态资源（前端独立部署，登录用 permitAll 放行，不在此忽略）
                "/favicon.ico", "/static/**", "/css/**", "/js/**", "/images/**"
        );
    }
}

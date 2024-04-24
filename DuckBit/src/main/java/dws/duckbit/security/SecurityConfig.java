package dws.duckbit.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	public final RepositoryUserDetailsService userDetailService;

	public SecurityConfig(RepositoryUserDetailsService userDetailService) {
		this.userDetailService = userDetailService;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

		authProvider.setUserDetailsService(userDetailService);
		authProvider.setPasswordEncoder(passwordEncoder());

		return authProvider;
	}


	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		String[] userEndpoints = {"/user", "/user/", "/shop", "/shop/", "/query", "/query/", "/upload_image",
				"/upload_image/", "/download_image", "/download_image/", "/delete_image", "/delete_image/",
				"/buy_combo", "/buy_combo/", "/download_combo", "/download_combo/", "/add_credits", "/add_credits/", "/success"};
		String[] adminEndpoints = {"/admin", "/admin/", "/users", "/users/", "/upload_leak", "/upload_leak/", "/delete_leak/*","/delete_leak/*/",
				"/create combo", "/create_combo/", "/delete_combo/*", "/delete_combo/*/", "/edit_combo", "/edit_combo/", "/success,","/delete_user/*","/delete_user/*/"
				};
		http.authenticationProvider(authenticationProvider());

		http
				.authorizeHttpRequests(authorize -> authorize
						// PUBLIC PAGES
						.requestMatchers("/", "/images/**", "/css/**", "/gifs/**", "/register", "/error","/api/**").permitAll()
						// PRIVATE PAGES
						.requestMatchers(userEndpoints).hasAnyRole("USER")
						.requestMatchers(adminEndpoints).hasAnyRole("ADMIN")
				)
				.formLogin(formLogin -> formLogin
						.loginPage("/login")
						.failureUrl("/error")
						.defaultSuccessUrl("/success")
						.permitAll()
				)
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/")
						.permitAll()
				);

		// Disable CSRF at the moment
		http.csrf(csrf -> csrf.disable());

		return http.build();
	}
}

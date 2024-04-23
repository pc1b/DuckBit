package dws.duckbit.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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

		http.authenticationProvider(authenticationProvider());

		http
				.authorizeHttpRequests(authorize -> authorize
						// PUBLIC PAGES
						.requestMatchers("/", "/images/**", "/css/**", "/gifs/**", "/register").permitAll()
						// PRIVATE PAGES
						.requestMatchers("/user").hasAnyRole("USER")
						.requestMatchers("/admin").hasAnyRole("ADMIN")
				)
				.formLogin(formLogin -> formLogin
						.loginPage("/login")
						.failureUrl("/error")
						.defaultSuccessUrl("/")
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

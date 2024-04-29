package dws.duckbit.security;

import dws.duckbit.security.jwt.JwtRequestFilter;
import dws.duckbit.security.jwt.UnauthorizedHandlerJwt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	public final RepositoryUserDetailsService userDetailService;
	private final UnauthorizedHandlerJwt unauthorizedHandlerJwt;
	private final JwtRequestFilter jwtRequestFilter;

	public SecurityConfig(RepositoryUserDetailsService userDetailService, UnauthorizedHandlerJwt unauthorizedHandlerJwt, JwtRequestFilter jwtRequestFilter) {
		this.userDetailService = userDetailService;
		this.unauthorizedHandlerJwt = unauthorizedHandlerJwt;
		this.jwtRequestFilter = jwtRequestFilter;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

		authProvider.setUserDetailsService(userDetailService);
		authProvider.setPasswordEncoder(passwordEncoder());

		return authProvider;
	}
	@Bean
	@Order(1)
	public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
		String[] userGETEndpoints = {"/api/user/", "/api/user", "/api/user/combo/**","/api/combo", "/api/combo/", "/api/query", "/api/query/",
				"/api/combo/buy/*", "/api/combo/buy/*/","/api/combo/*", "/api/combo/*/", "/api/credits", "/api/credits/"
				, "/api/image", "/api/image/", "/api/combo/*/file", "/api/combo/*/file/"};
		String[] userPOSTEndpoints ={"/api/image", "/api/image/"};
		String[] userDELEndpoints ={"/api/image", "/api/image/", "/api/combo/*", "/api/combo/*/", "/api/user/*", "/api/user/*/"};
		String[] userPUTEndpoints ={"/api/user", "/api/user/"};

		String[] adminGETEndpoints = {"/api/user/number", "/api/user/number/", "/api/user/all",
				"/api/user/all/", "/api/leak/**", "/api/leak/*/combos", "/api/leak/*/combos/", "api/combo/sold/number/",
				"api/combo/sold/number"};
		String[] adminPOSTEndpoints = {"/api/leak/", "/api/leak", "/api/combo/", "/api/combo"};
		String[] adminDELEndpoints = {"/api/leak/*/", "/api/leak/*", "/api/combo/*", "/api/combo/*/", "/api/user/*", "/api/user/*/"};
		String[] adminPUTEndpoints = {"/api/combo/*/", "/api/combo/*"};
		http.authenticationProvider(authenticationProvider());

		http
				.securityMatcher("/api/**")
				.exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedHandlerJwt));

		http
				.authorizeHttpRequests(authorize -> authorize
						// PRIVATE ENDPOINTS
						.requestMatchers(HttpMethod.GET,userGETEndpoints).hasRole("USER")
						.requestMatchers(HttpMethod.POST,userPOSTEndpoints).hasRole("USER")
						.requestMatchers(HttpMethod.DELETE, userDELEndpoints).hasRole("USER")
						.requestMatchers(HttpMethod.PUT, userPUTEndpoints).hasRole("USER")
						.requestMatchers(HttpMethod.GET, adminGETEndpoints).hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, adminPOSTEndpoints).hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, adminDELEndpoints).hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT,adminPUTEndpoints).hasRole("ADMIN")
						// PUBLIC ENDPOINTS
						.anyRequest().permitAll()
				);

		// Disable Form login Authentication
		http.formLogin(formLogin -> formLogin.disable());

		// Disable CSRF protection (it is difficult to implement in REST APIs)
		http.csrf(csrf -> csrf.disable());

		// Disable Basic Authentication
		http.httpBasic(httpBasic -> httpBasic.disable());

		// Stateless session
		http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// Add JWT Token filter
		http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		String[] userEndpoints = {"/user", "/user/", "/shop", "/shop/", "/query", "/query/", "/upload_image",
				"/upload_image/", "/download_image", "/download_image/", "/delete_image", "/delete_image/",
				"/buy_combo", "/buy_combo/", "/download_combo", "/download_combo/", "/add_credits", "/add_credits/",
				"/delete_user/**", "/delete_combo/*", "/delete_combo/*/", "/edit_user/**", "/edit_user/**"};
		String[] adminEndpoints = {"/admin", "/admin/", "/users", "/users/", "/upload_leak", "/upload_leak/", "/delete_leak/**","/delete_leak/*/",
				"/create_combo", "/create_combo/", "/delete_combo/*", "/delete_combo/*/", "/edit_combo", "/edit_combo/"
				};
		http.authenticationProvider(authenticationProvider());

		http
				.authorizeHttpRequests(authorize -> authorize
						// PUBLIC PAGES

						.requestMatchers("/", "/images/**", "/css/**", "/gifs/**", "/register", "/error", "/error/**").permitAll()

						// PRIVATE PAGES
						.requestMatchers("/successLogin").authenticated()
						.requestMatchers(userEndpoints).hasAnyRole("USER")
						.requestMatchers(adminEndpoints).hasAnyRole("ADMIN")
				)
				.formLogin(formLogin -> formLogin
						.loginPage("/login")
						.failureUrl("/login?fail")
						.defaultSuccessUrl("/successLogin")
						.permitAll()
				)
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/")
						.permitAll()
				);

		// Disable CSRF at the moment
		//http.csrf(csrf -> csrf.disable());

		return http.build();
	}
}

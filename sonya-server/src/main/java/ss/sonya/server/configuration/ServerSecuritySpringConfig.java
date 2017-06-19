/*
 * Copyright (C) 2017 ss
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ss.sonya.server.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration
        .EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration
        .WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Server security configuration.
 * @author ss
 */
@Configuration
@EnableWebSecurity
public class ServerSecuritySpringConfig extends WebSecurityConfigurerAdapter {
    /** Entry point. */
    @Autowired
    private AuthenticationEntryPoint entryPoint;
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests().antMatchers(HttpMethod.GET, "/**")
                .permitAll().and()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/rest/data/route/search")
                .permitAll().and()
                .authorizeRequests().antMatchers(HttpMethod.POST, "/**")
                .access("hasRole('ADMIN')").and()
                .authorizeRequests().antMatchers(HttpMethod.PUT, "/**")
                .access("hasRole('ADMIN')").and()
                .authorizeRequests().antMatchers(HttpMethod.DELETE, "/**")
                .access("hasRole('ADMIN')").and()
                .formLogin().and()
                .exceptionHandling().authenticationEntryPoint(entryPoint);
    }
    @Override
    public void configure(final WebSecurity web) throws Exception {
        //web.ignoring().and().ignoring().antMatchers("/resources/**");
    }
}

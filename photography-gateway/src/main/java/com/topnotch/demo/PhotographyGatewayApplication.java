package com.topnotch.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

import com.topnotch.demo.utils.DiscoveryUtil;

@SpringBootApplication
@EnableDiscoveryClient
public class PhotographyGatewayApplication {

	@Autowired
	private GatewayFilter authFilter ;
	
	@Autowired
	private DiscoveryUtil discUtil ;
	
	private String SERVICE_1 = "tn-auth-service";
	private String SERVICE_2 = "tn-serviceA";
	
	
	public static void main(String[] args) {
		SpringApplication.run(PhotographyGatewayApplication.class, args);
	}
	
	@Bean
	public RouteLocator gateway(RouteLocatorBuilder builder) {
		
		return builder.routes()
				.route( preSpec -> preSpec.method( HttpMethod.GET )
						  .and()
						  .path( "/myapp/gateway/endpoint1" )
				   .filters( filterSpec -> filterSpec.rewritePath( "/myapp/gateway/endpoint1" , "/myapp/authService/signupPage" ) )
				   .uri( discUtil.getServiceUri(SERVICE_1) ) )
				
				.route( preSpec -> preSpec.method( HttpMethod.POST )
						  .and()
						  .path( "/myapp/gateway/endpoint1" )
				   .filters( filterSpec -> filterSpec.rewritePath( "/myapp/gateway/endpoint1" , "/myapp/authService/signupPage" ) )
				   .uri( discUtil.getServiceUri(SERVICE_1) ) )
				
				.route( preSpec -> preSpec.method( HttpMethod.GET )
						  .and()
						  .path( "/myapp/gateway/endpoint2" )
				   .filters( filterSpec -> filterSpec.rewritePath( "/myapp/gateway/endpoint2" , "/myapp/authService/loginPage" ) )
				   .uri( discUtil.getServiceUri(SERVICE_1) ) )
				
				.route( preSpec -> preSpec.method( HttpMethod.POST )
						  .and()
						  .path( "/myapp/gateway/endpoint2" )
				   .filters( filterSpec -> filterSpec.rewritePath( "/myapp/gateway/endpoint2" , "/myapp/authService/loginPage" ) )
				   .uri( discUtil.getServiceUri(SERVICE_1) ) )
				
				.route( preSpec -> preSpec.method(HttpMethod.GET).and().path( "/myapp/gateway/endpoint3" )
							       .filters( filterSpec -> {
							    	   
							    	   return filterSpec.filter(authFilter)
							    	   		 .rewritePath( "/myapp/gateway/endpoint3" , "/myapp/serviceA/homePage" ) ;
							       })
							       .uri( discUtil.getServiceUri(SERVICE_2) ) )
				
				.route( preSpec -> preSpec.method(HttpMethod.POST).and().path( "/myapp/gateway/endpoint4" )
								   .filters( filterSpec -> {
									   return filterSpec.filter(authFilter)
											  .rewritePath( "/myapp/gateway/endpoint4" , "/myapp/serviceA/uploadPage" ) ;
								   }) 
								   .uri( discUtil.getServiceUri(SERVICE_2) ) )
				.build() ;
	}
}

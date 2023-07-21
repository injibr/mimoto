package io.mosip.mimoto.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;


@Configuration
@ComponentScan(basePackages = {
        "io.mosip.mimoto.*"}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {
        Config.class, OpenApiProperties.class, SwaggerConfig.class}))
public class MimotoConfigTest extends WebSecurityConfigurerAdapter {
    OpenApiProperties openApiProperties = new OpenApiProperties();

    SwaggerConfig swaggerConfig = new SwaggerConfig();

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http = http.csrf().disable();
        http.authorizeRequests().antMatchers("*").authenticated().anyRequest().permitAll().and().exceptionHandling();
        http.headers().cacheControl();
        http.headers().frameOptions().sameOrigin();

    }

    @Bean
    public RestTemplate selfTokenRestTemplate()
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        return new RestTemplate(requestFactory);

    }

    @Bean
    public RestTemplate plainRestTemplate()
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        return new RestTemplate(requestFactory);

    }


    @Bean
    public Map<String, String> injiConfig() {
        return new HashMap<>();
    }

    @Test
    public void testSwaggerConfigOpenApi() {
        InfoProperty info = new InfoProperty();
        info.setTitle("infoTitle");
        info.setVersion("infoVersion");
        info.setDescription("infoDescription");
        LicenseProperty license = new LicenseProperty();
        license.setName("licenseName");
        license.setUrl("licenseUrl");
        info.setLicense(license);
        Service service = new Service();
        Server server = new Server();
        server.setDescription("serverDescription");
        server.setUrl("serverUrl");
        service.setServers(List.of(server));
        openApiProperties.setService(service);
        openApiProperties.setInfo(info);
        ReflectionTestUtils.setField(swaggerConfig, "openApiProperties", openApiProperties);

        OpenAPI expectedOpenAPI = new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("infoTitle")
                        .version("infoVersion")
                        .description("infoDescription")
                        .license(new License()
                                .name("licenseName")
                                .url("licenseUrl")));
        expectedOpenAPI.addServersItem(new io.swagger.v3.oas.models.servers.Server().description("serverDescription").url("serverUrl"));

        OpenAPI actualOpenApi = swaggerConfig.openApi();

        assertEquals(expectedOpenAPI, actualOpenApi);
    }
}

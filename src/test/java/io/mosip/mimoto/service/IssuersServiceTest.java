package io.mosip.mimoto.service;

import com.google.gson.Gson;
import io.mosip.mimoto.dto.IssuerDTO;
import io.mosip.mimoto.dto.IssuersDTO;
import io.mosip.mimoto.dto.ServiceConfiguration;
import io.mosip.mimoto.exception.ApiNotAccessibleException;
import io.mosip.mimoto.service.impl.IssuersServiceImpl;
import io.mosip.mimoto.util.Utilities;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class IssuersServiceTest {

    @InjectMocks
    IssuersServiceImpl issuersService = new IssuersServiceImpl();

    @Mock
    Utilities utilities;

    List<String> issuerConfigRelatedFields = List.of("additionalHeaders", "serviceConfiguration", "redirectionUri");


    static IssuerDTO getIssuerDTO(String issuerName, List<String> nullFields) {
        IssuerDTO issuer = new IssuerDTO();
        issuer.setId(issuerName + "id");
        issuer.setDisplayName(issuerName);
        issuer.setLogoUrl("/logo");
        issuer.setClientId("123");
        if (issuerName.equals("Issuer1")) issuer.setWellKnownEndpoint("/.well-known");
        else {
            if (!nullFields.contains("redirectionUri"))
                issuer.setRedirectUrl("/redirection");
            if (!nullFields.contains("serviceConfiguration")) {
                ServiceConfiguration serviceConfiguration = new ServiceConfiguration();
                serviceConfiguration.setAuthorizationEndpoint("/authorization");
                serviceConfiguration.setTokenEndpoint("/token");
                issuer.setServiceConfiguration(serviceConfiguration);
            }
            if (!nullFields.contains("additionalHeaders"))
                issuer.setAdditionalHeaders(Map.of("Content-Type", "application/json"));
        }
        return issuer;
    }


    @Before
    public void setUp() throws Exception {
        IssuersDTO issuers = new IssuersDTO();
        issuers.setIssuers(List.of(getIssuerDTO("Issuer1", Collections.emptyList()), getIssuerDTO("Issuer2", Collections.emptyList())));
        Mockito.when(utilities.getIssuersConfigJsonValue()).thenReturn(new Gson().toJson(issuers));
    }

    @Test
    public void shouldReturnIssuersWithIssuerConfigAsNull() throws ApiNotAccessibleException, IOException {
        IssuersDTO expectedIssuers = new IssuersDTO();
        List<IssuerDTO> issuers = new ArrayList<>(List.of(getIssuerDTO("Issuer1", issuerConfigRelatedFields), getIssuerDTO("Issuer2", issuerConfigRelatedFields)));
        expectedIssuers.setIssuers(issuers);

        IssuersDTO allIssuers = issuersService.getAllIssuers();

        assertEquals(expectedIssuers, allIssuers);
    }

    @Test(expected = ApiNotAccessibleException.class)
    public void shouldThrowApiNotAccessibleExceptionWhenIssuersJsonStringIsNullForGettingAllIssuers() throws IOException, ApiNotAccessibleException {
        Mockito.when(utilities.getIssuersConfigJsonValue()).thenReturn(null);

        issuersService.getAllIssuers();
    }

    @Test
    public void shouldReturnIssuerDataAndConfigForTheIssuerIdIfExist() throws ApiNotAccessibleException, IOException {
        IssuerDTO expectedIssuer = getIssuerDTO("Issuer1", issuerConfigRelatedFields);

        IssuerDTO issuer = issuersService.getIssuerConfig("Issuer1id");

        assertEquals(expectedIssuer, issuer);
    }

    @Test
    public void shouldReturnNullIfTheIssuerIdNotExists() throws ApiNotAccessibleException, IOException {
        IssuerDTO issuer = issuersService.getIssuerConfig("Issuer3id");

        assertNull(issuer);
    }

    @Test(expected = ApiNotAccessibleException.class)
    public void shouldThrowApiNotAccessibleExceptionWhenIssuersJsonStringIsNullForGettingIssuerConfig() throws IOException, ApiNotAccessibleException {
        Mockito.when(utilities.getIssuersConfigJsonValue()).thenReturn(null);

        issuersService.getIssuerConfig("Issuers1id");
    }
}

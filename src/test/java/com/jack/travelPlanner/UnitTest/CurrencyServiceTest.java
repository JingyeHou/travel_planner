package com.jack.travelPlanner.UnitTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jack.travelPlanner.services.CurrencyService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
class CurrencyServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @Autowired
    @InjectMocks
    CurrencyService currencyService;

    @Test
    public void testGetCountryFromCity() {
        String country = "Mexico";

        Mockito
                .when(restTemplate.getForEntity(
                        "https://wft-geo-db.p.rapidapi.com/v1/geo/cities?namePrefix=Cancun", Optional.class))
                .thenReturn(new ResponseEntity(country, HttpStatus.OK));
        Optional<JsonNode> result = currencyService.getCountryFromCity("Cancun");
        Assert.assertEquals(result.get().textValue(), country);
    }

    @Test
    public void testGetCurrencyCode() {
        String currencyCode = "MXN";

        Mockito
                .when(restTemplate.getForEntity(
                        "https://restcountries.eu/rest/v2/name/Mexico", Optional.class))
                .thenReturn(new ResponseEntity(currencyCode, HttpStatus.OK));
        Optional<JsonNode> result = currencyService.getCurrencySymbol("Mexico");
        Assert.assertEquals(result.get().textValue(), currencyCode);
    }

    @Test
    public void testGetExchangeRate() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity("https://api.exchangeratesapi.io/latest?base=CAD", String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rateInfo = null;

        try {
            rateInfo = mapper.readTree(response.getBody()).findPath("MXN");
            Optional<String> result = currencyService.getExchangeRate("Cancun", "CAD");
            Assert.assertEquals(result.get(), String.format("%.2f", rateInfo.asDouble()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

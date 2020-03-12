package com.jack.travelPlanner.IntegrationTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jack.travelPlanner.controllers.PlannerController;
import com.jack.travelPlanner.services.CurrencyService;
import com.jack.travelPlanner.services.WeatherService;
import com.jack.travelPlanner.services.WikiService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Optional;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(PlannerController.class)
public class PlannerControllerIntegrationTest {

    @MockBean
    private CurrencyService mockCurrencyService;

    @MockBean
    private WeatherService weatherService;

    @MockBean
    private WikiService wikiService;

    @Autowired
    private MockMvc mvc;

    @Test
    public void testGetDesInfro() throws Exception {
        String json = "{ \"f1\" : \"v1\" } ";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(json);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        result.put("description", "data");

        when(mockCurrencyService.getCountryFromCity("Cancun")).thenReturn(Optional.of(jsonNode));
        when(mockCurrencyService.getCurrencySymbol("Mexico")).thenReturn(Optional.of(jsonNode));
        when(mockCurrencyService.getExchangeRate("Cancun", "CAD")).thenReturn(Optional.of(json));
        when(weatherService.getWeatherByCityAndDate("Cancun", "2020-03-12")).thenReturn(Optional.of(result));
        when(wikiService.getWikiInfor("Cancun")).thenReturn(Optional.of(result));
        mvc.perform(
                get("/desInfor").content("{\n" +
                        "\t\"city\": \"Cancun\",\n" +
                        "\t\"home_currency\": \"CAD\",\n" +
                        "\t\"travel_date\": \"2020-03-12\"\n" +
                        "}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    public void testGetCurrConverter() throws Exception {
        String json = "{\"country\": \"Mexico\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(json).findPath("country");
        ObjectNode result = objectMapper.createObjectNode();
        result.put("description", "data");
        when(mockCurrencyService.getCurrencySymbol("Mexico")).thenReturn(Optional.of(jsonNode));
        when(mockCurrencyService.getCountryFromCity("Cancun")).thenReturn(Optional.of(jsonNode));
        when(mockCurrencyService.getExchangeRate("Cancun", "CAD")).thenReturn(Optional.of(json));
        when(mockCurrencyService.getConvertedAmount("Cancun", "CAD", 1500.00)).thenReturn(Optional.of(0.00));
        when(weatherService.getWeatherByCityAndDate("Cancun", "2020-03-12")).thenReturn(Optional.of(result));
        when(wikiService.getWikiInfor("Cancun")).thenReturn(Optional.of(result));
        mvc.perform(
                get("/currConverter").content("{\n" +
                        "\"city\": \"Cancun\" ,\n" +
                        "\"home_currency\": \"CAD\" ,\n" +
                        "\"amount\": 1500.00\n" +
                        "}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
}


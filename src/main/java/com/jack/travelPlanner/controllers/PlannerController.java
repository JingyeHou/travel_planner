package com.jack.travelPlanner.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jack.travelPlanner.services.CurrencyService;
import com.jack.travelPlanner.services.WeatherService;
import com.jack.travelPlanner.services.WikiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;

@RestController
public class PlannerController {

    private final Logger logger = LoggerFactory.getLogger(PlannerController.class);

    private CurrencyService currencyService;
    private WeatherService weatherService;
    private WikiService wikiService;

    public PlannerController(CurrencyService currencyService, WeatherService weatherService, WikiService wikiService) {
        this.currencyService = currencyService;
        this.weatherService = weatherService;
        this.wikiService = wikiService;
    }

    @GetMapping("/desInfor")
    ResponseEntity<?> getDesInfro(@RequestBody String string) {

        final String INVALID_INPUT = "Invalid Input!";

        /*
         * Parse input json and return error message when get invalid input
         */
        ObjectMapper mapper = new ObjectMapper();
        JsonNode desInforNode = null;

        try {
            desInforNode = mapper.readTree(string);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(INVALID_INPUT, HttpStatus.BAD_REQUEST);
        }

        String cityName = null;
        String homeCurrency = null;
        String travelDate = null;

        cityName = desInforNode.findPath("city").textValue();
        homeCurrency = desInforNode.findPath("home_currency").textValue();
        travelDate = desInforNode.findPath("travel_date").textValue();

        if (cityName.length() == 0 || homeCurrency.length() == 0 || travelDate.length() == 0) {
            return new ResponseEntity<>(INVALID_INPUT, HttpStatus.BAD_REQUEST);
        }

        /*
         * Fetch city/weather/currency information from Related APIs
         */
        Optional wikiOpt = wikiService.getWikiInfor(cityName);
        Optional weatherOpt = weatherService.getWeatherByCityAndDate(cityName, travelDate);
        Optional currencyOpt = currencyService.getExchangeRate(cityName, homeCurrency);

        if (!wikiOpt.isPresent() || !weatherOpt.isPresent() || !currencyOpt.isPresent()) {
            return new ResponseEntity<>(INVALID_INPUT, HttpStatus.BAD_REQUEST);
        }

        /*
         *  Reconstruct Json result and return
         */
        ObjectNode rootNode = mapper.createObjectNode();

        // save "city" field
        rootNode.put("city", cityName);
        ObjectNode wikiNode = (ObjectNode) wikiOpt.get();
        ObjectNode weatherNode = (ObjectNode) weatherOpt.get();
        String exchangeRate = (String) currencyOpt.get();
        // save field
        rootNode.setAll(wikiNode);
        rootNode.put("exchange_rate", exchangeRate);
        rootNode.setAll(weatherNode);

        return new ResponseEntity<>(rootNode, HttpStatus.OK);
    }

    @GetMapping("/currConverter")
    ResponseEntity<?> getCurrConverter(@RequestBody String string) {

        final String INVALID_INPUT = "Invalid Input!";

        /*
         * Parse input json and return error message when get invalid input
         */
        ObjectMapper mapper = new ObjectMapper();
        JsonNode desInforNode = null;

        try {
            desInforNode = mapper.readTree(string);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(INVALID_INPUT, HttpStatus.BAD_REQUEST);
        }

        String cityName = null;
        String homeCurrency = null;
        double amount = -0.1;

        cityName = desInforNode.findPath("city").textValue();
        homeCurrency = desInforNode.findPath("home_currency").textValue();
        amount = desInforNode.findPath("amount").asDouble();

        if (cityName.length() == 0 || homeCurrency.length() == 0 || amount < 0) {
            return new ResponseEntity<>(INVALID_INPUT, HttpStatus.BAD_REQUEST);
        }
        /*
         * Fetch country information from Related APIs
         */
        Optional desCountry = currencyService.getCountryFromCity(cityName);

        if (!desCountry.isPresent()) {
            return new ResponseEntity<>(INVALID_INPUT, HttpStatus.BAD_REQUEST);
        }

        TextNode countryNameNode = (TextNode) desCountry.get();
        String countryName = countryNameNode.textValue();

        Optional desCurrOpt = currencyService.getCurrencySymbol(countryName);
        Optional desAmountOpt = currencyService.getConvertedAmount(cityName, homeCurrency, amount);

        if (!desCurrOpt.isPresent() || !desAmountOpt.isPresent()) {
            return new ResponseEntity<>(INVALID_INPUT, HttpStatus.BAD_REQUEST);
        }

        JsonNode desCurrNode = (JsonNode) desCurrOpt.get();

        String desCurr = desCurrNode.textValue();
        double desAmount= (double)desAmountOpt.get();

        /*
         *  Reconstruct Json result and return
         */
        ObjectNode rootNode = mapper.createObjectNode();

        // save "city" field
        rootNode.put("city", cityName);
        rootNode.put("home_currency", homeCurrency);
        rootNode.put("destination_currency", desCurr);
        rootNode.put("home_amount", amount);
        rootNode.put("destination_amount", desAmount);

        return new ResponseEntity<>(rootNode, HttpStatus.OK);
    }

}

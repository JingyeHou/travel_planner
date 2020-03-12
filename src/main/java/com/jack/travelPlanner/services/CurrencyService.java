package com.jack.travelPlanner.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Optional;

@Service
public class CurrencyService {

    public Optional<JsonNode> getCountryFromCity(String cityName) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://wft-geo-db.p.rapidapi.com/v1/geo/cities?namePrefix=" + cityName)
                .get()
                .addHeader("x-rapidapi-host", "wft-geo-db.p.rapidapi.com")
                .addHeader("x-rapidapi-key", "4f3c2bfb78msh29106bb593472b3p1894e4jsnf1f5c962137a")
                .build();

        JsonNode countryInfo = null;
        try {
            Response response = client.newCall(request).execute();
            ObjectMapper mapper = new ObjectMapper();
            ObjectReader reader = mapper.reader();
            String jsonString = response.body().string();
            System.out.println(jsonString);
            countryInfo = reader
                         .readTree(jsonString)
                         .findPath("data")
                         .get(0)
                         .findPath("country");
            System.out.println(countryInfo);
            return Optional.of(countryInfo);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<JsonNode> getCurrencySymbol(String countryName) {
        String uri = "https://restcountries.eu/rest/v2/name/";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(uri + countryName, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode currencyInfo = null;
        try {
            currencyInfo = mapper.readTree(response.getBody()).get(0).findPath("currencies").get(0).findPath("code");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currencyInfo == null ? Optional.empty() : Optional.of(currencyInfo);
    }

    public Optional<String> getExchangeRate(String cityName, String homeCurrency) {
        String countryName = getCountryFromCity(cityName).get().textValue();
        String currencyCode = getCurrencySymbol(countryName).get().textValue();

        String uri = "https://api.exchangeratesapi.io/latest?base=";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(uri + homeCurrency, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rateInfo = null;

        try {
            rateInfo = mapper.readTree(response.getBody()).findPath(currencyCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rateInfo == null ? Optional.empty() : Optional.of(String.format("%.2f", rateInfo.asDouble()));
    }

    public Optional<Double> getConvertedAmount(String cityName, String homeCurrency, double amount) {
        String exChangeRateStr = getExchangeRate(cityName, homeCurrency).get();
        double exChangeRate = Double.parseDouble(exChangeRateStr);
        Double convertedAmount = exChangeRate * amount;
        return Optional.of(convertedAmount);
    }
}

package com.jack.travelPlanner.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Iterator;
import java.util.Optional;

@Service
public class WeatherService {

    /**
     *
     * @param cityName
     * @param date          Input date should be within 5 days of the current date
     * @return              undefined weather data structure, based on called API
     */
    public Optional<ObjectNode> getWeatherByCityAndDate(String cityName, String date) {
        String uri = String.format("http://api.openweathermap.org/data/2.5/forecast?q=%1$s&appid=52084c05f63432538c5e4b6a1f1be4f7", cityName);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode weatherInfor = null;

        try {
            ArrayNode arrayNode = (ArrayNode)mapper.readTree(response.getBody()).findPath("list");
            Iterator<JsonNode> node = arrayNode.elements();
            while (node.hasNext()) {
                JsonNode next = node.next();
                if(next.findPath("dt_txt").textValue().equals(date + " 09:00:00")) weatherInfor = next;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (weatherInfor == null) {
            return Optional.empty();
        } else {
            ArrayNode arrayResult = mapper.createArrayNode();
            arrayResult.add(weatherInfor);
            ObjectNode result = mapper.createObjectNode();
            result.set("weather", arrayResult);
            return Optional.of(result);
        }
    }
}

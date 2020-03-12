package com.jack.travelPlanner.UnitTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jack.travelPlanner.services.WeatherService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
class WeatherServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @Autowired
    @InjectMocks
    private WeatherService weatherService;

    @Test
    public void testGetWeatherByCityAndDate() {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date dt = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.DATE, 1);
        String date = simpleDateFormat.format(c.getTime());
        System.out.println(date);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity("http://api.openweathermap.org/data/2.5/forecast?q=Cancun&appid=52084c05f63432538c5e4b6a1f1be4f7", String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode weatherInfor = null;

        try {
            ArrayNode arrayNode = (ArrayNode)mapper.readTree(response.getBody()).findPath("list");
            Iterator<JsonNode> node = arrayNode.elements();
            while (node.hasNext()) {
                JsonNode next = node.next();
                if(next.findPath("dt_txt").textValue().equals(date + " 09:00:00")) weatherInfor = next;
            }
            Optional<ObjectNode> result = weatherService.getWeatherByCityAndDate("Cancun", date);
            Assert.assertEquals(result.get().findValue("weather").get(0), weatherInfor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
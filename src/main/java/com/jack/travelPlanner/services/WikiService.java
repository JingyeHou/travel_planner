package com.jack.travelPlanner.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
@Service
public class WikiService {

    /**
     *
     * @param cityName    The name of city
     * @return            Optional[String] If input cityName is valid, return the first paragraph of a Wikipedia article;
     *                    Optional.empty   If input cityName is invalid
     */

    public Optional<ObjectNode> getWikiInfor(String cityName) {

        String uri = "https://en.wikipedia.org/w/api.php?action=query&prop=extracts&exintro&explaintext&format=json&redirects&titles=";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(uri + cityName, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode wikiInfor = null;

        try {
            wikiInfor = mapper.readTree(response.getBody()).findPath("extract");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (wikiInfor == null) {
            return Optional.empty();
        } else {
            ObjectNode result = mapper.createObjectNode();
            result.put("description", wikiInfor.textValue());
            return Optional.of(result);
        }
    }
}

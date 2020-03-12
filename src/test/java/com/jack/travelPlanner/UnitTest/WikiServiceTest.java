package com.jack.travelPlanner.UnitTest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jack.travelPlanner.services.WikiService;
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
class WikiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Autowired
    @InjectMocks
    private WikiService wikiService;

    @Test
    public void testGetWikiInfor() {
        String str = "Cancún ( or ;Spanish pronunciation: [kaŋˈkun] pronunciation ) is a city in southeast Mexico on the northeast coast of the Yucatán Peninsula in the Mexican state of Quintana Roo. It is a significant tourist destination in Mexico and the seat of the municipality of Benito Juárez. The city is on the Caribbean Sea and is one of Mexico's easternmost points.\nCancún is just north of Mexico's Caribbean coast resort band known as the Riviera Maya. In older English-language documents, the city’s name is sometimes spelled \"Cancoon\", an attempt to convey the sound of the name.";

        Mockito
                .when(restTemplate.getForEntity(
                        "https://en.wikipedia.org/w/api.php?action=query&prop=extracts&exintro&explaintext&format=json&redirects&titles=Cancún", Optional.class))
                .thenReturn(new ResponseEntity(str, HttpStatus.OK));
        Optional<ObjectNode> result = wikiService.getWikiInfor("Cancun");
        Assert.assertEquals(result.get().findPath("description").textValue(), str);
    }
}
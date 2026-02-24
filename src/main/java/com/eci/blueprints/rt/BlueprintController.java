package com.eci.blueprints.rt;

import com.eci.blueprints.rt.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Controller
public class BlueprintController {

  private final SimpMessagingTemplate template;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final String REST_API_URL = "http://localhost:8081";

  public BlueprintController(SimpMessagingTemplate template, RestTemplate restTemplate, ObjectMapper objectMapper) {
    this.template = template;
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
  }

  @MessageMapping("/draw")
  public void onDraw(DrawEvent evt) {
    try {
      System.out.println(" Recibido evento de dibujo: " + evt);
      
      // 1. Guardar el nuevo punto en la API REST (8081) usando PUT
      String addPointUrl = REST_API_URL + "/api/v1/blueprints/" + evt.author() + "/" + evt.name() + "/points";
      System.out.println("PUT a: " + addPointUrl);
      restTemplate.put(addPointUrl, evt.point());

      // 2. Obtener el blueprint completo con todos los puntos
      String getBlueprintUrl = REST_API_URL + "/api/v1/blueprints/" + evt.author() + "/" + evt.name();
      System.out.println("GET desde: " + getBlueprintUrl);
      
      // Obtener la respuesta como Map para extraer el 'data'
      ResponseEntity<Map> response = restTemplate.getForEntity(getBlueprintUrl, Map.class);
      Map<String, Object> apiResponse = response.getBody();
      
      if (apiResponse != null && apiResponse.containsKey("data")) {
        Object blueprintData = apiResponse.get("data");
        System.out.println("Blueprint data: " + blueprintData);
        
        // Convertir el data a BlueprintUpdate
        BlueprintUpdate blueprint = objectMapper.convertValue(blueprintData, BlueprintUpdate.class);
        
        // 3. Enviar el blueprint completo al topic
        String topic = "/topic/blueprints." + evt.author() + "." + evt.name();
        System.out.println("Enviando a topic: " + topic + " con " + blueprint.points().size() + " puntos");
        template.convertAndSend(topic, blueprint);
        System.out.println("Mensaje enviado exitosamente");
      }
    } catch (Exception e) {
      System.err.println("Error al procesar el punto: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @ResponseBody
  @GetMapping("/api/blueprints/{author}/{name}")
  public BlueprintUpdate get(@PathVariable String author, @PathVariable String name) {
    return new BlueprintUpdate(author, name, List.of(new Point(10,10), new Point(40,50)));
  }
}

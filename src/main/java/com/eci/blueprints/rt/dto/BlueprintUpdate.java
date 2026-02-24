package com.eci.blueprints.rt.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BlueprintUpdate(String author, String name, List<Point> points) {}

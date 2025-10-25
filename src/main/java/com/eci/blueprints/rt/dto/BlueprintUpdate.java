package com.eci.blueprints.rt.dto;

import java.util.List;

public record BlueprintUpdate(String author, String name, List<Point> points) {}

package com.example.openmapvalidator.model.open;

import lombok.Data;

@Data
public class GeographicRectangle {
    private double minLatitude;
    private double minLongitude;
    private double maxLatitude;
    private double maxLongitude;
}

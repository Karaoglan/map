package com.example.openmapvalidator.model.foursquare;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class FoursquareResult {
    private Response response;
    @JsonIgnore
    private Meta meta;
}

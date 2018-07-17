package com.example.openmapvalidator.model.google;

import lombok.Data;

import java.util.List;

@Data
public class GoogleResult {
    private String error_message;
    private List<Result> results;
    private List<String> html_attributions;
    private Boolean permanently_closed;
    private String status;
    private String next_page_token;
}

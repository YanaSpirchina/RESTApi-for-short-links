package test.spring.restapi.util;

import lombok.Data;

@Data
public class LinkErrorResponse {
    private String message;
    private long timestamp;

    public LinkErrorResponse(String message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

}

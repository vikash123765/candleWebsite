package com.vikash.mobileCaseBackend.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestOrderRequestWrapper {
    private  GuestOrderRequest guestOrderRequest;
    @JsonRawValue
    private String jsonPayload;
}


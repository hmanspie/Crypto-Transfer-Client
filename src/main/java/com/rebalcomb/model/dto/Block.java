package com.rebalcomb.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Block {

    @NotBlank
    private String from;

    @NotBlank
    private String to;

    @NotBlank
    private String message;

    @NotBlank
    private String hash;
}

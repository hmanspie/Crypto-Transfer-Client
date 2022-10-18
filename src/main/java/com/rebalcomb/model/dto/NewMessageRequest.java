package com.rebalcomb.model.dto;


import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewMessageRequest {

    @NotBlank
    private String from;

    @NotBlank
    private String to;

    @NotBlank
    private String title;

    @NotBlank
    private String bodyMessage;
}

package com.rebalcomb.model.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageRequest {

    @NotBlank
    private Long id;

    @NotBlank
    private String user_from;

    @NotBlank
    private String user_to;

    @NotBlank
    private String title;

    @NotBlank
    private String bodyMessage;

    @NotBlank
    private String dateTime;
}

package com.rebalcomb.model.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlockRequest {

    private MessageRequest messageRequest;

    private String secretKey;
}

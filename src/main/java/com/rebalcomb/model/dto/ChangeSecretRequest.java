package com.rebalcomb.model.dto;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangeSecretRequest {

    @NotBlank
    private String serverID;

    @NotBlank
    private String secretKey;

    @NotBlank
    private String username;

    @NotBlank
    private Timestamp regTime;
}

package com.rebalcomb.model.dto;


import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import java.security.PublicKey;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserRequest {

    @NotBlank
    private String serverId;

    @NotBlank
    private String publicKey;

    @NotBlank
    private String regTime;

}

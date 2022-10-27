package com.rebalcomb.model.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import javax.validation.constraints.NotBlank;
import java.math.BigInteger;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeyPairRequest {

    @NotBlank
    private String login;

    @NotBlank
    private BigInteger publicKey;

    @NotBlank
    private BigInteger module;
}

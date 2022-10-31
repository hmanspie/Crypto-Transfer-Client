package com.rebalcomb.model.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import javax.validation.constraints.NotBlank;
import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeyPair {

    @NotBlank
    private BigInteger publicKey;

    @NotBlank
    private BigInteger module;
}

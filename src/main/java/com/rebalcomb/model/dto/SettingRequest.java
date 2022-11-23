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
public class SettingRequest {

    @NotBlank
    private String serverID;

    @NotBlank
    private String rsaLength;

    @NotBlank
    private String aesLength;

    @NotBlank
    private String hashType;

    @NotBlank
    private String imagesPoolCount;

}

package com.dope.breaking.dto.financial;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmountRequestDto {

    @NotNull
    @JsonProperty(value = "amount")
    private int amount;

}

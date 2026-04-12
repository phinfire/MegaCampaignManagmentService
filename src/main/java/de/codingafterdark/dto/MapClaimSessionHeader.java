package de.codingafterdark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapClaimSessionHeader {
    private Long id;
    private String name;
    private Boolean isPublic;
    private String creatorId;
}

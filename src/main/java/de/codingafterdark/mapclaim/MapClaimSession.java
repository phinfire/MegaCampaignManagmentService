package de.codingafterdark.mapclaim;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import de.codingafterdark.dto.MapClaimCountryData;

import java.util.Map;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class MapClaimSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Column(name = "is_public")
    private Boolean isPublic;
    
    private String creatorId;
    
    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, MapClaimCountryData> countries;
    
    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> ownership;
}

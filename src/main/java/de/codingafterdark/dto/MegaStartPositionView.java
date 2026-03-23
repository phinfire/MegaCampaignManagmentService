package de.codingafterdark.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import de.codingafterdark.assignment.MegaStartPosition;

@Data
@AllArgsConstructor
public class MegaStartPositionView {
    @JsonProperty("userId")
    private String userId;

    @JsonProperty("megaCampaignId")
    private Long megaCampaignId;

    @JsonProperty("startKey")
    private String startKey;

    @JsonProperty("startData")
    private String startData;

    public static MegaStartPositionView fromMegaStartPosition(MegaStartPosition position) {
        return new MegaStartPositionView(
            position.getUserId(),
            position.getMegaCampaignId(),
            position.getStartKey(),
            position.getStartData()
        );
    }
}

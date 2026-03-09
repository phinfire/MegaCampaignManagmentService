package de.codingafterdark.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import de.codingafterdark.assignment.MegaStartPosition;

@Data
@AllArgsConstructor
public class MegaStartPositionView {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("mega_campaign_id")
    private Long megaCampaignId;

    @JsonProperty("start_key")
    private String startKey;

    @JsonProperty("start_data")
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

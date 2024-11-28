package com.wibeechat.missa.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsageItem {

    @JsonProperty("organization_id")
    private String organizationId;

    @JsonProperty("organization_name")
    private String organizationName;

    @JsonProperty("aggregation_timestamp")
    private long aggregationTimestamp;

    @JsonProperty("n_requests")
    private int nRequests;

    @JsonProperty("operation")
    private String operation;

    @JsonProperty("snapshot_id")
    private String snapshotId;

    @JsonProperty("n_context_tokens_total")
    private int nContextTokensTotal;

    @JsonProperty("n_generated_tokens_total")
    private int nGeneratedTokensTotal;

    // 필요 없는 필드들은 무시되도록 설정 (email, api_key_id 등)
}
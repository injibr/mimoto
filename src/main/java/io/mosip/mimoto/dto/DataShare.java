package io.mosip.mimoto.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Instantiates a new data share.
 */

/**
 * Instantiates a new data share.
 */
@Data
public class DataShare implements Serializable {

    /** The url. */
    private String url;

    /** The valid for in minutes. */
    private int validForInMinutes;

    /** The transactions allowed. */
    private int transactionsAllowed;

    /** The policy id. */
    private String policyId;

    /** The subscriber id. */
    private String subscriberId;

    /** The signature. */
    private String signature;

}

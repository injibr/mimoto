package io.mosip.mimoto.spi;

import io.mosip.kernel.biometrics.entities.BIR;

import java.util.List;

/**
 * @author
 * 
 * 
 *         Interface for Cbeff Interface
 *
 */
public interface CbeffUtil {

    public List<BIR> getBIRDataFromXML(byte[] xmlBytes) throws Exception;
}

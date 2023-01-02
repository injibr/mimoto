package io.mosip.mimoto.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import io.mosip.kernel.biometrics.commons.CbeffValidator;
import io.mosip.kernel.biometrics.entities.BIR;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.mimoto.spi.CbeffUtil;

/**
 * This class is used to create,update, validate and search Cbeff data.
 *
 * @author Ramadurai Pandian
 */
@Component
public class CbeffImpl implements CbeffUtil {

    /**
     * Method used for getting list of BIR from XML bytes *
     * 
     * @param xmlBytes byte array of XML data
     * @return List of BIR data extracted from XML
     * @throws Exception Exception
     */
    @Override
    public List<BIR> getBIRDataFromXML(byte[] xmlBytes) throws Exception {
        BIR bir = CbeffValidator.getBIRFromXML(xmlBytes);
        return bir.getBirs();
    }
}

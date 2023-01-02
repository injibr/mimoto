package io.mosip.mimoto.util;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.mimoto.constant.LoggerFileConstant;
import io.mosip.mimoto.service.impl.CbeffImpl;
import io.mosip.mimoto.spi.CbeffUtil;
import org.apache.commons.codec.binary.Base64;

import java.util.List;

/**
 * The Class CbeffToBiometricUtil.
 * 
 * @author M1048358 Alok
 * @author M1030448 Jyoti
 */
public class CbeffToBiometricUtil {

    Logger logger = LoggerUtil.getLogger(CbeffToBiometricUtil.class);

    /** The cbeffutil. */
    private CbeffUtil cbeffutil = new CbeffImpl();
    /** the bioApi */

    /**
     * Instantiates a new cbeff to biometric util.
     *
     * @param cbeffutil the cbeffutil
     */
    public CbeffToBiometricUtil(CbeffUtil cbeffutil) {
        this.cbeffutil = cbeffutil;
    }

    /**
     * Instantiates biometric util
     * 
     */
    public CbeffToBiometricUtil() {

    }

    /**
     * Gets the photo.
     *
     * @param cbeffFileString the cbeff file string
     * @param type            the type
     * @param subType         the sub type
     * @return the photo
     * @throws Exception the exception
     */
    public byte[] getImageBytes(String cbeffFileString, String type, List<String> subType) throws Exception {
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
                "CbeffToBiometricUtil::getImageBytes()::entry");

        byte[] photoBytes = null;
        if (cbeffFileString != null) {
            List<BIR> bIRTypeList = getBIRTypeList(cbeffFileString);
            photoBytes = getPhotoByTypeAndSubType(bIRTypeList, type, subType);
        }
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
                "CbeffToBiometricUtil::getImageBytes()::exit");

        return photoBytes;
    }

    /**
     * Gets the photo by type and sub type.
     *
     * @param bIRList the b IR type list
     * @param type        the type
     * @param subType     the sub type
     * @return the photo by type and sub type
     */
    public byte[] getPhotoByTypeAndSubType(List<BIR> bIRList, String type, List<String> subType) {
        byte[] photoBytes = null;
        for (BIR bir : bIRList) {
            if (bir.getBdbInfo() != null) {
                List<BiometricType> singleTypeList = bir.getBdbInfo().getType();
                List<String> subTypeList = bir.getBdbInfo().getSubtype();

                boolean isType = isBiometricType(type, singleTypeList);
                boolean isSubType = isSubType(subType, subTypeList);

                if (isType && isSubType) {
                    photoBytes = bir.getBdb();
                    break;
                }
            }
        }
        return photoBytes;
    }

    /**
     * Checks if is sub type.
     *
     * @param subType     the sub type
     * @param subTypeList the sub type list
     * @return true, if is sub type
     */
    private boolean isSubType(List<String> subType, List<String> subTypeList) {
        return subTypeList.equals(subType) ? Boolean.TRUE : Boolean.FALSE;
    }

    private boolean isBiometricType(String type, List<BiometricType> biometricTypeList) {
        boolean isType = false;
        for (BiometricType biometricType : biometricTypeList) {
            if (biometricType.value().equalsIgnoreCase(type)) {
                isType = true;
            }
        }
        return isType;
    }

    /**
     * Checks if is same type.
     *
     * @param file1BirList the file 1 bir type list
     * @param file2BirList the file 2 bir type list
     * @return true, if is same type
     */
    private boolean isBiometricTypeSame(List<BIR> file1BirList, List<BIR> file2BirList) {
        boolean isTypeSame = false;
        for (BIR bir1 : file1BirList) {
            List<BiometricType> biometricTypeList1 = bir1.getBdbInfo().getType();
            for (BIR bir2 : file2BirList) {
                List<BiometricType> biometricTypeList2 = bir2.getBdbInfo().getType();
                if (biometricTypeList1.equals(biometricTypeList2)) {
                    isTypeSame = true;
                    break;
                }
            }
            if (isTypeSame)
                break;
        }
        return isTypeSame;
    }

    /**
     * Gets the BIR type list.
     *
     * @param cbeffFileString the cbeff file string
     * @return the BIR type list
     * @throws Exception the exception
     */

    public List<BIR> getBIRTypeList(String cbeffFileString) throws Exception {
        return cbeffutil.getBIRDataFromXML(Base64.decodeBase64(cbeffFileString));
    }

}

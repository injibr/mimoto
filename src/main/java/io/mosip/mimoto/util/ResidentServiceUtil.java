package io.mosip.mimoto.util;

import io.mosip.mimoto.dto.mimoto.AppOTPRequestDTO;
import io.mosip.mimoto.exception.InvalidInputException;
import io.mosip.mimoto.exception.PlatformErrorMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ResidentServiceUtil {

    private static Logger logger = LoggerFactory.getLogger(ResidentServiceUtil.class);

    @Value("${mosip.notificationtype:EMAIL|PHONE}")
    private String notificationType;

    public void validateNotificationChannel(AppOTPRequestDTO requestDTO){
        List<String> incorrectNotificationChannel = new ArrayList<String>();
        logger.info("\n Notification Types from application-default.properties in mosip-config - > " + notificationType);
        incorrectNotificationChannel = requestDTO.getOtpChannel()
                .stream()
                .filter(otpChannel -> !notificationType.contains(otpChannel))
                .collect(Collectors.toList());
        if(incorrectNotificationChannel.size() > 0) {
            logger.error("Invalid Input is received in otpChannels " + String.join(",", incorrectNotificationChannel));
            throw new InvalidInputException(PlatformErrorMessages.MIMOTO_PGS_INVALID_INPUT_PARAMETER.getMessage() + " - " + "otpChannels");
        }
    }
}

package io.mosip.mimoto.govbr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping()
public class GovBRController {
    private static final Logger logger = LoggerFactory.getLogger(GovBRController.class);

    @Autowired
    private GovBRService govBRService;

    @GetMapping("/user/profile")
    public ResponseEntity<ApiResponse<GovBRUserProfileResponse>> getUserProfile(@RequestParam("code") String code, @RequestParam("codeVerifier") String codeVerifier) {
        logger.info("Received request for user profile");
        GovBRUserProfileResponse response = govBRService.getUserProfile(code, codeVerifier);
        ApiResponse<GovBRUserProfileResponse> apiResponse = new ApiResponse<>(
                "success",
                200,
                "User profile fetched successfully",
                response,
                java.time.format.DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now())
        );
        return ResponseEntity.ok(apiResponse);
    }
}

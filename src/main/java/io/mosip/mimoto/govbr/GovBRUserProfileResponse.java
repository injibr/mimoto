package io.mosip.mimoto.govbr;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GovBRUserProfileResponse {
    private String sub;
    private String name;
    private String socialName;
    private String profile;
    private String picture;
    private String email;
    private Boolean emailVerified;
    private String profilePictureBase64;

    // Getters and setters
    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSocialName() { return socialName; }
    public void setSocialName(String socialName) { this.socialName = socialName; }
    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }
    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    public String getProfilePictureBase64() { return profilePictureBase64; }
    public void setProfilePictureBase64(String profilePictureBase64) { this.profilePictureBase64 = profilePictureBase64; }
}


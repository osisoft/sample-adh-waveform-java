package com.github.osisoft.sdsjava;

public class Appsettings {
  private String Resource;
  private String ApiVersion;
  private String TenantId;
  private String NamespaceId;
  private String CommunityId;
  private String ClientId;
  private String ClientSecret;

  public String getResource() {
    return Resource;
  }

  public void setResource(String resource) {
      this.Resource = resource;
  }

  public String getApiVersion() {
    return ApiVersion;
  }

  public void setApiVersion(String apiVersion) {
      this.ApiVersion = apiVersion;
  }

  public String getTenantId() {
    return TenantId;
  }

  public void setTenantId(String tenantId) {
      this.TenantId = tenantId;
  }

  public String getNamespaceId() {
    return NamespaceId;
  }

  public void setNamespaceId(String namespaceId) {
      this.NamespaceId = namespaceId;
  }

  public String getCommunityId() {
    return CommunityId;
  }

  public void setCommunityId(String communityId) {
      this.CommunityId = communityId;
  }

  public String getClientId() {
    return ClientId;
  }

  public void setClientId(String clientId) {
      this.ClientId = clientId;
  }

  public String getClientSecret() {
    return ClientSecret;
  }

  public void setClientSecret(String clientSecret) {
      this.ClientSecret = clientSecret;
  }
}

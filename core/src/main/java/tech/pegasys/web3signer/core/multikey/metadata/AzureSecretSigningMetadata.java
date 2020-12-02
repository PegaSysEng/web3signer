/*
 * Copyright 2020 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.web3signer.core.multikey.metadata;

import tech.pegasys.web3signer.core.config.AzureAuthenticationMode;
import tech.pegasys.web3signer.core.signing.ArtifactSigner;
import tech.pegasys.web3signer.core.signing.KeyType;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

public class AzureSecretSigningMetadata extends SigningMetadata {

  private final String clientId;
  private final String clientSecret;
  private final String tenantId;
  private final String vaultName;
  private final String secretName;
  private final AzureAuthenticationMode authenticationMode;

  @JsonCreator
  public AzureSecretSigningMetadata(
      @JsonProperty("clientId") final String clientId,
      @JsonProperty("clientSecret") final String clientSecret,
      @JsonProperty("tenantId") final String tenantId,
      @JsonProperty(value = "vaultName", required = true) final String vaultName,
      @JsonProperty(value = "secretName", required = true) final String secretName,
      @JsonProperty("authenticationMode") final AzureAuthenticationMode azureAuthenticationMode,
      @JsonProperty("keyType") final KeyType keyType) {
    super(keyType != null ? keyType : KeyType.BLS);
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.tenantId = tenantId;
    this.vaultName = vaultName;
    this.secretName = secretName;
    this.authenticationMode =
        azureAuthenticationMode == null
            ? AzureAuthenticationMode.CLIENT_SECRET
            : azureAuthenticationMode;
  }

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getVaultName() {
    return vaultName;
  }

  public String getSecretName() {
    return secretName;
  }

  public AzureAuthenticationMode getAuthenticationMode() {
    return authenticationMode;
  }

  @Override
  public ArtifactSigner createSigner(final ArtifactSignerFactory factory) {
    return factory.create(this);
  }

  @Override
  public void validate() throws SigningMetadataException {
    final List<String> missingParameters = new ArrayList<>();
    if (authenticationMode == AzureAuthenticationMode.CLIENT_SECRET) {
      if (StringUtils.isBlank(clientId)) {
        missingParameters.add("clientId");
      }

      if (StringUtils.isBlank(clientSecret)) {
        missingParameters.add("clientSecret");
      }

      if (StringUtils.isBlank(tenantId)) {
        missingParameters.add("tenantId");
      }

      if (!missingParameters.isEmpty()) {
        throw new SigningMetadataException(
            "Missing required parameters for type: \"azure-secret\", authenticationMode: \"CLIENT_SECRET\" - "
                + String.join(", ", missingParameters));
      }
    }
  }
}

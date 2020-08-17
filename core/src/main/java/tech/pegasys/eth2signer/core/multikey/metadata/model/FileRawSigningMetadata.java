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
package tech.pegasys.eth2signer.core.multikey.metadata.model;

import tech.pegasys.eth2signer.core.multikey.metadata.ArtifactSignerFactory;
import tech.pegasys.eth2signer.core.signing.ArtifactSigner;
import tech.pegasys.eth2signer.core.signing.KeyType;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.tuweni.bytes.Bytes;

public class FileRawSigningMetadata extends SigningMetadata {

  private final Bytes privateKey;

  public FileRawSigningMetadata(
      @JsonProperty(value = "privateKey", required = true) final Bytes privateKey,
      @JsonProperty(value = "keyType") final KeyType keyType) {
    super(keyType != null ? keyType : KeyType.BLS);
    this.privateKey = privateKey;
  }

  @Override
  public ArtifactSigner createSigner(final ArtifactSignerFactory artifactSignerFactory) {
    return artifactSignerFactory.create(this);
  }

  public Bytes getPrivateKeyBytes() {
    return privateKey;
  }
}

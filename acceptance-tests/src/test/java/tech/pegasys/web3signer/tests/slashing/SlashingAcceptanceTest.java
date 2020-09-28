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
package tech.pegasys.web3signer.tests.slashing;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.teku.bls.BLSKeyPair;
import tech.pegasys.web3signer.core.service.http.ArtifactType;
import tech.pegasys.web3signer.core.service.http.Eth2SigningRequestBody;
import tech.pegasys.web3signer.core.signing.KeyType;
import tech.pegasys.web3signer.dsl.signer.SignerConfigurationBuilder;
import tech.pegasys.web3signer.dsl.utils.MetadataFileHelpers;
import tech.pegasys.web3signer.tests.AcceptanceTestBase;

import java.nio.file.Path;
import java.security.SecureRandom;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SlashingAcceptanceTest extends AcceptanceTestBase {

  private static final MetadataFileHelpers metadataFileHelpers = new MetadataFileHelpers();

  final BLSKeyPair keyPair = BLSKeyPair.random(new SecureRandom());

  void setupSigner(final Path testDirectory, final boolean enableSlashing) {
    final SignerConfigurationBuilder builder = new SignerConfigurationBuilder();
    builder.withMode("eth2");
    builder.withSlashingEnabled(enableSlashing);
    builder.withSlashingProtectionDbUsername("postgres");
    builder.withSlashingProtectionDbPassword("postgres");
    builder.withKeyStoreDirectory(testDirectory);

    final Path keyConfigFile = testDirectory.resolve("keyfile.yaml");
    metadataFileHelpers.createUnencryptedYamlFileAt(
        keyConfigFile, keyPair.getSecretKey().toBytes().toHexString(), KeyType.BLS);

    startSigner(builder.build());
  }

  @Test
  void canSignSameAttestationTwiceWhenSlashingIsEnabled(@TempDir Path testDirectory)
      throws JsonProcessingException {

    setupSigner(testDirectory, true);

    final Eth2SigningRequestBody request =
        new Eth2SigningRequestBody(
            Bytes.fromHexString("0x01"),
            ArtifactType.ATTESTATION,
            null,
            UInt64.valueOf(5L),
            UInt64.valueOf(6L));

    final Response initialResponse =
        signer.eth2Sign(keyPair.getPublicKey().toBytesCompressed().toHexString(), request);
    assertThat(initialResponse.getStatusCode()).isEqualTo(200);
    final Response secondResponse =
        signer.eth2Sign(keyPair.getPublicKey().toBytesCompressed().toHexString(), request);
    assertThat(secondResponse.getStatusCode()).isEqualTo(200);
  }

  @Test
  void cannotSignASecondAttestationForSameSlotWithDifferentSigningRoot(@TempDir Path testDirectory)
      throws JsonProcessingException {
    setupSigner(testDirectory, true);

    final Bytes signingRoot = Bytes.fromHexString("0x01");
    final Eth2SigningRequestBody initialRequest =
        new Eth2SigningRequestBody(
            signingRoot, ArtifactType.ATTESTATION, null, UInt64.valueOf(5L), UInt64.valueOf(6L));

    final Response initialResponse =
        signer.eth2Sign(keyPair.getPublicKey().toBytesCompressed().toHexString(), initialRequest);
    assertThat(initialResponse.getStatusCode()).isEqualTo(200);

    final Bytes secondSigningRoot = Bytes.fromHexString("0x02");
    final Eth2SigningRequestBody secondRequest =
        new Eth2SigningRequestBody(
            secondSigningRoot,
            ArtifactType.ATTESTATION,
            null,
            UInt64.valueOf(5L),
            UInt64.valueOf(6L));

    final Response secondResponse =
        signer.eth2Sign(keyPair.getPublicKey().toBytesCompressed().toHexString(), secondRequest);
    assertThat(secondResponse.getStatusCode()).isEqualTo(403);
  }

  @Test
  void canSignSameBlockTwiceWhenSlashingIsEnabled(@TempDir Path testDirectory)
      throws JsonProcessingException {

    setupSigner(testDirectory, true);

    final Eth2SigningRequestBody request =
        new Eth2SigningRequestBody(
            Bytes.fromHexString("0x01"), ArtifactType.BLOCK, UInt64.valueOf(3L), null, null);

    final Response initialResponse =
        signer.eth2Sign(keyPair.getPublicKey().toBytesCompressed().toHexString(), request);
    assertThat(initialResponse.getStatusCode()).isEqualTo(200);
    final Response secondResponse =
        signer.eth2Sign(keyPair.getPublicKey().toBytesCompressed().toHexString(), request);
    assertThat(secondResponse.getStatusCode()).isEqualTo(200);
  }

  @Test
  void signingBlockWithDifferentSigningRootForPreviousSLotFailsWith403(@TempDir Path testDirectory)
      throws JsonProcessingException {
    setupSigner(testDirectory, true);

    final Eth2SigningRequestBody initialRequest =
        new Eth2SigningRequestBody(
            Bytes.fromHexString("0x01"), ArtifactType.BLOCK, UInt64.valueOf(3L), null, null);

    final Response initialResponse =
        signer.eth2Sign(keyPair.getPublicKey().toBytesCompressed().toHexString(), initialRequest);
    assertThat(initialResponse.getStatusCode()).isEqualTo(200);

    final Eth2SigningRequestBody secondRequest =
        new Eth2SigningRequestBody(
            Bytes.fromHexString("0x02"), ArtifactType.BLOCK, UInt64.valueOf(3L), null, null);

    final Response secondResponse =
        signer.eth2Sign(keyPair.getPublicKey().toBytesCompressed().toHexString(), secondRequest);
    assertThat(secondResponse.getStatusCode()).isEqualTo(403);
  }

  @Test
  void twoDifferentBlocksCanBeSignedForSameSlotIfSlashingIsDisabled(@TempDir Path testDirectory)
      throws JsonProcessingException {
    setupSigner(testDirectory, false);
    final Eth2SigningRequestBody initialRequest =
        new Eth2SigningRequestBody(
            Bytes.fromHexString("0x01"), ArtifactType.BLOCK, UInt64.valueOf(3L), null, null);

    final Response initialResponse =
        signer.eth2Sign(keyPair.getPublicKey().toBytesCompressed().toHexString(), initialRequest);
    assertThat(initialResponse.getStatusCode()).isEqualTo(200);

    final Eth2SigningRequestBody secondRequest =
        new Eth2SigningRequestBody(
            Bytes.fromHexString("0x02"), ArtifactType.BLOCK, UInt64.valueOf(3L), null, null);

    final Response secondResponse =
        signer.eth2Sign(keyPair.getPublicKey().toBytesCompressed().toHexString(), secondRequest);
    assertThat(secondResponse.getStatusCode()).isEqualTo(200);
  }
}

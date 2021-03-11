/*
 * Copyright 2021 ConsenSys AG.
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
package tech.pegasys.web3signer.slashingprotection;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.web3signer.slashingprotection.dao.SignedAttestation;
import tech.pegasys.web3signer.slashingprotection.dao.SignedBlock;
import tech.pegasys.web3signer.slashingprotection.dao.SigningWatermark;

import java.util.List;

import dsl.TestSlashingProtectionParameters;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class PruningIntegrationTest extends IntegrationTestBase {

  @ParameterizedTest
  @CsvSource({
    "1, 1, 9, 9",
    "5, 1, 5, 5",
    "9, 1, 1, 1, 1",
    "10, 1, 0, 0",
    "20, 1, 0, 0",
    "1, 2, 9, 8",
    "3, 2, 7, 4"
  })
  void prunesDataForRegisteredValidator(
      final int amountToKeep,
      final int slotsPerEpoch,
      final int expectedLowestPopulatedEpoch,
      final int expectedLowestPopulatedSlot) {
    final SlashingProtection slashingProtection =
        SlashingProtectionFactory.createSlashingProtection(
            new TestSlashingProtectionParameters(
                databaseUrl, USERNAME, PASSWORD, amountToKeep, slotsPerEpoch));
    final int size = 10;
    insertAndRegisterData(slashingProtection, size, size, 1);
    final List<SignedAttestation> allAttestations = fetchAttestations(1);
    final List<SignedBlock> allBlocks = fetchBlocks(1);

    slashingProtection.prune();

    final List<SignedAttestation> expectedAttestations =
        allAttestations.subList(expectedLowestPopulatedEpoch, size);
    final List<SignedAttestation> attestationsInDatabase = fetchAttestations(1);
    assertThat(attestationsInDatabase)
        .usingFieldByFieldElementComparator()
        .isEqualTo(expectedAttestations);

    final List<SignedBlock> expectedBlocks = allBlocks.subList(expectedLowestPopulatedSlot, size);
    final List<SignedBlock> blocks = fetchBlocks(1);
    assertThat(blocks).usingFieldByFieldElementComparator().isEqualTo(expectedBlocks);

    assertThat(getWatermark(1))
        .isEqualToComparingFieldByField(
            new SigningWatermark(
                1,
                UInt64.valueOf(expectedLowestPopulatedSlot),
                UInt64.valueOf(expectedLowestPopulatedEpoch),
                UInt64.valueOf(expectedLowestPopulatedEpoch)));
  }

  @Test
  void dataUnchangedForNonRegisteredValidators() {
    final SlashingProtection slashingProtection =
        SlashingProtectionFactory.createSlashingProtection(
            new TestSlashingProtectionParameters(databaseUrl, USERNAME, PASSWORD, 1, 1));
    jdbi.withHandle(h -> validators.registerValidators(h, List.of(Bytes.of(1))));
    insertData(2, 2, 1);
    insertAndRegisterData(slashingProtection, 2, 2, 2);

    slashingProtection.prune();

    final List<SignedAttestation> attestationsForValidator1 = fetchAttestations(1);
    assertThat(attestationsForValidator1).hasSize(2);

    final List<SignedAttestation> attestationsForValidator2 = fetchAttestations(2);
    assertThat(attestationsForValidator2).hasSize(1);
  }

  @Test
  void watermarkIsNotMovedLower() {
    final SlashingProtection slashingProtection =
        SlashingProtectionFactory.createSlashingProtection(
            new TestSlashingProtectionParameters(databaseUrl, USERNAME, PASSWORD, 5, 1));
    insertAndRegisterData(slashingProtection, 10, 10, 1);
    jdbi.useTransaction(
        h -> {
          lowWatermarkDao.updateSlotWatermarkFor(h, 1, UInt64.valueOf(8));
          lowWatermarkDao.updateEpochWatermarksFor(h, 1, UInt64.valueOf(8), UInt64.valueOf(8));
        });
    slashingProtection.prune();

    // we are only able to prune 2 entries because the watermark is at 8
    assertThat(fetchAttestations(1)).hasSize(2);
    assertThat(fetchBlocks(1)).hasSize(2);
    assertThat(getWatermark(1).getSlot()).isEqualTo(UInt64.valueOf(8));
  }

  @Test
  void noPruningOccursWhenThereIsNoWatermark() {
    final SlashingProtection slashingProtection =
        SlashingProtectionFactory.createSlashingProtection(
            new TestSlashingProtectionParameters(databaseUrl, USERNAME, PASSWORD, 1, 1));
    slashingProtection.registerValidators(List.of(Bytes.of(1)));
    for (int i = 0; i < 5; i++) {
      insertBlockAt(UInt64.valueOf(i), 1);
    }
    for (int i = 0; i < 5; i++) {
      insertAttestationAt(UInt64.valueOf(1), UInt64.valueOf(1), 1);
    }

    slashingProtection.prune();
    assertThat(fetchAttestations(1)).hasSize(5);
    assertThat(fetchBlocks(1)).hasSize(5);
  }

  private void insertAndRegisterData(
      final SlashingProtection slashingProtection,
      final int noOfBlocks,
      final int noOfAttestations,
      final int validatorId) {
    final Bytes validatorPublicKey = Bytes.of(validatorId);
    slashingProtection.registerValidators(List.of(validatorPublicKey));
    insertData(noOfBlocks, noOfAttestations, validatorId);
  }

  private void insertData(final int noOfBlocks, final int noOfAttestations, final int validatorId) {
    for (int b = 0; b < noOfBlocks; b++) {
      insertBlockAt(UInt64.valueOf(b), validatorId);
    }
    for (int a = 0; a < noOfAttestations; a++) {
      insertAttestationAt(UInt64.valueOf(a), UInt64.valueOf(a), validatorId);
    }

    jdbi.useTransaction(
        h -> {
          lowWatermarkDao.updateSlotWatermarkFor(h, validatorId, UInt64.ZERO);
          lowWatermarkDao.updateEpochWatermarksFor(h, validatorId, UInt64.ZERO, UInt64.ZERO);
        });
  }
}

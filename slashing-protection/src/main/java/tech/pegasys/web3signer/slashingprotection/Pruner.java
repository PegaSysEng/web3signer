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

import static com.google.common.base.Preconditions.checkArgument;
import static org.jdbi.v3.core.transaction.TransactionIsolationLevel.READ_UNCOMMITTED;
import static tech.pegasys.web3signer.slashingprotection.DbLocker.lockForValidator;

import tech.pegasys.web3signer.slashingprotection.DbLocker.LockType;
import tech.pegasys.web3signer.slashingprotection.dao.LowWatermarkDao;
import tech.pegasys.web3signer.slashingprotection.dao.SignedAttestationsDao;
import tech.pegasys.web3signer.slashingprotection.dao.SignedBlocksDao;
import tech.pegasys.web3signer.slashingprotection.dao.SigningWatermark;

import java.util.Optional;

import org.apache.tuweni.units.bigints.UInt64;
import org.apache.tuweni.units.bigints.UInt64s;
import org.jdbi.v3.core.Jdbi;

public class Pruner {
  private final Jdbi jdbi;
  private final SignedBlocksDao signedBlocksDao;
  private final SignedAttestationsDao signedAttestationsDao;
  private final LowWatermarkDao lowWatermarkDao;

  public Pruner(
      final Jdbi jdbi,
      final SignedBlocksDao signedBlocksDao,
      final SignedAttestationsDao signedAttestationsDao,
      final LowWatermarkDao lowWatermarkDao) {
    this.jdbi = jdbi;
    this.signedBlocksDao = signedBlocksDao;
    this.signedAttestationsDao = signedAttestationsDao;
    this.lowWatermarkDao = lowWatermarkDao;
  }

  public void pruneForValidator(
      final int validatorId, final long epochsToKeep, final long slotsPerEpoch) {
    checkArgument(
        epochsToKeep > 0,
        "epochsToKeep to keep must be a positive value, but was %s",
        epochsToKeep);
    checkArgument(
        slotsPerEpoch > 0, "slotsPerEpoch must be a positive value, but was %", slotsPerEpoch);
    final long slotsToKeep = Math.max(epochsToKeep / slotsPerEpoch, 1);
    pruneBlocks(validatorId, slotsToKeep);
    pruneAttestations(validatorId, epochsToKeep);
  }

  private void pruneBlocks(final int validatorId, final long slotsToKeep) {
    final Optional<UInt64> pruningSlot =
        jdbi.inTransaction(
            READ_UNCOMMITTED,
            h -> {
              lockForValidator(h, LockType.BLOCK, validatorId);
              final Optional<UInt64> watermarkSlot =
                  lowWatermarkDao
                      .findLowWatermarkForValidator(h, validatorId)
                      .map(SigningWatermark::getSlot);
              final Optional<UInt64> slot = signedBlocksDao.findMaxSlot(h, validatorId);
              final Optional<UInt64> slotToPruneTo =
                  calculatePruningMark(slotsToKeep, slot, watermarkSlot);
              slotToPruneTo.ifPresent(
                  s -> lowWatermarkDao.updateSlotWatermarkFor(h, validatorId, s));
              return slotToPruneTo;
            });

    pruningSlot.ifPresent(
        s ->
            jdbi.useTransaction(
                READ_UNCOMMITTED, h -> signedBlocksDao.deleteBlocksBelowSlot(h, validatorId, s)));
  }

  private void pruneAttestations(final int validatorId, final long epochsToKeep) {
    final Optional<UInt64> pruningEpoch =
        jdbi.inTransaction(
            READ_UNCOMMITTED,
            h -> {
              lockForValidator(h, LockType.ATTESTATION, validatorId);
              final Optional<UInt64> watermarkEpoch =
                  lowWatermarkDao
                      .findLowWatermarkForValidator(h, validatorId)
                      .map(SigningWatermark::getTargetEpoch);
              final Optional<UInt64> epoch =
                  signedAttestationsDao.findMaxTargetEpoch(h, validatorId);
              final Optional<UInt64> epochToPruneTo =
                  calculatePruningMark(epochsToKeep, epoch, watermarkEpoch);
              epochToPruneTo.ifPresent(
                  e -> lowWatermarkDao.updateEpochWatermarksFor(h, validatorId, e, e));
              return epochToPruneTo;
            });

    pruningEpoch.ifPresent(
        e ->
            jdbi.useTransaction(
                READ_UNCOMMITTED,
                h -> signedAttestationsDao.deleteAttestationsBelowEpoch(h, validatorId, e)));
  }

  private Optional<UInt64> calculatePruningMark(
      final long amountToPrune,
      final Optional<UInt64> highpoint,
      final Optional<UInt64> watermark) {
    return highpoint.flatMap(
        h ->
            watermark.map(
                w -> {
                  final UInt64 pruningPoint =
                      h.compareTo(UInt64.valueOf(amountToPrune)) <= -1
                          ? UInt64.ZERO
                          : h.subtract(amountToPrune).add(1);
                  final UInt64 newWatermark = UInt64s.max(UInt64.ZERO, pruningPoint);
                  return UInt64s.max(newWatermark, w);
                }));
  }
}
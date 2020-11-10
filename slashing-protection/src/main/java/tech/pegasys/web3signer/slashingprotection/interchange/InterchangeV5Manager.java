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
package tech.pegasys.web3signer.slashingprotection.interchange;

import tech.pegasys.web3signer.slashingprotection.dao.SignedAttestationsDao;
import tech.pegasys.web3signer.slashingprotection.dao.SignedBlocksDao;
import tech.pegasys.web3signer.slashingprotection.dao.ValidatorsDao;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.NotImplementedException;
import org.jdbi.v3.core.Jdbi;

public class InterchangeV5Manager implements InterchangeManager {

  private final InterchangeV5Exporter exporter;

  public InterchangeV5Manager(
      final Jdbi jdbi,
      final ValidatorsDao validatorsDao,
      final SignedBlocksDao signedBlocksDao,
      final SignedAttestationsDao signedAttestationsDao,
      final ObjectMapper mapper) {
    exporter =
        new InterchangeV5Exporter(
            jdbi, validatorsDao, signedBlocksDao, signedAttestationsDao, mapper);
  }

  @Override
  public void importData(InputStream in) throws IOException {
    throw new NotImplementedException(
        "Importing of interchange data to be performed in later release");
  }

  @Override
  public void export(final OutputStream out) throws IOException {
    exporter.export(out);
  }
}
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
package tech.pegasys.eth2signer.tests.comparison;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.eth2signer.dsl.lotus.FilecoinJsonRequests.walletHas;
import static tech.pegasys.eth2signer.dsl.lotus.FilecoinJsonRequests.walletList;
import static tech.pegasys.eth2signer.dsl.lotus.FilecoinJsonRequests.walletSign;

import tech.pegasys.eth2signer.core.service.jsonrpc.FilecoinSignature;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariables;

@EnabledIfEnvironmentVariables({
  @EnabledIfEnvironmentVariable(named = "LOTUS_PORT", matches = ".*")
})
public class CompareFilecoinApis extends CompareApisAcceptanceTestBase {

  @BeforeEach
  void initSigner() {
    super.initAndStartSigner(true);
  }

  @Test
  void compareWalletHasResponses() {
    addressMap.forEach(
        (address, key) -> {
          assertThat(walletHas(LOTUS_NODE.getJsonRpcClient(), address)).isTrue();
          assertThat(walletHas(getSignerJsonRpcClient(), address)).isTrue();
        });

    nonExistentAddressMap.forEach(
        (address, key) -> {
          assertThat(walletHas(LOTUS_NODE.getJsonRpcClient(), address)).isFalse();
          assertThat(walletHas(getSignerJsonRpcClient(), address)).isFalse();
        });
  }

  @Test
  void compareWalletListResponses() {
    final List<String> lotusWalletList = walletList(LOTUS_NODE.getJsonRpcClient());
    final List<String> signerWalletList = walletList(getSignerJsonRpcClient());

    // note: lotus node may have additional miner addresses which aren't loaded in Signer.
    Assertions.assertThat(lotusWalletList).containsAll(signerWalletList);
  }

  @Test
  void compareWalletSignResponses() {
    for (int i = 0; i < 100; i++) {
      addressMap.forEach(
          (address, key) -> {
            final Bytes dataToSign = Bytes.random(32);
            final FilecoinSignature lotusFcSig =
                walletSign(LOTUS_NODE.getJsonRpcClient(), address, dataToSign);
            final FilecoinSignature signerFcSig =
                walletSign(getSignerJsonRpcClient(), address, dataToSign);

            assertThat(signerFcSig).isEqualTo(lotusFcSig);
          });
    }
  }
}

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
package tech.pegasys.eth2signer.core.service.jsonrpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigInteger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt64;
import org.web3j.abi.datatypes.generated.Uint64;

@SuppressWarnings("UnusedVariable")
@JsonPropertyOrder({ "Version", "To", "From", "Nonce", "Value", "GasPrice", "GasLimit", "Method", "Params" })
public class FilecoinMessage {

  //Version gets written as a uint64. or (-version-1 if < 0)
  //To & From are strings
  //Nonce is written as a positive number
  //Value is written as an array of bytes
  //GasPrice is written as an array of bytes
  //GasLimit is written as uint64 or(-version-1 if <0)
  //method written as uint64
  //params written as bytes if small enough


  @JsonProperty("Version")
  private final long version;

  @JsonProperty("To")
  private final String to;

  @JsonProperty("From")
  private final String from;

  @JsonProperty("Nonce")
  private final UInt64 nonce;

  @JsonProperty("Value")
  private final BigInteger value;

  @JsonProperty("GasFeeCap")
  private final BigInteger gasFeeCap;

  @JsonProperty("GasPremium")
  private final BigInteger gasPremium;

  @JsonProperty("GasLimit")
  private final long gasLimit;

  @JsonProperty("Method")
  private final UInt64 method;

  @JsonProperty("Params")
  private final Bytes params;

  @JsonCreator
  public FilecoinMessage(
      final @JsonProperty("Version") long version,
      final @JsonProperty("To") String to,
      final @JsonProperty("From") String from,
      final @JsonProperty("Nonce") UInt64 nonce,
      final @JsonProperty("Value") BigInteger value,
      final @JsonProperty("GasLimit") long gasLimit,
      final @JsonProperty("GasFeeCap") BigInteger gasFeeCap,
      final @JsonProperty("GasPremium") BigInteger gasPremium,
      final @JsonProperty("Method") UInt64 method,
      final @JsonProperty("Params") String params) {
    this.version = version;
    this.to = to;
    this.from = from;
    this.nonce = nonce;
    this.value = value;
    this.gasLimit = gasLimit;
    this.gasFeeCap = gasFeeCap;
    this.gasPremium = gasPremium;

    this.method = method;
    this.params = Bytes.fromBase64String(params);
  }

  public long getVersion() {
    return version;
  }

  public String getTo() {
    return to;
  }

  public String getFrom() {
    return from;
  }

  public UInt64 getNonce() {
    return nonce;
  }

  public BigInteger getValue() {
    return value;
  }

  public long getGasLimit() {
    return gasLimit;
  }

  public BigInteger getGasFeeCap() {
    return gasFeeCap;
  }

  public BigInteger getGasPremium() {
    return gasPremium;
  }

  public UInt64 getMethod() {
    return method;
  }

  @JsonGetter("Params")
  public String getParams() {
    return params.toBase64String();
  }
}

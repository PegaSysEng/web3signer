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
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package tech.pegasys.eth2signer.core.service.jsonrpc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.math.BigInteger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt64;
import tech.pegasys.eth2signer.core.multikey.metadata.SigningMetadataException;
import tech.pegasys.eth2signer.core.multikey.metadata.parser.SigningMetadataModule.HexStringDeserialiser;
import tech.pegasys.eth2signer.core.multikey.metadata.parser.SigningMetadataModule.HexStringSerializer;

public class CborModule extends SimpleModule {

  public CborModule() {
    super("CborMessage");
    addDeserializer(Bytes.class, new HexStringDeserialiser());
    addSerializer(Bytes.class, new HexStringSerializer());
    addDeserializer(UInt64.class, new UInt64Deserializer());
    addSerializer(UInt64.class, new UInt64Serialiser());

  }

  public static class UInt64Deserializer extends JsonDeserializer<UInt64> {
    @Override
    public UInt64 deserialize(final JsonParser p, final DeserializationContext ctxt) {
      try {
        return UInt64.valueOf(p.getBigIntegerValue());
      } catch (final Exception e) {
        throw new SigningMetadataException("Invalid hex value for private key", e);
      }
    }
  }

  public static class UInt64Serialiser extends JsonSerializer<UInt64> {
    @Override
    public void serialize(
        final UInt64 value, final JsonGenerator gen, final SerializerProvider serializers)
        throws IOException {
      gen.writeNumber(value.toBigInteger());
    }
  }

  public static class BigIntegerDeserializer extends JsonDeserializer<BigInteger> {
    @Override
    public BigInteger deserialize(final JsonParser p, final DeserializationContext ctxt) {
      try {
        return new BigInteger(p.getValueAsString());
      } catch (final Exception e) {
        throw new SigningMetadataException("Invalid hex value for private key", e);
      }
    }
  }

  public static class BigIntegerSerialiser extends JsonSerializer<BigInteger> {
    @Override
    public void serialize(
        final BigInteger value, final JsonGenerator gen, final SerializerProvider serializers)
        throws IOException {
      gen.writeString(value.toString(10));
    }
  }

}

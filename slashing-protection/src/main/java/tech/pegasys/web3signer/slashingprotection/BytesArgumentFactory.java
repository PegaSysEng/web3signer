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
package tech.pegasys.web3signer.slashingprotection;

import java.sql.Types;

import org.apache.tuweni.bytes.Bytes;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

public class BytesArgumentFactory extends AbstractArgumentFactory<Bytes> {

  public BytesArgumentFactory() {
    super(Types.BINARY);
  }

  @Override
  protected Argument build(final Bytes value, final ConfigRegistry config) {
    return (position, statement, ctx) -> statement.setBytes(position, value.toArrayUnsafe());
  }
}

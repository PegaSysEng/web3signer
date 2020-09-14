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
package tech.pegasys.web3signer.commandline.subcommands;

import tech.pegasys.web3signer.core.Eth2Runner;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = Eth2SubCommand.COMMAND_NAME,
    description = "Handle Ethereum-2 BLS signing operations and public key reporting",
    mixinStandardHelpOptions = true)
public class Eth2SubCommand extends ModeSubCommand {

  public static final String COMMAND_NAME = "eth2";

  @Option(
      names = {"--Xslashing-protection-enabled"},
      hidden = true,
      description =
          "Set to true if all Eth2 signing operations should be validated against historic data, "
              + "prior to responding with signatures"
              + "(default: ${DEFAULT-VALUE})",
      paramLabel = "<BOOL>",
      arity = "1")
  private boolean slashingProtectionEnabled = false;

  @Option(
      names = {"--slashing-protection-db-url"},
      description = "The jdbc url to use to connect to the slashing protection database",
      paramLabel = "<storage label>",
      arity = "1")
  private String slashingProtectionDbUrl;

  @Option(
      names = {"--slashing-protection-db-username"},
      description = "The username to use when connecting to the slashing protection database")
  private String slashingProtectionDbUser;

  @Option(
      names = {"--slashing-protection-db-password"},
      description = "The password to use when connecting to the slashing protection database")
  private String slashingProtectionDbPassword;

  @Override
  public Eth2Runner createRunner() {
    return new Eth2Runner(
        config,
        slashingProtectionEnabled,
        slashingProtectionDbUrl,
        slashingProtectionDbUser,
        slashingProtectionDbPassword);
  }

  @Override
  public String getCommandName() {
    return COMMAND_NAME;
  }
}
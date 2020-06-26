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
package tech.pegasys.eth2signer.tests;

import static io.restassured.RestAssured.given;

import tech.pegasys.eth2signer.dsl.signer.SignerConfigurationBuilder;

import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Test;

public class SwaggerUIAcceptanceTest extends AcceptanceTestBase {

  private static final String SWAGGER_UI_ENDPOINT = "/swagger-ui";

  @Test
  void swaggerUiEndPointRespondsWith200() {
    final SignerConfigurationBuilder builder = new SignerConfigurationBuilder();
    startSigner(builder.build());

    given()
        .baseUri(signer.getUrl())
        .when()
        .get(SWAGGER_UI_ENDPOINT)
        .then()
        .assertThat()
        .statusCode(200)
        .body(StringContains.containsString("<title>Eth2Signer Service OpenApi</title>"));
  }

  @Test
  void swaggerUiWithTrailingSlashEndPointRespondsWith200() {
    final SignerConfigurationBuilder builder = new SignerConfigurationBuilder();
    startSigner(builder.build());

    given()
        .baseUri(signer.getUrl())
        .when()
        .get(SWAGGER_UI_ENDPOINT + "/")
        .then()
        .assertThat()
        .statusCode(200)
        .body(StringContains.containsString("<title>Eth2Signer Service OpenApi</title>"));
  }

  @Test
  void eth2SignerYamlEndPointRespondsWith200() {
    final SignerConfigurationBuilder builder = new SignerConfigurationBuilder();
    startSigner(builder.build());

    given()
        .baseUri(signer.getUrl())
        .when()
        .get(SWAGGER_UI_ENDPOINT + "/eth2signer.yaml")
        .then()
        .assertThat()
        .statusCode(200)
        .body(StringContains.containsString("openapi: 3.0.0"));
  }
}

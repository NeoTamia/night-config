# Changelog

## [3.11.0](https://github.com/NeoTamia/night-config/compare/v3.10.5...v3.11.0) (2026-01-31)


### ✨ Features

* Add SerdeConfig support for default values and assertions ([6633a1d](https://github.com/NeoTamia/night-config/commit/6633a1d7b078779dcf76807361669ed3dfa0c70f))
* **build:** Add Renovate configuration for dependency management ([0f9517b](https://github.com/NeoTamia/night-config/commit/0f9517bc96420fe57a142e7a60858097d704ca95))
* **core:** Add `SerdeContext` support to various config types ([f83587d](https://github.com/NeoTamia/night-config/commit/f83587d1cf585a230ef9426ba9418ffd2c613a74))
* **core:** Add `TypeAdapter` and `TypeAdapterProvider` interfaces with tests for generic type handling ([78ba188](https://github.com/NeoTamia/night-config/commit/78ba1889ff19a0c2ce52771356776ef8446d6c9b))
* **core:** Add `TypeAdapter` and `TypeAdapterProvider` interfaces with tests for generic type handling ([fb47fe5](https://github.com/NeoTamia/night-config/commit/fb47fe5e2cf52c81e405664ab94e40db1af0eb22))
* **core:** Add deserialization and serialization support for additional types ([5d4572d](https://github.com/NeoTamia/night-config/commit/5d4572df04358c5fde3a9dda391c1994f9e78939))
* **core:** Extract key validation logic for serialization and deserialization ([50804bf](https://github.com/NeoTamia/night-config/commit/50804bf0a5a69fb9792c605efb8181d19bd21421))
* **examples:** Add advanced `TypeAdapter` implementations for handling generic types with examples ([fe64998](https://github.com/NeoTamia/night-config/commit/fe649987366dfd2b562c9e08021ed16e3c59459e))
* **serde:** Add deserializer and serializer registration methods ([40d4622](https://github.com/NeoTamia/night-config/commit/40d46221b468e71b63b952eb4116d26fbf52c14f))
* **serde:** Add header comment parsing and processing to HOCON configuration ([9e6d1bc](https://github.com/NeoTamia/night-config/commit/9e6d1bc3471156e766d3e55be847128fad3b5a32))
* **serde:** Add header comment support to CommentedConfig and related classes ([e53131b](https://github.com/NeoTamia/night-config/commit/e53131b57300a2aba0e211dfa67da93d00af5416))
* **serde:** Add setNamingStrategy method to ObjectDeserializer and ObjectSerializer ([3a04de1](https://github.com/NeoTamia/night-config/commit/3a04de14660289078b55c677c470a3d2ee900ef7))
* **serde:** Implement header comment support in YamlParser and YamlWriter ([003be7a](https://github.com/NeoTamia/night-config/commit/003be7adeaa92ee52007cfc123729232c1f0f981))
* **serde:** Introduce `SerdeContext` with builder pattern, type-aware get/set support, and TypeAdapter runtime registration ([0f9f22f](https://github.com/NeoTamia/night-config/commit/0f9f22feb5263ef24bcf0e578ec15d25ba9a81bc))
* **serde:** Introduce NamingStrategy for field name transformation ([ee3249b](https://github.com/NeoTamia/night-config/commit/ee3249b3842e95d2a16a32cf9141389d8719dcea))
* **serde:** Update annotations to support default values for key and skip conditions ([3089ff2](https://github.com/NeoTamia/night-config/commit/3089ff2e90f94c6a6752c061c6002edd6490b370))
* **toml:** Add support for header comments in TOML parsing and writing ([61ab76b](https://github.com/NeoTamia/night-config/commit/61ab76b9329c8c0928161dd5ec7d89a4e53b3282))
* Update version to 3.9.3 and add FloatDeserializer ([8b45936](https://github.com/NeoTamia/night-config/commit/8b459364e533f720db5d44bcd82a5adf0319c38a))
* **yaml:** ✨ Upgrade to SnakeYaml Engine V2 with comment support ([4fd4cf4](https://github.com/NeoTamia/night-config/commit/4fd4cf41d365554e9d11f05dc8bd6a7988681161))
* **yaml:** Add support for comments in YamlWriter using SnakeYaml Engine V2 ([22f460b](https://github.com/NeoTamia/night-config/commit/22f460b38d50d48ef9acf90b2b5abe9e4afbe3f5))
* **yaml:** Enhance YamlParser and YamlWriter to support comments in YAML files ([ace2f02](https://github.com/NeoTamia/night-config/commit/ace2f029d713c7bb4cad80da18fdd9afa6f3924b))


### 🐛 Bug Fixes

* 195: NoSuchFileException when writing config to relative path with ATOMIC_REPLACE ([8f7f9e0](https://github.com/NeoTamia/night-config/commit/8f7f9e06cfc5adbb9a07e4f6533939479504784d))
* NPE inside ConfigParser#parseHeaderComment ([88e67ea](https://github.com/NeoTamia/night-config/commit/88e67ea586be77097de7733605b56b47cd1cb11a))
* **parser:** Remove header comment on the first key ([e57cedb](https://github.com/NeoTamia/night-config/commit/e57cedb5eaea9986cb92b7dd88b9c0d2342490f9))


### 📚 Documentation

* **templates:** Add issue templates, PR template, and code of conduct ([0fefffb](https://github.com/NeoTamia/night-config/commit/0fefffb867792293e33ec1ffb33aeb83e0090094))
* Update README with project info, a table of contents, and detailed sections covering features like FileConfig, ConfigSpec, and the Serde system. ([fd362b7](https://github.com/NeoTamia/night-config/commit/fd362b792078cfdf36d7d514dac7a7fe558493be))


### ♻️ Code Refactoring

* **config:** Set insertion order preserved to true by default ([eb342c8](https://github.com/NeoTamia/night-config/commit/eb342c8001552c3a28dc87a5586e832956765550))
* **core, test:** Replace `List.get(index)` with `getFirst()` for improved readability and usage consistency ([407443d](https://github.com/NeoTamia/night-config/commit/407443d6100772cc262477d50751f1f829169bb2))
* **core, test:** Replace lambdas with method references for improved readability and consistency ([ddde66a](https://github.com/NeoTamia/night-config/commit/ddde66a58426fdff85b81cf61ae7ae34520aaad4))
* **core, toml, json:** Add `@NotNull` annotations and simplify type declarations for improved readability and null-safety ([e8d9d48](https://github.com/NeoTamia/night-config/commit/e8d9d484cbdbeca00df5116a4258846b9094c7ac))
* **core:** Enhance `RiskyNumberDeserializer` to support more numeric types and clean up lossy conversion logic ([5c919e8](https://github.com/NeoTamia/night-config/commit/5c919e821434e9edb9c3a3a40873e977688dd43b))
* **core:** Remove duplicate `@NotNull` annotations for improved readability ([a3de2cb](https://github.com/NeoTamia/night-config/commit/a3de2cbba27fd26aaeae1af416b2335ac2f3352e))
* **core:** Remove Java 17 specific POJO deserialization and unify logic ([6a667a1](https://github.com/NeoTamia/night-config/commit/6a667a17072582288d32ca363ea4186f3e0d001e))
* **core:** Remove unused `Optional` imports across deserialization classes ([7a4cfd1](https://github.com/NeoTamia/night-config/commit/7a4cfd1ce6a9a19db7bc7da916f6599fee0894a9))
* **core:** Remove unused imports and redundant exception handling across core and test classes ([15b0c5a](https://github.com/NeoTamia/night-config/commit/15b0c5adf793dca8e1755b23a46427097e23672f))
* **core:** Replace `Optional<TypeConstraint>` with nullable `TypeConstraint` for deserialization ([c40682d](https://github.com/NeoTamia/night-config/commit/c40682df7ba24f8a42b4a2f87f874a54e8d28bca))
* **core:** Replace inner classes with `record` for improved readability and conciseness ([eaac573](https://github.com/NeoTamia/night-config/commit/eaac57375ca6214fb2adb332d14ff9218a24c55e))
* **core:** Simplify type checks, imports, and list operations across core classes ([4f33215](https://github.com/NeoTamia/night-config/commit/4f33215873580cc8d1d17214b7c9e81db6ee921c))
* **core:** Standardize `@NotNull` annotations across serializers and deserializers for improved null-safety and readability ([1cbabc4](https://github.com/NeoTamia/night-config/commit/1cbabc470d094a0823930b839b3c460ddf08d8bf))
* **core:** Update null-safety annotations in serializers and deserializers for consistency and improved clarity ([86d47c3](https://github.com/NeoTamia/night-config/commit/86d47c3472375e7dacfc5ad7c856885674b00e5d))
* **format:** Disable auto registration of format ([83e91d0](https://github.com/NeoTamia/night-config/commit/83e91d03cc91d0f8e3d32013528901de1593f731))
* **json:** Reorganize JSON test files and update configuration initialization ([89028ff](https://github.com/NeoTamia/night-config/commit/89028ff37d9815ebf3e8d1a9c852d47b6ac8b4d5))
* **package:** Change package names from com.electronwill to re.neotamia ([bad32b9](https://github.com/NeoTamia/night-config/commit/bad32b9bb9882f97ba07e4c31007ef59e5b97cf3))
* **parser:** Migrate `switch` statements to `switch` expressions for improved conciseness ([691d55b](https://github.com/NeoTamia/night-config/commit/691d55b66f0f2f24d550b930ee068bbb335aa850))
* **serde:** Add SerdeComment, SerdeKey and SerdeAssert from @SerdeConfig ([b5794cf](https://github.com/NeoTamia/night-config/commit/b5794cf3de949b46e416583ef0417b15f6e6ae67))
* **serde:** Enhance deserialization logic with improved field skipping and assertion handling ([5d1686a](https://github.com/NeoTamia/night-config/commit/5d1686a64ec8f8d7ff2837364a8ef451f687a2a2))
* **serde:** Introduce AbstractDeSerializerContext for shared deserialization logic ([b5794cf](https://github.com/NeoTamia/night-config/commit/b5794cf3de949b46e416583ef0417b15f6e6ae67))
* **serde:** Remove obsolete `ReproductionTest` and enhance null-safety in serializers and deserializers ([4e06236](https://github.com/NeoTamia/night-config/commit/4e06236b680a5e7b05753e7c56eefc5b95213236))
* **serde:** Remove unused deserializer and serializer methods ([f641b5b](https://github.com/NeoTamia/night-config/commit/f641b5bd502b2c58fd639c1050e8fa82e89e2964))
* **serde:** Update nullability annotations in serializers/deserializers for improved type safety and consistency ([6cfcb50](https://github.com/NeoTamia/night-config/commit/6cfcb508e03b0b71fad76c1d8ff68f6aaadc4832))
* **serde:** Update YAML Header comment parsing/processing ([f9ef08c](https://github.com/NeoTamia/night-config/commit/f9ef08c2abd0f5c51bbdc6284f8a4284cebb66cd))
* **toml:** Use try-with-resources for `CharsWrapper.Builder` and improve string handling consistency ([fb45be6](https://github.com/NeoTamia/night-config/commit/fb45be663fdda875332edc3126576ead3c2cc2c5))
* Update builder methods ([d56282e](https://github.com/NeoTamia/night-config/commit/d56282ee21c1784cee459bca7d458688f14673f7))
* Update project metadata and repository URLs ([416eb8f](https://github.com/NeoTamia/night-config/commit/416eb8f3ff833109a51d421350d752ed94f20bbb))
* **yaml:** Add `Map` support in `YamlWriter` and update related tests ([206e389](https://github.com/NeoTamia/night-config/commit/206e38993213fd589fca8bb9e4e867eac2d0c07e))
* **yaml:** Update YamlFormat and YamlParser to use CommentedConfig ([d22b8dd](https://github.com/NeoTamia/night-config/commit/d22b8dd0c0cb9fdc346244b6d3de24af68f3855a))


### 🧪 Tests

* **config:** Add YAML, TOML, and JSON configuration files with sample data ([586b956](https://github.com/NeoTamia/night-config/commit/586b9562bd11970b748828a32831f1ee4a16c462))
* **config:** Set Insertion Order Preserved ([02d6451](https://github.com/NeoTamia/night-config/commit/02d6451c869e7e1237d8a35e2304564523a801c7))
* **core:** Add unit test for hex, binary, and octal serialization ([ab68f27](https://github.com/NeoTamia/night-config/commit/ab68f273163f1a0457aacce48080238b09fc2ad3))
* **core:** Add unit tests for inheritance, collections, and various data types ([ede78cd](https://github.com/NeoTamia/night-config/commit/ede78cd0a8b7270dce7399a2c486ecdb7ad7bf42))
* **core:** Update `NullableBoxConfig` to handle `@SerdeDefault` for missing values in deserialization ([d737add](https://github.com/NeoTamia/night-config/commit/d737add5a6ad86d61abae208f67a12283cb82bbf))
* Enhance HOCON tests with updated serialized output and comment parsing logic ([f7ea6e2](https://github.com/NeoTamia/night-config/commit/f7ea6e2d8e5c4ca8375cdcc60bea504d71b66dfa))
* Reorganize and update test resources for YAML, TOML, and JSON configurations ([4124243](https://github.com/NeoTamia/night-config/commit/412424371fd28b81349a05fbb29ff11904418aea))
* **serde:** Add assertions for null comments in HOCON and YAML header tests ([716806a](https://github.com/NeoTamia/night-config/commit/716806ab48cc3c9d47610c1401cb30a55a5e0f60))
* **serde:** Java update ([01b0df3](https://github.com/NeoTamia/night-config/commit/01b0df3979f24a297ebdb48a506baa10958d8e1e))
* **toml:** Clean up TOML comment tests and remove redundant header comment cases ([4b90c81](https://github.com/NeoTamia/night-config/commit/4b90c81e3a48aa0fac9ac947bac7f2f3a185d40e))
* **yaml:** Restore and enhance YAML comment support and preservation tests ([bb251c1](https://github.com/NeoTamia/night-config/commit/bb251c1870617bb306a684ba9ead1fe718229986))


### 🔧 Build System

* **deps:** Pin cimg/openjdk docker tag to 381003f ([#12](https://github.com/NeoTamia/night-config/issues/12)) ([2acdab5](https://github.com/NeoTamia/night-config/commit/2acdab56f196e3f43b7996257a03bbc5343dad7f))
* **gradle:** Add credentials for neotamiaSnapshots repository ([411a4c9](https://github.com/NeoTamia/night-config/commit/411a4c9e809835c42eaad201c55d78b7ad4ecc46))
* **gradle:** Configure finalized tasks and JaCoCo reports ([9ffc8df](https://github.com/NeoTamia/night-config/commit/9ffc8dff85f69e2d8ca619cd6e028e9d5ea3698c))
* **gradle:** Fix neotamia repository ([6375c57](https://github.com/NeoTamia/night-config/commit/6375c5717a7f6eee94eb0aad86f4830a45d09b1c))


### 👷 Continuous Integration

* **build:** Add GitHub Actions workflow for build and publish ([83dd5d4](https://github.com/NeoTamia/night-config/commit/83dd5d442f9751be265fc144288b437c1c24f289))
* **build:** Update workflow to skip tests during build ([37cebd0](https://github.com/NeoTamia/night-config/commit/37cebd00d74c5d61554f84234fc47001bc7a199d))
* **release-please:** Integrate Release Please for automated release management ([ef780d8](https://github.com/NeoTamia/night-config/commit/ef780d87460b48e5036945123cd051531c0bc20a))
* **renovate:** Remove `$default` from base branch patterns ([c2256fd](https://github.com/NeoTamia/night-config/commit/c2256fd0939e6823972469784cc94ac8e1fa5d85))
* **test:** Add GitHub Actions workflow for testing Java and Kotlin files ([33bcb0b](https://github.com/NeoTamia/night-config/commit/33bcb0b8dac777d3e41f932f60b41313c9ff7507))
* **test:** Enable recursive submodule checkout in test workflow ([d102f25](https://github.com/NeoTamia/night-config/commit/d102f2550d10dba26ee578f14c8ed08799cb3b2e))
* **test:** Inherit secrets and enable Codecov in test workflow ([f858df5](https://github.com/NeoTamia/night-config/commit/f858df54bb229b35d47b915fdb727c755929147f))

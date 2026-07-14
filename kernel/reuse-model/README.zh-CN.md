# `:kernel:reuse-model`

[English](README.md) | 绠€浣撲腑鏂?
`:kernel:reuse-model` 妯″潡瀹氫箟 Athena 鍦?M16 鐨勭涓€灞傚钩鍙扮骇 Semantic Macro contract銆?
杩欎釜妯″潡鎶婃墍鏈夋潈閾捐矾鍥哄畾涓猴細

`reuse entry surface -> semantic macro contract -> template payloads -> runtime reuse services -> M8 mutation authority -> Engineering IR`

## 鑱岃矗

- 閫氳繃 `SemanticMacroId` 鍙戝竷绋冲畾鐨?Semantic Macro identity銆?- 閫氳繃 `SemanticMacroInstantiationId` 鍙戝竷绋冲畾鐨?instantiation identity銆?- 閫氳繃 `SemanticMacroPackageBinding` 鍙戝竷鍙?package governance 绾︽潫鐨?definition binding锛屽悓鏃朵笉璁?package identity 鍙栦唬 macro identity銆?- 閫氳繃浠ヤ笅绫诲瀷鍙戝竷涓庣晫闈㈡棤鍏崇殑 parameter schema锛?  - `SemanticMacroParameterName`
  - `SemanticMacroParameterValueKind`
  - `SemanticMacroParameterValue`
  - `SemanticMacroParameterDefinition`
- 閫氳繃浠ヤ笅绫诲瀷鍙戝竷 review-first preview contract锛?  - `SemanticMacroPreview`
  - `SemanticMacroPreviewChange`
- 閫氳繃浠ヤ笅绫诲瀷鍙戝竷 accepted expansion traceability contract锛?  - `SemanticMacroAcceptedExpansion`
  - `ExpansionOrigin`
  - `ExpansionMembership`
- 淇濇寔 semantic reuse 鏄?semantic-first銆佸彈 package governance 绾︽潫锛屽苟涓斾綅浜?M8 mutation authority 鐨勪笅娓歌竟鐣屻€?
## 涓昏绫诲瀷

- `SemanticMacroId`
- `SemanticMacroInstantiationId`
- `SemanticMacroParameterName`
- `SemanticMacroPackageBinding`
- `SemanticMacroContract`
- `SemanticMacroPreviewId`
- `SemanticMacroPreviewChangeKind`
- `SemanticMacroPreviewStatus`
- `SemanticMacroPreview`
- `SemanticMacroExpansionId`
- `SemanticMacroAcceptedExpansion`

## 渚濊禆

杩欎釜妯″潡渚濊禆锛?
- `:kernel:engineering-model`锛岄€氳繃 `StableSemanticIdentity` 澶嶇敤 canonical semantic identity
- `:kernel:repository-model`锛岄€氳繃 `PackageIdentifier` 澶嶇敤鍙楁不鐞嗙殑 package identity

## 杈圭晫

杩欎釜妯″潡涓嶅畾涔?reusable template payload銆乧atalog loading銆乸arameter validation execution銆乸review execution銆乤cceptance orchestration銆乀heia widget銆丩SP handler銆乺enderer behavior 鎴?domain-specific macro pack銆?
瀹冨彧鏄竴涓獎鑼冨洿鐨勫钩鍙?contract 灞傦紝鍚庣画 catalog銆乫orm銆丄I銆丏SL 涓?API surface 閮藉彲浠ヤ緷璧栧畠锛岃€屼笉浼氬垱閫犵浜屽 package system 鎴栫浜屾潯 mutation path銆?
## 楠岃瘉

```bash
./gradlew :kernel:reuse-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:reuse-model:test
```

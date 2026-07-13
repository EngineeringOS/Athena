# `:kernel:connection-model`

[English](README.md) | 绠€浣撲腑鏂?
`:kernel:connection-model` 妯″潡瀹氫箟 Athena 鍦?M14 鐨?semantic port knowledge contract銆?
杩欎釜妯″潡鎶婃墍鏈夋潈閾炬潯鍥哄畾涓猴細

`Engineering IR -> semantic port knowledge -> downstream M9 / projection / presentation consumers`

## 鑱岃矗

- 閫氳繃 `SemanticPortTypeId` 鍙戝竷绋冲畾 semantic port identity銆?- 鍙戝竷绋冲畾鐨?role銆乨irection銆乻ignal-family 涓?protocol 鏍囪瘑銆?- 閫氳繃 `SemanticPortDefinition` 鍙戝竷 vendor-neutral semantic port definition銆?- 閫氳繃 `ResolvedSemanticPortDefinition` 鍙戝竷鍙 resolved semantic port knowledge銆?- 淇濇寔 canonical authored port truth 浠嶇劧鐣欏湪 `Engineering IR`銆?
## 涓昏绫诲瀷

- `SemanticPortTypeId`
- `SemanticPortRoleId`
- `SemanticSignalFamilyId`
- `SemanticProtocolId`
- `SemanticPortDirection`
- `SemanticPortDefinition`
- `ResolvedSemanticPortDefinition`

## 渚濊禆

璇ユā鍧椾緷璧?`:kernel:engineering-model`锛岄€氳繃 `StableSemanticIdentity` 澶嶇敤瑙勮寖璇箟鏍囪瘑銆?
## 杈圭晫

璇ユā鍧椾笉瀹氫箟 compatibility 鎴?sufficiency judgement锛屼笉瀹氫箟 routing geometry锛屼笉鎼哄甫 graph coordinates銆乻hape ids銆亀idget state锛屼笉瀹氫箟 physical traits锛屼篃涓嶈礋璐?knowledge-pack loading銆乧ompiler orchestration銆乺untime transport銆乸rojection logic銆乸resentation logic 鎴?renderer behavior銆傛洿涓板瘜鐨勫垽鏂暀鍦?M9 鐨勪笅娓搁摼鏉￠噷锛?
`DerivedEngineeringContext -> EngineeringCapabilityFacts -> EngineeringConstraintEvaluations`

瀹冨彧鏄獎璇箟绔彛鐭ヨ瘑濂戠害灞傘€?
## 楠岃瘉

```bash
./gradlew :kernel:connection-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:connection-model:test
```

# `:kernel:component-model`

[English](README.md) | 绠€浣撲腑鏂?
`:kernel:component-model` 妯″潡瀹氫箟 Athena 鍦?M14 鐨勭涓€灞?vendor-neutral component knowledge contract銆?
杩欎釜妯″潡鎶婃墍鏈夋潈閾炬潯鍥哄畾涓猴細

`Engineering IR -> component knowledge -> vendor implementations / semantic ports / physical traits -> downstream M9 and M13 consumers`

## 鑱岃矗

- 閫氳繃 `EngineeringConceptId` 鍙戝竷绋冲畾 concept identity銆?- 閫氳繃 `EngineeringConceptDefinition` 鍙戝竷 vendor-neutral concept definition銆?- 閫氳繃 `ResolvedComponentDefinition` 鍙戝竷鍙 resolved component knowledge銆?- 淇濇寔 canonical authored truth 浠嶇劧鐣欏湪 `Engineering IR`銆?- 淇濇寔 component knowledge resolution 鏄彧璇荤粨鏋滐紝鑰屼笉鏄柊鐨?mutation path銆?
## 涓昏绫诲瀷

- `EngineeringConceptId`
- `EngineeringConceptDefinition`
- `ResolvedComponentDefinition`

## 渚濊禆

璇ユā鍧椾緷璧?`:kernel:engineering-model`锛岄€氳繃 `StableSemanticIdentity` 澶嶇敤瑙勮寖璇箟涓讳綋鏍囪瘑銆?
## 杈圭晫

璇ユā鍧椾笉瀹氫箟 vendor implementation mapping锛屼笉瀹氫箟 semantic port contract锛屼笉瀹氫箟 physical-trait contract锛屼笉璐熻矗 knowledge-pack loading銆乧ompiler orchestration銆乺untime transport銆乸rojection logic銆乸resentation logic 鎴?renderer behavior銆傚畠鍙槸鍚庣画妯″潡鍙互渚濊禆鐨勭獎姒傚康濂戠害灞傘€?
## 楠岃瘉

```bash
./gradlew :kernel:component-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:component-model:test
```

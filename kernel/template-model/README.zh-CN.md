# `:kernel:template-model`

[English](README.md) | 绠€浣撲腑鏂?
`:kernel:template-model` 妯″潡瀹氫箟 Athena 鍦?M16 鐨?reusable component-template 涓?connection-template payload contract銆?
杩欎釜妯″潡鎶婃墍鏈夋潈閾捐矾鍥哄畾涓猴細

`semantic macro contract -> template payloads -> runtime reuse services -> M8 mutation authority -> Engineering IR`

## 鑱岃矗

- 閫氳繃 `ComponentTemplateId` 鍙戝竷 reusable component payload identity銆?- 閫氳繃 `ConnectionTemplateId` 鍙戝竷 reusable connection payload identity銆?- 閫氳繃浠ヤ笅绫诲瀷鍙戝竷鏀寔鍙傛暟寮曠敤鐨?template property value锛?  - `TemplateValue.Literal`
  - `TemplateValue.ParameterReference`
- 閫氳繃 `ComponentTemplate` 鍙戝竷 semantic component-template payload銆?- 閫氳繃 `ConnectionTemplate` 鍙戝竷 semantic connection-template payload銆?- 閫氳繃浠ヤ笅绫诲瀷鍙戝竷 template-scoped default metadata 涓庡彲閫?advisory hint锛?  - `TemplateDefaultMetadata`
  - `TemplatePresentationHint`
  - `TemplateDocumentationHint`
- 淇濇寔 templates 鏄?semantic-first锛屽苟涓斾笉鎶?SVG銆乵anual layout 鎴?renderer state 鍙樻垚 engineering truth銆?
## 涓昏绫诲瀷

- `ComponentTemplateId`
- `ComponentTemplatePropertyName`
- `TemplateValue`
- `ComponentTemplate`
- `ConnectionTemplateId`
- `TemplatePortReference`
- `ConnectionTemplate`
- `TemplateDefaultMetadata`
- `TemplatePresentationHint`
- `TemplateDocumentationHint`

## 渚濊禆

杩欎釜妯″潡渚濊禆锛?
- `:kernel:component-model`锛岄€氳繃 `EngineeringConceptId` 澶嶇敤 vendor-neutral concept identity
- `:kernel:part-model`锛岄€氳繃 `PartImplementationId` 鏀寔鍙€?implementation mapping
- `:kernel:connection-model`锛岄€氳繃 `SemanticPortRoleId` 澶嶇敤 semantic endpoint role
- `:kernel:reuse-model`锛屽鐢?macro parameter name 涓?literal parameter value

## 杈圭晫

杩欎釜妯″潡涓嶅畾涔?package resolution銆乺untime expansion銆乸arameter validation execution銆乸review orchestration銆乤cceptance handoff銆乀heia widget銆丩SP handler銆丼VG geometry 鎴?manual layout truth銆?
瀹冨彧鏄竴涓獎鑼冨洿鐨?payload 灞傦紝璁╁悗缁?runtime service 鍙互缁勫悎鍙楁不鐞嗙殑 reusable assembly锛岃€屼笉鐢ㄥ湪 frontend 鎴?renderer 浠ｇ爜閲岄噸鏂板畾涔?semantic authority銆?
## 楠岃瘉

```bash
./gradlew :kernel:template-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:template-model:test
```

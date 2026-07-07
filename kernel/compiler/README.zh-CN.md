# `:kernel:compiler`

[English](README.md) | 绠€浣撲腑鏂?
`:kernel:compiler` 妯″潡鏄?Athena 鐨勭紪璇戠紪鎺掓牳蹇冦€傚畠鍏紑缂栬瘧鍣ㄩ棬闈紝鎷ユ湁缂栬瘧娴佹按绾挎姤鍛婏紝鍗忚皟棰嗗煙璇箟锛岃В鏋愬彈娌荤悊鐨勭煡璇嗗寘锛屾牎楠屽閮ㄨ竟鐣屾弿杩扮锛屾帹瀵兼樉寮忕殑 `Layout IR` 涓?`Geometry IR`锛屽苟椹卞姩绗竴鏉″熀浜?geometry 鐨勪笅娓稿悗绔矾寰勩€?
## 鑱岃矗

- 鍏紑 `AthenaCompiler` 鐨?`parse`銆乣lower`銆乣compile` 鍏ュ彛銆?- 淇濇寔澹版槑寮?pass 椤哄簭绋冲畾锛歚PARSE -> LOWER -> SEMANTIC_ENRICHMENT -> VALIDATE -> BACKEND_PREPARATION -> BACKEND_EMISSION`銆?- 灏嗚娉曞眰婧愭枃妗?lowering 涓鸿鑼冪殑 `Engineering IR`銆?- 杩愯閫氱敤璇箟鏍￠獙涓庨鍩熸彃浠舵牎楠屻€?- 浠庤鑼?`Engineering IR` 涓庣被鍨嬪寲 `ViewDefinition` 璐＄尞鎺ㄥ鍙楁敮鎸佺殑 `Layout IR`銆?- 浠庢樉寮?`Layout IR` 鎺ㄥ鍙楁敮鎸佺殑 `Geometry IR`銆?- 娑堣垂 `:kernel:plugins:plugin-api` 鎻愪緵鐨勭ǔ瀹氬叕鍏?SPI銆?- 娑堣垂鐢?`:kernel:plugins:plugin-host` 娌荤悊鐨勫凡鎵瑰噯鎻掍欢娓呭崟銆?- 鍔犺浇骞惰В鏋愬彈娌荤悊鐭ヨ瘑鍖呫€?- 鍔犺浇骞舵牎楠屽閮ㄨ竟鐣屾弿杩扮銆?- 浠庨€夊畾鐨?`Geometry IR` 娲剧敓杩愯鏃?viewer 妯″瀷銆?- 鐩存帴鎶婇€夊畾鐨?`Geometry IR` 閫佸叆 SVG 鍚庣銆?
## 涓昏鍖哄煙

- `AthenaCompiler`锛氶棬闈笌娴佹按绾跨紪鎺掋€?- `LayoutIrDeriver`锛氱‘瀹氭€х殑 `Engineering IR -> Layout IR` 鎺ㄥ銆?- `GeometryIrDeriver`锛氱‘瀹氭€х殑 `Layout IR -> Geometry IR` 鎺ㄥ銆?- `CompilerModels.kt`锛氬叕寮€缂栬瘧缁撴灉妯″瀷銆?- `EngineeringIrLowerer`锛氳娉曞埌 IR 鐨?lowering銆?- `plugin/*`锛氫粎淇濈暀缂栬瘧鍣ㄦ嫢鏈夌殑棰嗗煙鍗忚皟閫昏緫銆?- `knowledge/*`锛氬彈娌荤悊鐭ヨ瘑鍖呮ā鍨嬨€佸姞杞戒笌瑙ｆ瀽銆?- `boundary/*`锛氬閮ㄨ竟鐣屾弿杩扮妯″瀷銆佸姞杞戒笌瑙ｆ瀽銆?
## 澧為噺鍒锋柊杈圭晫

Story `2.3` 涓?M2 寮曞叆浜嗙涓€鏉＄獎鑼冨洿澧為噺閲嶇畻璇佹槑锛?
- 鑼冨洿浠呴檺浜?runtime 鎷ユ湁鐨?`connect ports` 鍙樻洿璺緞銆?- validation銆乴ayout銆乬eometry銆乨ownstream rendering 閮戒細鎶ュ憡鏄惁淇濇寔 scoped reuse锛岃繕鏄洖閫€涓?full fallback銆?- `LayoutIrDeriver` 涓?`GeometryIrDeriver` 鍙互鍦ㄥ埛鏂板悗鐨勬枃妗ｇ粨鏋勪粛绋冲畾鏃跺鐢ㄦ湭鍙樺寲鐨勬姇褰卞璞°€?- 缂栬瘧鍣ㄥ繀椤讳繚鎸佽瘹瀹烇細濡傛灉娌℃湁瀹夊叏鐨?scoped merge锛宲ass 浼氭姤鍛?`FULL_FALLBACK`锛岃€屼笉鏄吉瑁呮垚澧為噺鎴愬姛銆?- 瑙勮寖璇箟鐪熺浉鏉ユ簮涓嶄細鏀瑰彉銆俁untime 鍙樻洿鐨勬槸 `Engineering IR`锛涚紪璇戝櫒浠庤瑙勮寖鐘舵€侀噸鏂拌绠椾笅娓稿伐浠躲€?
## 渚濊禆

- `:kernel:language`
- `:kernel:plugins:plugin-api`
- `:kernel:plugins:plugin-host`
- `:kernel:validation`
- `:kernel:engineering-model`
- `:kernel:layout-model`
- `:kernel:geometry-model`
- `:kernel:svg-renderer`

浠呮祴璇曚緷璧栵細

- `:extensions:domain-electrical`

## 杈圭晫

璇ユā鍧椾笉鎷ユ湁 DSL 璇硶鏈韩銆佷笉鎷ユ湁瑙勮寖 IR 缁撴瀯銆佷笉鎷ユ湁鍏叡鎻掍欢 SPI銆佷笉鎷ユ湁瀹夸富鎻掍欢 source 鎴?approval 娌荤悊锛屼篃涓嶆嫢鏈夊叿浣撶殑 Electrical/Runtime 棰嗗煙瑙勫垯銆傚畠璐熻矗缂栨帓杩欎簺閮ㄥ垎锛屽悓鏃朵繚鎸佹灦鏋勮鍒欎笉鍙橈細DSL 鏄綔鑰呰緭鍏ユ簮锛宍Engineering IR` 鏄鑼冩ā鍨嬶紝`Layout IR` 鏄涓€灞傛樉寮忎笅娓告姇褰憋紝`Geometry IR` 鏄潰鍚?renderer 鐨勪笅娓稿眰锛岃€?renderer 鏄秷璐?geometry 鐨勫悗绔紝鑰屼笉鏄涔夋嵎寰勩€?
## 楠岃瘉

```bash
./gradlew :kernel:compiler:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:compiler:test
```


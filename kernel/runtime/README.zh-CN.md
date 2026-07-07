# `:kernel:runtime`

[English](README.md) | 绠€浣撲腑鏂?
`:kernel:runtime` 妯″潡鎷ユ湁 Athena 鐨勯暱鐢熷懡鍛ㄦ湡杩愯鏃惰竟鐣屻€傚畠璐熻矗宸ヤ綔鍖虹敓鍛藉懆鏈熴€佹椿鍔ㄩ」鐩笂涓嬫枃銆佽繍琛屾椂鏈嶅姟瑙ｆ瀽銆佹姇褰变細璇濄€佸懡浠ゆ墽琛屻€佸巻鍙层€佸伐绋嬪浘鎶曞奖銆佸涓绘彃浠剁敓鍛藉懆鏈熸鏌ヤ笌鎵ц锛屼互鍙婂彲閫夌殑 AI 鎻愭瀹￠槄锛屽悓鏃朵笉浼氬彉鎴愮浜岃涔夌湡婧愩€?
## 鑱岃矗

- 閫氳繃 `AthenaRuntime` 鎵撳紑鍜屽叧闂伐浣滃尯銆?- 灏嗛」鐩縺娲诲埌鍏变韩鐨?`AthenaExecutionContext`銆?- 瑙ｆ瀽鍥俱€佸懡浠ゃ€佹彃浠朵笌娓叉煋鍗忚皟绛夎繍琛屾椂鏈嶅姟銆?- 娑堣垂 `:kernel:plugins:plugin-host` 娌荤悊鐨勫凡鎵瑰噯鎻掍欢娓呭崟銆?- 瀵瑰鏆撮湶 runtime 鍙鐨勬彃浠剁敓鍛藉懆鏈熸鏌ワ紝鍚屾椂涓嶆妸缂栨帓鎵€鏈夋潈浜ょ粰鎻掍欢銆?- 鎵樼杩愯鏃舵嫢鏈夌殑鎶曞奖浼氳瘽锛屽寘鎷彈鏀寔瑙嗗浘鍙戠幇涓庢椿鍔ㄨ鍥惧垏鎹€?- 璁╄繍琛屾椂瑙勮寖鐘舵€佸缁堜笌 `Engineering IR` 淇濇寔涓€鑷淬€?- 鎵樼鍛戒护鍘嗗彶銆佹挙閿€銆侀噸鍋氥€侀噸鏀俱€佸樊寮傛鏌ヤ互鍙婂凡鎺ュ彈 AI 鎻愭娴佺▼銆?- 鍦ㄥ彈鏀寔鐨勮涔夊彉鏇村悗锛屽澶栧彂甯冭繍琛屾椂鍙鐨勫閲忓埛鏂板厓鏁版嵁銆?
## 涓昏绫诲瀷

- `AthenaRuntime`
- `AthenaWorkspace`
- `AthenaExecutionContext`
- `AthenaServiceRegistry`
- `AthenaRuntimeProjectionSession`
- `AthenaCommandRuntimeService`
- `AthenaEngineeringGraphService`

## 渚濊禆

- `:kernel:compiler`
- `:kernel:plugins:plugin-host`
- `:kernel:engineering-model`
- `:kernel:svg-renderer`

## 杈圭晫

璇ユā鍧椾笉鐩存帴瑙ｆ瀽 DSL 婧愭枃鏈紝涓嶅畾涔夎鑼?IR 缁撴瀯锛屼篃涓嶆嫢鏈夐鍩熻涔夈€傚畠鎷ユ湁杩欎簺涓嬪眰涔嬩笂鐨勮繍琛屾椂鐢熷懡鍛ㄦ湡涓庣紪鎺掋€傛姇褰变細璇濆彧鏄缓绔嬪湪缂栬瘧鍣ㄦ淳鐢熸姇褰卞伐浠朵箣涓婄殑杩愯鏃剁姸鎬侊紱鍒囨崲瑙嗗浘涓嶄細淇敼瑙勮寖宸ョ▼璇箟銆?
## 楠岃瘉

```bash
./gradlew :kernel:runtime:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:runtime:test
```


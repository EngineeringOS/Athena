# `ide/theia-product`

[English](README.md) | 简体中文

`ide/theia-product` 是当前已经投入使用的 Athena Theia 产品组合包。

## 职责

- 产品级组合
- curated capability set 策略
- 桌面启动脚本与 Theia application 配置
- 产品身份与 branding 入口
- 后续桌面与浏览器打包挂点

## 边界

这个包负责 shell 组合，而不是 frontend 小部件、backend 编排细节或直接 `kernel` 调用。它应该保持精简，并始终站在产品边界一侧。

## 命令

```powershell
Set-Location ide
yarn workspace @engineeringood/athena-theia-product build
yarn workspace @engineeringood/athena-theia-product start:smoke
yarn workspace @engineeringood/athena-theia-product start
```

## 启动说明

- 在 Windows 上，Athena Electron wrapper 会在产品壳启动前自动解析 Java 25。
- 使用 `start:smoke` 可以做确定性的桌面启动验证，确认窗口已经真正完成加载。
- 在 smoke 通过后，再使用 `start` 打开交互式窗口。

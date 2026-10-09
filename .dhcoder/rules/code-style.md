# AndroidBase 编码风格

> 作为规则源入口。与 `android-native-dev` skill 表述冲突时，以 skill 为准；本文件保留不应丢失的静态规则。

## Kotlin 风格

- 优先 `val` 而非 `var`；优先不可变数据。
- 不使用通配符导入（wildcard imports）。
- 避免魔法数字，除 `-1 / 0 / 1 / 2` 外提取为常量。
- 优先 `require` / `check` 做前置校验，而非手写 `throw`。
- 重复出现的字符串字面量提取为常量。

## 控制流

- 单函数 return 语句尽量 ≤ 2；throw 语句 ≤ 2。
- 不吞异常、不留空 `catch`；不 catch 泛型 `Exception` / `Throwable`（网络层统一封装除外）。
- 不抛出泛型 `Exception` / `Error` / `RuntimeException`。

## 协程

- **禁止 `GlobalScope`**；跨 UI 生命周期的工作用 `viewModelScope` 或注入的 `CoroutineScope`。
- 返回 `Flow<T>` 的函数不要同时标 `suspend`。
- 用 `delay()` 而非 `Thread.sleep()`。

## 复杂度与体量

- 函数建议 < 80 行；类 < 1500 行。
- 函数参数 ≤ 6，构造参数 ≤ 7。
- 嵌套层级 ≤ 6；复杂条件分支避免超过 6 个。

## 格式

- 单行最长 120 字符。
- 无空函数体、无空控制流块。
- 移除未使用的私有成员与参数。

## 本项目硬约束

- 序列化统一 **kotlinx.serialization**（`@Serializable` / `@SerialName`），**禁止** Gson/Moshi 注解。
- 日志统一 **Timber**，**禁止** `android.util.Log`。
- 依赖版本只在 `gradle/libs.versions.toml` 维护，**禁止**裸版本号。
- 注解处理统一 **KSP**，不使用 kapt。

> 当前仓库尚未接入 Detekt/Ktlint；以上为约定，新增工具时按其默认规则集校验。

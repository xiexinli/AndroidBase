# FlutterDemo

AndroidBase 仓库内的**独立 Flutter 示例工程**，与 Gradle 构建完全解耦：
`settings.gradle.kts` 没有包含它，`./gradlew` 的任何任务都不会碰它，
需要单独用 `flutter` 命令运行。

## 定位

对齐 `:app` 的主页结构与 `:demo` 的 Compose 示例目录做法，提供一套
Flutter 版的「控件与用法示例」：

- 底部三个 Tab：**Home / IM / Account**
- **Home** 是 Flutter 控件与用法示例的入口：一级页是分类目录，
  点进去是承载单项示例的二级页
- **IM** 是会话列表 → 聊天页的界面示例（本地假数据，不依赖后端）
- **Account** 是设置项示例，可切换明暗模式

## 运行

```bash
cd flutterdemo
flutter pub get
flutter run                 # 需要已连接的设备或模拟器
flutter analyze             # 静态检查
flutter test                # 单元测试
```

> Android 端联网需要 `android.permission.INTERNET`，本工程的
> `android/app/src/main/AndroidManifest.xml` 已声明。

## 目录结构

```text
flutterdemo/
├── pubspec.yaml
├── analysis_options.yaml
└── lib/
    ├── main.dart                       # 入口
    ├── app/                            # 应用外壳
    │   ├── app_settings.dart           # 全局设置（主题模式 ValueNotifier）
    │   ├── app_theme.dart              # 明暗两套主题
    │   ├── flutter_demo_app.dart       # MaterialApp 装配
    │   └── main_shell.dart             # 底部三 Tab（IndexedStack）
    ├── shared/widgets/
    │   └── example_block.dart          # ExampleBlock / ExampleLabel / ResultText / ContrastNote
    └── features/
        ├── home/                       # 示例入口
        │   ├── home_page.dart          # 一级页：分类目录
        │   └── catalog/
        │       ├── example_category.dart        # 分类模型 + 13 类清单
        │       ├── example_category_page.dart   # 二级页：脚手架
        │       └── ../examples/                 # 各分类的示例实现
        ├── im/im_page.dart             # 会话列表 + 聊天页
        └── account/account_page.dart   # 设置页
```

## 示例分类（Home 页目录）

| # | 分类 | 覆盖内容 |
|---|---|---|
| 1 | 基础组件 Basics | Text / RichText / Icon / Button 家族 / Card / Chip / Divider / LayoutBuilder |
| 2 | 布局 Layout | Row / Column / Expanded / Stack / Positioned / Container / Wrap / AspectRatio / Table |
| 3 | 输入与表单 Input | TextField / Form 校验 / Checkbox / Switch / SegmentedButton / DropdownMenu / Slider |
| 4 | 列表与网格 Lists | ListView / ListView.builder / separated / GridView / Dismissible / RefreshIndicator / Sliver |
| 5 | 状态管理 State | setState / ValueNotifier / ChangeNotifier / InheritedWidget / 状态提升 |
| 6 | 交互与手势 Interaction | GestureDetector / InkWell / Listener / Draggable / AbsorbPointer |
| 7 | 动画 Animation | AnimatedContainer / AnimatedSwitcher / TweenAnimationBuilder / AnimationController / Hero |
| 8 | 弹窗与浮层 Overlay | Dialog / BottomSheet / SnackBar / Tooltip / PopupMenu / 日期时间选择器 / OverlayEntry |
| 9 | 主题与样式 Theme | Theme.of / 局部覆盖 / ColorScheme.fromSeed / copyWith / 明暗切换 |
| 10 | 路由与导航 Navigation | push-pop 传值 / RouteSettings / pushReplacement / popUntil / PopScope / 自定义转场 |
| 11 | 网络请求 Network | GET / POST / JSON 解析 / 三态 UI / 超时与异常 / compute |
| 12 | 异步与流 Async | Future / await / FutureBuilder / Stream / StreamBuilder / StreamController / async* / Future.wait |
| 13 | 易错点 Pitfalls | dispose / BuildContext 跨 await / const / Controller 释放 / 列表 key |

## 与 Android 工程的关系

- 不被 `settings.gradle.kts` 包含，**不参与** Gradle 构建，改动不影响 `:base` / `:app` / `:demo`。
- 仅作为仓库内的示例产物存在，可独立打包运行。

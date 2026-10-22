import 'package:flutter/material.dart';

import '../examples/animation_example.dart';
import '../examples/async_example.dart';
import '../examples/basics_example.dart';
import '../examples/input_example.dart';
import '../examples/interaction_example.dart';
import '../examples/layout_example.dart';
import '../examples/list_example.dart';
import '../examples/navigation_example.dart';
import '../examples/network_example.dart';
import '../examples/overlay_example.dart';
import '../examples/pitfall_example.dart';
import '../examples/state_example.dart';
import '../examples/theme_example.dart';

/// 一类 Flutter 用法示例，对应 Home 页目录里的一条二级页面。
@immutable
class ExampleCategory {
  const ExampleCategory({
    required this.id,
    required this.title,
    required this.summary,
    required this.icon,
    required this.builder,
  });

  /// 唯一标识，同时用于二级页面的路由名。
  final String id;

  /// 目录页与二级页标题栏使用的名称。
  final String title;

  /// 一句话说明这一类解决什么问题。
  final String summary;

  /// 目录页左侧图标。
  final IconData icon;

  /// 二级页面的内容构造器。
  final Widget Function() builder;
}

/// 目录清单：Home 页展示它，二级页面按 [ExampleCategory.id] 查找。
///
/// 顺序即首页的展示顺序：从基础控件 → 布局 → 输入 → 列表 → 状态 → 交互
/// → 动画 → 浮层 → 主题 → 路由 → 网络 → 异步 → 踩坑。
final List<ExampleCategory> exampleCategories = <ExampleCategory>[
  ExampleCategory(
    id: 'basics',
    title: '基础组件 Basics',
    summary: 'Text / RichText / Icon / Button 家族 / Card / Chip / Divider',
    icon: Icons.widgets,
    builder: () => const BasicsExample(),
  ),
  ExampleCategory(
    id: 'layout',
    title: '布局 Layout',
    summary: 'Row / Column / Expanded / Stack / Wrap / Container / Table',
    icon: Icons.dashboard,
    builder: () => const LayoutExample(),
  ),
  ExampleCategory(
    id: 'input',
    title: '输入与表单 Input',
    summary: 'TextField / Form 校验 / Checkbox / Switch / Slider / SegmentedButton',
    icon: Icons.edit,
    builder: () => const InputExample(),
  ),
  ExampleCategory(
    id: 'list',
    title: '列表与网格 Lists',
    summary: 'ListView / GridView / Dismissible / RefreshIndicator / Sliver',
    icon: Icons.list,
    builder: () => const ListExample(),
  ),
  ExampleCategory(
    id: 'state',
    title: '状态管理 State',
    summary: 'setState / ValueNotifier / ChangeNotifier / InheritedWidget',
    icon: Icons.memory,
    builder: () => const StateExample(),
  ),
  ExampleCategory(
    id: 'interaction',
    title: '交互与手势 Interaction',
    summary: 'GestureDetector / InkWell / Listener / Draggable / 指针控制',
    icon: Icons.touch_app,
    builder: () => const InteractionExample(),
  ),
  ExampleCategory(
    id: 'animation',
    title: '动画 Animation',
    summary: 'AnimatedXxx 隐式动画 / AnimationController / Hero / 转场',
    icon: Icons.movie,
    builder: () => const AnimationExample(),
  ),
  ExampleCategory(
    id: 'overlay',
    title: '弹窗与浮层 Overlay',
    summary: 'Dialog / BottomSheet / SnackBar / Tooltip / PopupMenu / OverlayEntry',
    icon: Icons.layers,
    builder: () => const OverlayExample(),
  ),
  ExampleCategory(
    id: 'theme',
    title: '主题与样式 Theme',
    summary: 'Theme.of / ColorScheme 种子色 / 局部覆盖 / 明暗模式切换',
    icon: Icons.palette,
    builder: () => const ThemeExample(),
  ),
  ExampleCategory(
    id: 'navigation',
    title: '路由与导航 Navigation',
    summary: 'push / pop 传值 / 命名路由 / PopScope / 自定义转场',
    icon: Icons.navigation,
    builder: () => const NavigationExample(),
  ),
  ExampleCategory(
    id: 'network',
    title: '网络请求 Network',
    summary: 'http GET / POST / JSON 解析 / 加载与错误态 / 超时重试',
    icon: Icons.cloud,
    builder: () => const NetworkExample(),
  ),
  ExampleCategory(
    id: 'async',
    title: '异步与流 Async',
    summary: 'Future / Stream / StreamController / async* / Future.wait / Isolate',
    icon: Icons.sync,
    builder: () => const AsyncExample(),
  ),
  ExampleCategory(
    id: 'pitfall',
    title: '易错点 Pitfalls',
    summary: 'dispose / BuildContext 跨 await / const / Controller 释放',
    icon: Icons.warning,
    builder: () => const PitfallExample(),
  ),
];

import 'package:flutter/material.dart';

/// 全局的轻量设置。
///
/// 只用 `ValueNotifier` 暴露，方便「主题与样式」示例直接读写并驱动重建，
/// 不引入额外的状态管理依赖。
class AppSettings {
  const AppSettings._();

  /// 当前主题模式：跟随系统 / 强制浅色 / 强制深色。
  static final ValueNotifier<ThemeMode> themeMode =
      ValueNotifier<ThemeMode>(ThemeMode.system);
}

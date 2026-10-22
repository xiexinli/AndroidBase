import 'package:flutter/material.dart';

/// 应用主题：由 Material 3 的种子色推导出明暗两套配色。
class AppTheme {
  const AppTheme._();

  /// 种子色，Material 3 会据此生成完整的 [ColorScheme]。
  static const Color seedColor = Color(0xFF3F6AE0);

  static ThemeData light() => _build(Brightness.light);

  static ThemeData dark() => _build(Brightness.dark);

  static ThemeData _build(Brightness brightness) {
    return ThemeData(
      colorScheme: ColorScheme.fromSeed(
        seedColor: seedColor,
        brightness: brightness,
      ),
    );
  }
}

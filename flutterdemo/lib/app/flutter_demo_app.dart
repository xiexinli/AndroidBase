import 'package:flutter/material.dart';

import 'app_settings.dart';
import 'app_theme.dart';
import 'main_shell.dart';

/// 应用根组件：装配主题与首页外壳。
///
/// 主题模式来自 [AppSettings.themeMode]，用 [ValueListenableBuilder] 监听，
/// 这样任意示例页面都能直接切换明暗模式。
class FlutterDemoApp extends StatelessWidget {
  const FlutterDemoApp({super.key});

  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder<ThemeMode>(
      valueListenable: AppSettings.themeMode,
      builder: (BuildContext context, ThemeMode mode, Widget? _) {
        return MaterialApp(
          title: 'Flutter Demo',
          debugShowCheckedModeBanner: false,
          theme: AppTheme.light(),
          darkTheme: AppTheme.dark(),
          themeMode: mode,
          home: const MainShell(),
        );
      },
    );
  }
}

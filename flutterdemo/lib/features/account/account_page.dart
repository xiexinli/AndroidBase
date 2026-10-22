import 'package:flutter/material.dart';

import '../../app/app_settings.dart';
import '../../app/app_theme.dart';

/// Account 页：设置项示例（主题模式、关于信息等）。
class AccountPage extends StatelessWidget {
  const AccountPage({super.key});

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(title: const Text('Account')),
      body: ListView(
        children: <Widget>[
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: <Widget>[
                CircleAvatar(
                  radius: 28,
                  backgroundColor: scheme.primaryContainer,
                  foregroundColor: scheme.onPrimaryContainer,
                  child: const Icon(Icons.person, size: 28),
                ),
                const SizedBox(width: 16),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    Text(
                      'Demo 用户',
                      style: Theme.of(context).textTheme.titleMedium,
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'flutterdemo@example.com',
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(
                            color: scheme.onSurfaceVariant,
                          ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          const Divider(height: 1),
          const _SectionTitle('外观'),
          ValueListenableBuilder<ThemeMode>(
            valueListenable: AppSettings.themeMode,
            builder: (BuildContext context, ThemeMode mode, Widget? _) {
              return Column(
                children: <Widget>[
                  for (final _ThemeModeOption option in _themeModeOptions)
                    ListTile(
                      leading: Icon(option.icon),
                      title: Text(option.label),
                      trailing: mode == option.mode
                          ? Icon(Icons.check, color: scheme.primary)
                          : null,
                      onTap: () => AppSettings.themeMode.value = option.mode,
                    ),
                ],
              );
            },
          ),
          const Divider(height: 1),
          const _SectionTitle('关于'),
          ListTile(
            leading: const Icon(Icons.palette_outlined),
            title: const Text('主题种子色'),
            subtitle: const Text('#3F6AE0'),
            trailing: Container(
              width: 24,
              height: 24,
              decoration: const BoxDecoration(
                color: AppTheme.seedColor,
                shape: BoxShape.circle,
              ),
            ),
          ),
          ListTile(
            leading: const Icon(Icons.info_outline),
            title: const Text('版本'),
            subtitle: const Text('1.0.0+1'),
            onTap: () => _showAbout(context),
          ),
          ListTile(
            leading: const Icon(Icons.description_outlined),
            title: const Text('本工程说明'),
            subtitle: const Text('Flutter 控件与用法示例，Home 页为示例入口'),
            onTap: () => _showAbout(context),
          ),
        ],
      ),
    );
  }

  static void _showAbout(BuildContext context) {
    showDialog<void>(
      context: context,
      builder: (BuildContext context) => AlertDialog(
        title: const Text('FlutterDemo'),
        content: const Text(
          'AndroidBase 仓库内的独立 Flutter 示例工程，\n'
          '与 Gradle 构建互不影响，用 flutter run 单独运行。\n\n'
          'Home 页是 Flutter 控件与用法示例的入口。',
        ),
        actions: <Widget>[
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('知道了'),
          ),
        ],
      ),
    );
  }
}

/// 主题模式选项：Account 页与「主题与样式」示例共用同一份定义。
@immutable
class _ThemeModeOption {
  const _ThemeModeOption(this.label, this.icon, this.mode);

  final String label;
  final IconData icon;
  final ThemeMode mode;
}

const List<_ThemeModeOption> _themeModeOptions = <_ThemeModeOption>[
  _ThemeModeOption('跟随系统', Icons.brightness_auto, ThemeMode.system),
  _ThemeModeOption('浅色', Icons.light_mode, ThemeMode.light),
  _ThemeModeOption('深色', Icons.dark_mode, ThemeMode.dark),
];

class _SectionTitle extends StatelessWidget {
  const _SectionTitle(this.text);

  final String text;

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 4),
      child: Text(
        text,
        style: Theme.of(context).textTheme.labelLarge?.copyWith(
              color: scheme.primary,
            ),
      ),
    );
  }
}

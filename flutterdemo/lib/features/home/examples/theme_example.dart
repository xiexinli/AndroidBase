import 'package:flutter/material.dart';

import '../../../app/app_settings.dart';
import '../../../app/app_theme.dart';
import '../../../shared/widgets/example_block.dart';

/// 主题与样式：Theme.of / 种子色 / 局部覆盖 / 明暗切换。
class ThemeExample extends StatelessWidget {
  const ThemeExample({super.key});

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final ColorScheme scheme = theme.colorScheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        ExampleBlock(
          title: 'Theme.of(context) 读取当前主题',
          description: '颜色一律从 colorScheme 取、文字样式一律从 textTheme 取，不要写死色值',
          result: '当前亮度：${theme.brightness == Brightness.dark ? '深色' : '浅色'}',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Text('headlineSmall：主题里定义的大标题样式', style: theme.textTheme.headlineSmall),
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: <Widget>[
                  for (final MapEntry<String, Color> entry in <String, Color>{
                    'primary': scheme.primary,
                    'secondary': scheme.secondary,
                    'tertiary': scheme.tertiary,
                    'error': scheme.error,
                    'surface': scheme.surface,
                  }.entries)
                    Column(
                      children: <Widget>[
                        Container(
                          width: 64,
                          height: 40,
                          decoration: BoxDecoration(
                            color: entry.value,
                            borderRadius: BorderRadius.circular(8),
                            border: Border.all(color: scheme.outlineVariant),
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(entry.key, style: theme.textTheme.labelSmall),
                      ],
                    ),
                ],
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: '局部 Theme 覆盖',
          description: 'Theme() 包住子树即可单独换色，不影响页面其它部分',
          child: Column(
            children: <Widget>[
              FilledButton(
                onPressed: () {},
                child: const Text('使用全局主题'),
              ),
              const SizedBox(height: 12),
              Theme(
                data: theme.copyWith(
                  colorScheme: ColorScheme.fromSeed(
                    seedColor: Colors.deepOrange,
                    brightness: theme.brightness,
                  ),
                ),
                child: Builder(
                  builder: (BuildContext context) => Column(
                    children: <Widget>[
                      FilledButton(
                        onPressed: () {},
                        child: const Text('橙色局部主题'),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        '这段文字也在局部主题内',
                        style: Theme.of(context).textTheme.bodySmall,
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'ColorScheme.fromSeed 种子色',
          description: 'Material 3 只给一个种子色，其余配色由算法推导；换种子色即换整体色调',
          child: Row(
            children: <Widget>[
              for (final Color seed in <Color>[
                AppTheme.seedColor,
                Colors.teal,
                Colors.purple,
                Colors.amber,
              ])
                Padding(
                  padding: const EdgeInsets.only(right: 12),
                  child: Column(
                    children: <Widget>[
                      CircleAvatar(backgroundColor: seed, radius: 16),
                      const SizedBox(height: 4),
                      Container(
                        width: 24,
                        height: 24,
                        decoration: BoxDecoration(
                          color: ColorScheme.fromSeed(seedColor: seed)
                              .primaryContainer,
                          shape: BoxShape.circle,
                        ),
                      ),
                    ],
                  ),
                ),
            ],
          ),
        ),
        ExampleBlock(
          title: '自定义文字样式 copyWith',
          description: '在 textTheme 的基础上 copyWith 微调，避免定义整套 TextStyle',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Text(
                '正常标题',
                style: theme.textTheme.titleLarge,
              ),
              const SizedBox(height: 8),
              Text(
                'copyWith 后的标题（加粗 + 主色 + 行高）',
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.w700,
                  color: scheme.primary,
                  height: 1.6,
                  letterSpacing: 0.5,
                ),
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: '明暗模式切换',
          description:
              'MaterialApp 的 themeMode 由全局 ValueNotifier 驱动，任意位置都能切',
          child: ValueListenableBuilder<ThemeMode>(
            valueListenable: AppSettings.themeMode,
            builder: (BuildContext context, ThemeMode mode, Widget? _) {
              return SegmentedButton<ThemeMode>(
                segments: const <ButtonSegment<ThemeMode>>[
                  ButtonSegment<ThemeMode>(
                    value: ThemeMode.system,
                    label: Text('跟随系统'),
                    icon: Icon(Icons.brightness_auto),
                  ),
                  ButtonSegment<ThemeMode>(
                    value: ThemeMode.light,
                    label: Text('浅色'),
                    icon: Icon(Icons.light_mode),
                  ),
                  ButtonSegment<ThemeMode>(
                    value: ThemeMode.dark,
                    label: Text('深色'),
                    icon: Icon(Icons.dark_mode),
                  ),
                ],
                selected: <ThemeMode>{mode},
                onSelectionChanged: (Set<ThemeMode> selection) =>
                    AppSettings.themeMode.value = selection.first,
              );
            },
          ),
        ),
      ],
    );
  }
}

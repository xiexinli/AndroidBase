import 'package:flutter/material.dart';

import '../../../shared/widgets/example_block.dart';

/// 基础组件：文本、图标、按钮家族、卡片、标签与分割线。
class BasicsExample extends StatefulWidget {
  const BasicsExample({super.key});

  @override
  State<BasicsExample> createState() => _BasicsExampleState();
}

class _BasicsExampleState extends State<BasicsExample> {
  int _taps = 0;

  @override
  Widget build(BuildContext context) {
    final TextTheme text = Theme.of(context).textTheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        ExampleBlock(
          title: 'Text 文本',
          description: 'style / maxLines / overflow / TextAlign / Text.rich 富文本',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Text('bodyMedium 默认正文', style: text.bodyMedium),
              const SizedBox(height: 6),
              Text('headlineSmall 大标题', style: text.headlineSmall),
              const SizedBox(height: 6),
              const Text(
                'maxLines: 1 + TextOverflow.ellipsis —— Flutter 是 Google 推出的跨平台 UI 框架，'
                '一套 Dart 代码可运行在 Android、iOS、Web 与桌面端。',
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
              const SizedBox(height: 6),
              Text.rich(
                const TextSpan(
                  text: '富文本：',
                  children: <InlineSpan>[
                    TextSpan(
                      text: '粗体',
                      style: TextStyle(fontWeight: FontWeight.bold),
                    ),
                    TextSpan(text: ' / '),
                    TextSpan(
                      text: '斜体',
                      style: TextStyle(fontStyle: FontStyle.italic),
                    ),
                    TextSpan(text: ' / '),
                    TextSpan(
                      text: '彩色',
                      style: TextStyle(color: Colors.deepOrange),
                    ),
                    TextSpan(text: ' / '),
                    TextSpan(
                      text: '等宽字体',
                      style: TextStyle(fontFamily: 'monospace'),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'Icon 图标',
          description: '内置 Icons；直接用 color/size 定制，或用 IconTheme 批量着色',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              const Row(
                children: <Widget>[
                  Icon(Icons.home),
                  SizedBox(width: 12),
                  Icon(Icons.star, color: Colors.amber, size: 32),
                  SizedBox(width: 12),
                  Icon(Icons.favorite, color: Colors.pink),
                ],
              ),
              const SizedBox(height: 12),
              IconTheme(
                data: const IconThemeData(color: Colors.teal, size: 28),
                child: Row(
                  children: const <Widget>[
                    Icon(Icons.wifi),
                    SizedBox(width: 12),
                    Icon(Icons.bluetooth),
                    SizedBox(width: 12),
                    Icon(Icons.battery_full),
                  ],
                ),
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'Button 按钮家族',
          description:
              'FilledButton / ElevatedButton / OutlinedButton / TextButton / IconButton；onPressed 传 null 即禁用',
          result: '累计点击 $_taps 次',
          child: Wrap(
            spacing: 12,
            runSpacing: 8,
            crossAxisAlignment: WrapCrossAlignment.center,
            children: <Widget>[
              FilledButton(
                onPressed: () => setState(() => _taps++),
                child: const Text('Filled'),
              ),
              ElevatedButton(
                onPressed: () => setState(() => _taps++),
                child: const Text('Elevated'),
              ),
              OutlinedButton(
                onPressed: () => setState(() => _taps++),
                child: const Text('Outlined'),
              ),
              TextButton(
                onPressed: () => setState(() => _taps++),
                child: const Text('Text'),
              ),
              const FilledButton(onPressed: null, child: Text('禁用态')),
              IconButton(
                tooltip: '点赞',
                onPressed: () {
                  setState(() => _taps++);
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      content: Text('IconButton 被点击'),
                      duration: Duration(seconds: 1),
                    ),
                  );
                },
                icon: const Icon(Icons.thumb_up),
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'Card / Chip / Divider',
          description: 'Card 卡片容器、Chip 标签、InputChip 可交互标签（带删除图标）',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Card(
                child: ListTile(
                  leading: const Icon(Icons.image),
                  title: const Text('Card + ListTile'),
                  subtitle: const Text('卡片内放列表项是最常见的组合'),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () {},
                ),
              ),
              const SizedBox(height: 12),
              const Wrap(
                spacing: 8,
                children: <Widget>[
                  Chip(label: Text('普通标签')),
                  Chip(
                    label: Text('带图标'),
                    avatar: Icon(Icons.tag, size: 18),
                  ),
                ],
              ),
              const SizedBox(height: 4),
              const Divider(),
              Text(
                'Divider 用于分隔内容区块，indent / endIndent 控制左右缩进',
                style: text.bodySmall,
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'LayoutBuilder / Builder',
          description: 'LayoutBuilder 拿到父级约束后再决定布局，是自适应布局的常用手段',
          child: LayoutBuilder(
            builder: (BuildContext context, BoxConstraints constraints) {
              final bool wide = constraints.maxWidth > 320;
              return Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  border: Border.all(
                    color: Theme.of(context).colorScheme.outlineVariant,
                  ),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  '当前可用宽度：${constraints.maxWidth.toStringAsFixed(1)}\n'
                  '→ ${wide ? '宽布局（并排）' : '窄布局（堆叠）'}',
                  style: const TextStyle(fontFamily: 'monospace', height: 1.5),
                ),
              );
            },
          ),
        ),
      ],
    );
  }
}

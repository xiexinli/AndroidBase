import 'package:flutter/material.dart';

import '../../../shared/widgets/example_block.dart';

/// 布局：Row / Column / Expanded / Stack / Wrap / Container / Table。
class LayoutExample extends StatelessWidget {
  const LayoutExample({super.key});

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        ExampleBlock(
          title: 'Row / Column / Expanded / Flexible / Spacer',
          description:
              'mainAxisAlignment 主轴对齐，crossAxisAlignment 交叉轴对齐；Expanded 按 flex 瓜分剩余空间',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              _box(scheme, 'Row：主轴 spaceBetween'),
              const SizedBox(height: 8),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: <Widget>[
                  _cell(scheme, 'A'),
                  _cell(scheme, 'B'),
                  _cell(scheme, 'C'),
                ],
              ),
              const SizedBox(height: 16),
              _box(scheme, 'Expanded flex 1 : 2 : 1'),
              const SizedBox(height: 8),
              Row(
                children: <Widget>[
                  Expanded(flex: 1, child: _cell(scheme, '1')),
                  const SizedBox(width: 8),
                  Expanded(flex: 2, child: _cell(scheme, '2')),
                  const SizedBox(width: 8),
                  Expanded(flex: 1, child: _cell(scheme, '1')),
                ],
              ),
              const SizedBox(height: 16),
              _box(scheme, 'Spacer 把两侧内容顶开'),
              const SizedBox(height: 8),
              Row(
                children: <Widget>[
                  _cell(scheme, '左'),
                  const Spacer(),
                  _cell(scheme, '右'),
                ],
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'Stack / Positioned / Align',
          description: 'Stack 层叠布局，Positioned 精确定位，Align 做对齐；后加的子项在上层',
          child: SizedBox(
            height: 160,
            child: Stack(
              fit: StackFit.expand,
              children: <Widget>[
                Container(
                  decoration: BoxDecoration(
                    color: scheme.surfaceContainerHighest,
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
                const Align(
                  alignment: Alignment.center,
                  child: Text('Stack 底层内容'),
                ),
                Positioned(
                  top: 8,
                  right: 8,
                  child: Chip(
                    label: const Text('Positioned'),
                    backgroundColor: scheme.tertiaryContainer,
                  ),
                ),
                Positioned(
                  left: 16,
                  bottom: 16,
                  child: Container(
                    width: 48,
                    height: 48,
                    decoration: BoxDecoration(
                      color: scheme.primary,
                      shape: BoxShape.circle,
                    ),
                    child: Icon(Icons.add, color: scheme.onPrimary),
                  ),
                ),
              ],
            ),
          ),
        ),
        ExampleBlock(
          title: 'Container / Padding / DecoratedBox / SizedBox',
          description:
              'Container = Padding + DecoratedBox + Align 等组合；只改尺寸用 SizedBox 更省',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(16),
                margin: const EdgeInsets.only(bottom: 12),
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    colors: <Color>[scheme.primary, scheme.tertiary],
                  ),
                  borderRadius: BorderRadius.circular(12),
                  boxShadow: const <BoxShadow>[
                    BoxShadow(
                      color: Color(0x33000000),
                      blurRadius: 8,
                      offset: Offset(0, 4),
                    ),
                  ],
                ),
                child: Text(
                  'Container：padding + margin + 渐变 + 圆角 + 阴影',
                  style: TextStyle(color: scheme.onPrimary),
                ),
              ),
              const Padding(
                padding: EdgeInsets.symmetric(horizontal: 8, vertical: 12),
                child: Text('Padding 只负责留白，不产生背景与装饰'),
              ),
              Row(
                children: <Widget>[
                  const SizedBox(width: 40, height: 40, child: _Dot()),
                  const SizedBox(width: 12),
                  const SizedBox(width: 20, height: 20, child: _Dot()),
                  const Spacer(),
                  Text('SizedBox 只是带尺寸的空盒子', style: Theme.of(context).textTheme.bodySmall),
                ],
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'Wrap 流式布局',
          description: '一行放不下自动换行，等价于 Android 的 FlexboxLayout；runningSpace 控制行间距',
          child: Wrap(
            spacing: 8,
            runSpacing: 8,
            children: <Widget>[
              for (final String tag in <String>[
                'Flutter',
                'Dart',
                'Widget',
                'State',
                'BuildContext',
                'Material 3',
                '热重载',
                '跨平台',
              ])
                Chip(label: Text(tag)),
            ],
          ),
        ),
        ExampleBlock(
          title: 'AspectRatio / FractionallySizedBox / ConstrainedBox',
          description: '按比例约束盒子尺寸的三种常用方式',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              const AspectRatio(
                aspectRatio: 16 / 9,
                child: _Dot(label: 'AspectRatio 16:9'),
              ),
              const SizedBox(height: 12),
              Container(
                height: 60,
                color: scheme.surfaceContainerHighest,
                child: FractionallySizedBox(
                  widthFactor: 0.5,
                  child: _Dot(label: '宽 50%'),
                ),
              ),
              const SizedBox(height: 12),
              ConstrainedBox(
                constraints: const BoxConstraints(
                  minWidth: 100,
                  maxWidth: 200,
                  minHeight: 44,
                ),
                child: const _Dot(label: 'ConstrainedBox 44~200'),
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'Table 表格',
          description: '固定列宽的二维布局，列宽用 FlexColumnWidth / FixedColumnWidth 控制',
          child: Table(
            border: TableBorder.all(color: scheme.outlineVariant),
            columnWidths: const <int, TableColumnWidth>{
              0: FlexColumnWidth(2),
              1: FlexColumnWidth(3),
            },
            children: const <TableRow>[
              TableRow(
                children: <Widget>[
                  _TableCell('控件', header: true),
                  _TableCell('用途', header: true),
                ],
              ),
              TableRow(
                children: <Widget>[
                  _TableCell('Row'),
                  _TableCell('横向排列子项'),
                ],
              ),
              TableRow(
                children: <Widget>[
                  _TableCell('Column'),
                  _TableCell('纵向排列子项'),
                ],
              ),
              TableRow(
                children: <Widget>[
                  _TableCell('Stack'),
                  _TableCell('层叠排列子项'),
                ],
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _box(ColorScheme scheme, String text) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      color: scheme.surfaceContainerHighest,
      child: Text(
        text,
        style: TextStyle(
          fontFamily: 'monospace',
          fontSize: 12,
          color: scheme.onSurfaceVariant,
        ),
      ),
    );
  }

  Widget _cell(ColorScheme scheme, String text) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: scheme.primaryContainer,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(
        text,
        style: TextStyle(color: scheme.onPrimaryContainer),
      ),
    );
  }
}

class _Dot extends StatelessWidget {
  const _Dot({this.label});

  final String? label;

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;
    return Container(
      alignment: Alignment.center,
      decoration: BoxDecoration(
        color: scheme.secondaryContainer,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(
        label ?? '·',
        style: TextStyle(
          color: scheme.onSecondaryContainer,
          fontSize: 12,
        ),
      ),
    );
  }
}

class _TableCell extends StatelessWidget {
  const _TableCell(this.text, {this.header = false});

  final String text;
  final bool header;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      child: Text(
        text,
        style: TextStyle(
          fontWeight: header ? FontWeight.w600 : FontWeight.normal,
        ),
      ),
    );
  }
}

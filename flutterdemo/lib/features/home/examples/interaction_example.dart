import 'package:flutter/material.dart';

import '../../../shared/widgets/example_block.dart';

/// 交互与手势：GestureDetector / InkWell / Listener / Draggable / 指针控制。
class InteractionExample extends StatefulWidget {
  const InteractionExample({super.key});

  @override
  State<InteractionExample> createState() => _InteractionExampleState();
}

class _InteractionExampleState extends State<InteractionExample> {
  String _gestureLog = '试着点击、双击、长按或拖拽下面色块';
  Offset _dragOffset = Offset.zero;
  String _pointerLog = '在下方区域按下 / 移动 / 抬起';
  Offset _target = const Offset(120, 40);
  bool _acceptsPointer = true;

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        ExampleBlock(
          title: 'GestureDetector 手势识别',
          description:
              'onTap / onDoubleTap / onLongPress / onPanUpdate / onScaleUpdate；同时设置多个手势时要注意优先级',
          result: _gestureLog,
          child: GestureDetector(
            onTap: () => setState(() => _gestureLog = 'onTap：单击'),
            onDoubleTap: () => setState(() => _gestureLog = 'onDoubleTap：双击'),
            onLongPress: () => setState(() => _gestureLog = 'onLongPress：长按'),
            onPanUpdate: (DragUpdateDetails details) {
              setState(() {
                _dragOffset += details.delta;
                _gestureLog =
                    'onPanUpdate：dx=${_dragOffset.dx.toStringAsFixed(1)} '
                    'dy=${_dragOffset.dy.toStringAsFixed(1)}';
              });
            },
            child: Container(
              height: 120,
              decoration: BoxDecoration(
                color: scheme.primaryContainer,
                borderRadius: BorderRadius.circular(12),
              ),
              alignment: Alignment.center,
              child: const Text('手势区域（可点击 / 拖拽）'),
            ),
          ),
        ),
        ExampleBlock(
          title: 'InkWell / InkResponse 水波纹',
          description: 'Material 风格的点击反馈；需要 Material 祖先，否则看不到涟漪',
          child: Column(
            children: <Widget>[
              InkWell(
                onTap: () {},
                borderRadius: BorderRadius.circular(12),
                child: Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(16),
                  child: const Text('InkWell + borderRadius，点击有涟漪'),
                ),
              ),
              const Divider(height: 1),
              InkResponse(
                onTap: () {},
                radius: 40,
                child: const Padding(
                  padding: EdgeInsets.all(16),
                  child: Text('InkResponse：涟漪从点击位置扩散'),
                ),
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'Listener 原始指针事件',
          description:
              '不参与手势竞技场，拿到最原始的 PointerDown / Move / Up 事件，适合做按压态、手写板',
          result: _pointerLog,
          child: Listener(
            onPointerDown: (PointerDownEvent event) => setState(
              () => _pointerLog =
                  'onPointerDown: ${event.localPosition.dx.toStringAsFixed(0)}, '
                  '${event.localPosition.dy.toStringAsFixed(0)}',
            ),
            onPointerMove: (PointerMoveEvent event) => setState(
              () => _pointerLog =
                  'onPointerMove: ${event.localPosition.dx.toStringAsFixed(0)}, '
                  '${event.localPosition.dy.toStringAsFixed(0)}',
            ),
            onPointerUp: (PointerUpEvent event) =>
                setState(() => _pointerLog = 'onPointerUp'),
            child: Container(
              height: 100,
              alignment: Alignment.center,
              decoration: BoxDecoration(
                color: scheme.surfaceContainerHighest,
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Text('指针事件区域'),
            ),
          ),
        ),
        ExampleBlock(
          title: 'Draggable / DragTarget 拖拽',
          description: '把可拖拽组件放进目标区域，onAcceptWithDetails 里处理落点数据',
          child: SizedBox(
            height: 180,
            child: Stack(
              children: <Widget>[
                Positioned(
                  left: 0,
                  top: 0,
                  child: Draggable<String>(
                    data: '被拖拽的数据',
                    feedback: Material(
                      color: Colors.transparent,
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 16,
                          vertical: 10,
                        ),
                        decoration: BoxDecoration(
                          color: scheme.primary,
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Text(
                          '拖我',
                          style: TextStyle(color: scheme.onPrimary),
                        ),
                      ),
                    ),
                    childWhenDragging: const Opacity(
                      opacity: 0.3,
                      child: Text('拖我'),
                    ),
                    child: Chip(
                      label: const Text('拖我'),
                      backgroundColor: scheme.primaryContainer,
                    ),
                  ),
                ),
                Positioned(
                  left: _target.dx,
                  top: _target.dy + 40,
                  child: DragTarget<String>(
                    onAcceptWithDetails: (DragTargetDetails<String> details) {
                      setState(() {
                        _target = Offset(
                          details.offset.dx.clamp(0, 220),
                          details.offset.dy.clamp(0, 100),
                        );
                      });
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(
                          content: Text('收到：${details.data}'),
                          duration: const Duration(seconds: 1),
                        ),
                      );
                    },
                    builder: (
                      BuildContext context,
                      List<String?> candidateData,
                      List<dynamic> rejectedData,
                    ) {
                      final bool hovering = candidateData.isNotEmpty;
                      return Container(
                        width: 140,
                        height: 90,
                        alignment: Alignment.center,
                        decoration: BoxDecoration(
                          color: hovering
                              ? scheme.tertiaryContainer
                              : scheme.surfaceContainerHighest,
                          border: Border.all(
                            color: hovering ? scheme.tertiary : scheme.outline,
                          ),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: const Text('拖到这里的 DragTarget'),
                      );
                    },
                  ),
                ),
              ],
            ),
          ),
        ),
        ExampleBlock(
          title: 'AbsorbPointer / IgnorePointer',
          description:
              'AbsorbPointer 拦截事件（自己响应），IgnorePointer 直接放行给下层；常用于加载遮罩',
          result: _acceptsPointer ? '当前：可点击' : '当前：被 AbsorbPointer 拦截',
          child: Column(
            children: <Widget>[
              AbsorbPointer(
                absorbing: !_acceptsPointer,
                child: FilledButton.tonal(
                  onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      content: Text('按钮被点击'),
                      duration: Duration(seconds: 1),
                    ),
                  ),
                  child: const Text('点我试试'),
                ),
              ),
              const SizedBox(height: 8),
              SwitchListTile(
                contentPadding: EdgeInsets.zero,
                title: const Text('允许点击'),
                value: _acceptsPointer,
                onChanged: (bool value) =>
                    setState(() => _acceptsPointer = value),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

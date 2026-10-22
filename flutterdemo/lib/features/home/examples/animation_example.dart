import 'package:flutter/material.dart';

import '../../../shared/widgets/example_block.dart';

/// 动画：隐式动画（AnimatedXxx）、显式动画（AnimationController）、Hero 转场。
class AnimationExample extends StatefulWidget {
  const AnimationExample({super.key});

  @override
  State<AnimationExample> createState() => _AnimationExampleState();
}

class _AnimationExampleState extends State<AnimationExample>
    with SingleTickerProviderStateMixin {
  bool _expanded = false;
  bool _visible = true;
  int _switchIndex = 0;
  double _progressTarget = 0.2;

  late final AnimationController _controller;
  late final Animation<double> _curve;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1200),
    );
    _curve = CurvedAnimation(parent: _controller, curve: Curves.easeInOutBack);
    _controller.addStatusListener((AnimationStatus status) {
      if (status == AnimationStatus.completed) {
        // 演示「动画结束回调」。
        debugPrint('AnimationController 完成');
      }
    });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        ExampleBlock(
          title: 'AnimatedContainer 隐式动画',
          description: '只改属性值，Flutter 自动补间；duration / curve 决定过程',
          child: Column(
            children: <Widget>[
              AnimatedContainer(
                duration: const Duration(milliseconds: 400),
                curve: Curves.easeInOut,
                width: _expanded ? double.infinity : 120,
                height: _expanded ? 120 : 56,
                alignment: Alignment.center,
                decoration: BoxDecoration(
                  color: _expanded ? scheme.tertiaryContainer : scheme.primaryContainer,
                  borderRadius:
                      BorderRadius.circular(_expanded ? 24 : 8),
                ),
                child: Text(_expanded ? '展开态' : '收起态'),
              ),
              const SizedBox(height: 12),
              FilledButton.tonal(
                onPressed: () => setState(() => _expanded = !_expanded),
                child: const Text('切换尺寸 / 圆角 / 颜色'),
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'AnimatedOpacity / Align / Padding',
          description: '每个 AnimatedXxx 对应一类可补间的属性，组合起来即可做淡入淡出',
          child: Column(
            children: <Widget>[
              Container(
                height: 100,
                color: scheme.surfaceContainerHighest,
                child: AnimatedAlign(
                  duration: const Duration(milliseconds: 500),
                  curve: Curves.easeInOut,
                  alignment: _visible
                      ? Alignment.centerLeft
                      : Alignment.centerRight,
                  child: AnimatedOpacity(
                    duration: const Duration(milliseconds: 500),
                    opacity: _visible ? 1 : 0.2,
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      child: Chip(
                        label: const Text('动画目标'),
                        backgroundColor: scheme.primaryContainer,
                      ),
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 12),
              FilledButton.tonal(
                onPressed: () => setState(() => _visible = !_visible),
                child: const Text('左右移动 + 改透明度'),
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'AnimatedSwitcher 内容切换动画',
          description: '子组件 key 变化时，旧内容淡出、新内容淡入；transitionBuilder 可换转场',
          child: Column(
            children: <Widget>[
              AnimatedSwitcher(
                duration: const Duration(milliseconds: 300),
                transitionBuilder: (
                  Widget child,
                  Animation<double> animation,
                ) {
                  return ScaleTransition(
                    scale: animation,
                    child: FadeTransition(opacity: animation, child: child),
                  );
                },
                child: Container(
                  key: ValueKey<int>(_switchIndex),
                  width: 120,
                  height: 120,
                  alignment: Alignment.center,
                  decoration: BoxDecoration(
                    color: Colors.primaries[_switchIndex % Colors.primaries.length]
                        .withValues(alpha: 0.35),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Text(
                    '#$_switchIndex',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                ),
              ),
              const SizedBox(height: 12),
              FilledButton.tonal(
                onPressed: () => setState(() => _switchIndex++),
                child: const Text('切换内容'),
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'TweenAnimationBuilder 一次性补间',
          description: '不需要 AnimationController，给一个目标值就自动从旧值补间到新值',
          result: '目标进度：${(_progressTarget * 100).round()}%',
          child: Column(
            children: <Widget>[
              TweenAnimationBuilder<double>(
                tween: Tween<double>(begin: 0, end: _progressTarget),
                duration: const Duration(milliseconds: 600),
                curve: Curves.easeOutCubic,
                builder: (BuildContext context, double value, Widget? child) {
                  return Column(
                    children: <Widget>[
                      LinearProgressIndicator(value: value),
                      const SizedBox(height: 8),
                      Text('进度：${(value * 100).toStringAsFixed(0)}%'),
                    ],
                  );
                },
              ),
              const SizedBox(height: 12),
              Wrap(
                spacing: 12,
                children: <Widget>[
                  for (final double target in <double>[0.2, 0.6, 1.0])
                    OutlinedButton(
                      onPressed: () =>
                          setState(() => _progressTarget = target),
                      child: Text('${(target * 100).round()}%'),
                    ),
                ],
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'AnimationController 显式动画',
          description:
              '手动 start / reverse / repeat，配合 AnimatedBuilder 只重建需要动的部分',
          result: '状态：${_controller.isAnimating ? '播放中' : '已停止'}',
          child: Column(
            children: <Widget>[
              SizedBox(
                height: 140,
                child: AnimatedBuilder(
                  animation: _controller,
                  builder: (BuildContext context, Widget? child) {
                    return Transform.rotate(
                      angle: _curve.value * 3.14159 * 2,
                      child: Transform.scale(
                        scale: 0.6 + _curve.value * 0.6,
                        child: Container(
                          width: 100,
                          height: 100,
                          decoration: BoxDecoration(
                            color: scheme.primaryContainer,
                            borderRadius: BorderRadius.circular(20),
                          ),
                          alignment: Alignment.center,
                          child: const Text('旋转 + 缩放'),
                        ),
                      ),
                    );
                  },
                ),
              ),
              Wrap(
                spacing: 12,
                children: <Widget>[
                  FilledButton.tonal(
                    onPressed: () => _controller.forward(),
                    child: const Text('forward'),
                  ),
                  FilledButton.tonal(
                    onPressed: () => _controller.reverse(),
                    child: const Text('reverse'),
                  ),
                  FilledButton.tonal(
                    onPressed: () => _controller.repeat(reverse: true),
                    child: const Text('repeat'),
                  ),
                  FilledButton.tonal(
                    onPressed: () => _controller.stop(),
                    child: const Text('stop'),
                  ),
                ],
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'Hero 跨页面共享元素动画',
          description: '两个页面里同 tag 的 Hero 会在路由切换时自动飞过去',
          result: '点击下方方块 → push 详情页，Hero 会飞过去（再返回观察回程）',
          child: Center(
            child: Hero(
              tag: 'hero-demo',
              child: Material(
                color: Colors.transparent,
                child: Container(
                  width: 100,
                  height: 100,
                  decoration: BoxDecoration(
                    color: scheme.tertiaryContainer,
                    borderRadius: BorderRadius.circular(16),
                  ),
                  alignment: Alignment.center,
                  child: const Text('点我'),
                ),
              ),
            ),
          ),
        ),
        Center(
          child: OutlinedButton(
            onPressed: () => Navigator.of(context).push<void>(
              MaterialPageRoute<void>(
                builder: (BuildContext context) => const _HeroDetailPage(),
              ),
            ),
            child: const Text('打开 Hero 详情页'),
          ),
        ),
      ],
    );
  }
}

class _HeroDetailPage extends StatelessWidget {
  const _HeroDetailPage();

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;
    return Scaffold(
      appBar: AppBar(title: const Text('Hero 目标页')),
      body: Center(
        child: Hero(
          tag: 'hero-demo',
          child: Material(
            color: Colors.transparent,
            child: Container(
              width: 240,
              height: 240,
              decoration: BoxDecoration(
                color: scheme.tertiaryContainer,
                borderRadius: BorderRadius.circular(24),
              ),
              alignment: Alignment.center,
              child: const Text('同一个 Hero 目标页'),
            ),
          ),
        ),
      ),
    );
  }
}

import 'package:flutter/material.dart';

import '../../../shared/widgets/example_block.dart';

/// 易错点：这些是 Flutter 新手最常踩的坑，多数会在控制台报错或性能下降。
class PitfallExample extends StatefulWidget {
  const PitfallExample({super.key});

  @override
  State<PitfallExample> createState() => _PitfallExampleState();
}

class _PitfallExampleState extends State<PitfallExample> {
  final TextEditingController _controller = TextEditingController();
  String _log = '（点击按钮观察控制台与这里的结果）';

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        const ExampleBlock(
          title: '在 dispose 之后 setState',
          description: '异步回调回来时页面可能已经销毁，直接 setState 会抛异常',
          child: ContrastNote(
            bad: 'await 之后直接 setState(...)\n// setState() called after dispose()',
            good: 'await 之后先判断 mounted：\nif (!mounted) return;\nsetState(...)',
          ),
        ),
        const ExampleBlock(
          title: 'BuildContext 跨 await 使用',
          description:
              'await 期间页面可能被 pop，context 已经失效；lint 也会提示 use_build_context_synchronously',
          child: ContrastNote(
            bad: 'await api.call();\nNavigator.of(context).pop();',
            good: 'await api.call();\nif (!context.mounted) return;\nNavigator.of(context).pop();',
          ),
        ),
        ExampleBlock(
          title: '用 mounted 判断的正确写法',
          description: '下面这个按钮演示：延迟 2 秒后如果页面还在，才更新文案',
          result: _log,
          child: FilledButton.tonal(
            onPressed: () async {
              setState(() => _log = '等待 2 秒…');
              await Future<void>.delayed(const Duration(seconds: 2));
              if (!mounted) {
                // 页面已销毁，安全返回。
                return;
              }
              setState(() => _log = '2 秒后仍然存活，安全地 setState');
            },
            child: const Text('延迟 2 秒后更新（可中途返回试试）'),
          ),
        ),
        const ExampleBlock(
          title: 'const 构造函数',
          description: '加 const 后同一个 Widget 实例会被复用，整棵子树跳过重建',
          child: ContrastNote(
            bad: 'Padding(padding: EdgeInsets.all(8),\n  child: Text(\'固定文案\'))',
            good: 'const Padding(padding: EdgeInsets.all(8),\n  child: Text(\'固定文案\'))',
          ),
        ),
        ExampleBlock(
          title: 'const 的实际效果',
          description: '点击按钮改变父组件状态，观察两个子组件的 build 次数差异',
          child: const _ConstCompareDemo(),
        ),
        const ExampleBlock(
          title: 'Controller / 监听器必须释放',
          description: 'TextEditingController、ScrollController、AnimationController 都要 dispose，否则内存泄漏',
          child: ContrastNote(
            bad: '@override\nvoid dispose() {\n  super.dispose();\n}',
            good: '@override\nvoid dispose() {\n  _controller.dispose();\n  super.dispose();\n}',
          ),
        ),
        ExampleBlock(
          title: 'Controller 用起来',
          description: '本页顶层的 controller 在 dispose 里释放；这里演示它仍可正常使用',
          child: TextField(
            controller: _controller,
            decoration: const InputDecoration(
              labelText: '输入点内容',
              border: OutlineInputBorder(),
            ),
          ),
        ),
        const ExampleBlock(
          title: '长列表不用 builder',
          description: '直接用 children 会把所有 item 一次性建出来，滚动卡顿、内存暴涨',
          child: ContrastNote(
            bad: 'ListView(children: List.generate(10000, (i) => Item(i)))',
            good: 'ListView.builder(itemCount: 10000,\n  itemBuilder: (c, i) => Item(i))',
          ),
        ),
        const ExampleBlock(
          title: '在 build 里做重活',
          description: 'build 会被频繁调用，创建对象、发请求、建 Controller 都不该写在这里',
          child: ContrastNote(
            bad: 'Widget build(context) {\n  final controller = TextEditingController(); // 每次都新建\n  http.get(...);                              // 每次都发请求\n  return TextField(controller: controller);\n}',
            good: '在 State 的字段 / initState 里创建；\n请求放进 initState 或事件回调里',
          ),
        ),
        const ExampleBlock(
          title: '列表 key 用下标',
          description: '用数组下标当 key，删除中间项后状态会串到别的行上；应使用业务 id',
          child: ContrastNote(
            bad: 'key: ValueKey(index)',
            good: 'key: ValueKey(item.id)',
          ),
        ),
      ],
    );
  }
}

class _ConstCompareDemo extends StatefulWidget {
  const _ConstCompareDemo();

  @override
  State<_ConstCompareDemo> createState() => _ConstCompareDemoState();
}

class _ConstCompareDemoState extends State<_ConstCompareDemo> {
  int _tick = 0;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Row(
          children: <Widget>[
            Expanded(
              child: FilledButton.tonal(
                onPressed: () => setState(() => _tick++),
                child: Text('父组件重建 $_tick 次'),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        const _BuildCounter(label: 'const 子组件'),
        _BuildCounter(label: '非 const 子组件', seed: _tick),
      ],
    );
  }
}

class _BuildCounter extends StatefulWidget {
  const _BuildCounter({required this.label, this.seed});

  final String label;
  final int? seed;

  @override
  State<_BuildCounter> createState() => _BuildCounterState();
}

class _BuildCounterState extends State<_BuildCounter> {
  late int _seed = widget.seed ?? -1;

  @override
  Widget build(BuildContext context) {
    // 父组件重建时，非 const 子组件的 widget 实例是新对象，
    // 但 State 会被复用，所以这里只演示「实例是否被复用」的差别。
    final bool sameInstance = widget.seed == _seed;
    _seed = widget.seed ?? -1;
    return ListTile(
      dense: true,
      contentPadding: EdgeInsets.zero,
      leading: Icon(
        sameInstance ? Icons.link : Icons.link_off,
        color: sameInstance ? Colors.green : Colors.orange,
      ),
      title: Text(widget.label),
      subtitle: Text(
        sameInstance
            ? 'widget 实例被复用了（父组件重建时没有新建）'
            : 'widget 实例是新建的',
      ),
    );
  }
}

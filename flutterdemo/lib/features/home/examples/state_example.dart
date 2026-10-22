import 'package:flutter/material.dart';

import '../../../shared/widgets/example_block.dart';

/// 状态管理：setState / ValueNotifier / ChangeNotifier / InheritedWidget / 状态提升。
class StateExample extends StatefulWidget {
  const StateExample({super.key});

  @override
  State<StateExample> createState() => _StateExampleState();
}

class _StateExampleState extends State<StateExample> {
  int _setStateCount = 0;
  int _liftedCount = 0;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        ExampleBlock(
          title: 'setState 本地状态',
          description: 'State 内部最轻量的状态：改完调用 setState 触发本组件重建',
          result: 'setState 计数：$_setStateCount',
          child: FilledButton.tonal(
            onPressed: () => setState(() => _setStateCount++),
            child: const Text('+1（setState）'),
          ),
        ),
        const ExampleBlock(
          title: 'ValueNotifier + ValueListenableBuilder',
          description:
              '把状态放进 ValueNotifier，监听方只在它变化时重建，粒度比 setState 更细',
          child: _ValueNotifierDemo(),
        ),
        const ExampleBlock(
          title: 'ChangeNotifier + ListenableBuilder',
          description:
              '复杂状态（购物车）封装成 ChangeNotifier，用 notifyListeners 通知；跨组件共享时在页面顶层创建',
          child: _CartDemo(),
        ),
        const ExampleBlock(
          title: 'InheritedWidget 跨层级共享',
          description: '自定义 InheritedWidget，任何后代都能 dependOn 到它并自动重建',
          child: _InheritedDemo(),
        ),
        ExampleBlock(
          title: '状态提升 Lifting state up',
          description:
              '需要被两个兄弟组件共享的状态，放到它们的共同父级，由父级持有、子级回调修改',
          result: '父级持有的计数：$_liftedCount',
          child: Row(
            children: <Widget>[
              Expanded(
                child: _CounterButton(
                  label: '子组件 A',
                  onPressed: () => setState(() => _liftedCount++),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: _CounterButton(
                  label: '子组件 B',
                  onPressed: () => setState(() => _liftedCount++),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class _CounterButton extends StatelessWidget {
  const _CounterButton({required this.label, required this.onPressed});

  final String label;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    return OutlinedButton(onPressed: onPressed, child: Text(label));
  }
}

/// ValueNotifier 示例：所有状态都收在一个 StatefulWidget 里。
class _ValueNotifierDemo extends StatefulWidget {
  const _ValueNotifierDemo();

  @override
  State<_ValueNotifierDemo> createState() => _ValueNotifierDemoState();
}

class _ValueNotifierDemoState extends State<_ValueNotifierDemo> {
  final ValueNotifier<int> _counter = ValueNotifier<int>(0);

  @override
  void dispose() {
    _counter.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: <Widget>[
        ValueListenableBuilder<int>(
          valueListenable: _counter,
          builder: (BuildContext context, int value, Widget? _) {
            return ResultText('ValueNotifier 当前值：$value');
          },
        ),
        const SizedBox(height: 12),
        Row(
          children: <Widget>[
            OutlinedButton(
              onPressed: () => _counter.value--,
              child: const Text('-1'),
            ),
            const SizedBox(width: 12),
            OutlinedButton(
              onPressed: () => _counter.value++,
              child: const Text('+1'),
            ),
          ],
        ),
      ],
    );
  }
}

/// ChangeNotifier 示例：购物车模型。
class _CartModel extends ChangeNotifier {
  final List<String> _items = <String>[];

  List<String> get items => List<String>.unmodifiable(_items);

  double get total => _items.length * 19.9;

  void add(String name) {
    _items.add(name);
    notifyListeners();
  }

  void removeLast() {
    if (_items.isEmpty) {
      return;
    }
    _items.removeLast();
    notifyListeners();
  }
}

class _CartDemo extends StatefulWidget {
  const _CartDemo();

  @override
  State<_CartDemo> createState() => _CartDemoState();
}

class _CartDemoState extends State<_CartDemo> {
  final _CartModel _cart = _CartModel();
  int _sequence = 1;

  @override
  void dispose() {
    _cart.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _cart,
      builder: (BuildContext context, Widget? _) {
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            ResultText(
              '购物车：${_cart.items.isEmpty ? '空' : _cart.items.join('、')}\n'
              '总价：￥${_cart.total.toStringAsFixed(2)}',
            ),
            const SizedBox(height: 12),
            Row(
              children: <Widget>[
                Expanded(
                  child: OutlinedButton(
                    onPressed: () => _cart.add('商品 ${_sequence++}'),
                    child: const Text('加入商品'),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: OutlinedButton(
                    onPressed: _cart.removeLast,
                    child: const Text('移除末项'),
                  ),
                ),
              ],
            ),
          ],
        );
      },
    );
  }
}

/// InheritedWidget 示例。
class _CounterScope extends InheritedWidget {
  const _CounterScope({
    required this.count,
    required this.increment,
    required super.child,
  });

  final int count;
  final VoidCallback increment;

  static _CounterScope of(BuildContext context) {
    final _CounterScope? scope =
        context.dependOnInheritedWidgetOfExactType<_CounterScope>();
    assert(scope != null, '未找到 _CounterScope');
    return scope!;
  }

  @override
  bool updateShouldNotify(_CounterScope oldWidget) => oldWidget.count != count;
}

class _InheritedDemo extends StatefulWidget {
  const _InheritedDemo();

  @override
  State<_InheritedDemo> createState() => _InheritedDemoState();
}

class _InheritedDemoState extends State<_InheritedDemo> {
  int _count = 0;

  @override
  Widget build(BuildContext context) {
    return _CounterScope(
      count: _count,
      increment: () => setState(() => _count++),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          const _DeepChild(label: '第一层后代'),
          const Padding(
            padding: EdgeInsets.only(left: 16),
            child: _DeepChild(label: '第二层后代'),
          ),
          const SizedBox(height: 12),
          Text(
            '父级当前计数：$_count',
            style: Theme.of(context).textTheme.bodySmall,
          ),
        ],
      ),
    );
  }
}

class _DeepChild extends StatelessWidget {
  const _DeepChild({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) {
    final _CounterScope scope = _CounterScope.of(context);
    return Row(
      children: <Widget>[
        Text('$label 读到：${scope.count}'),
        const SizedBox(width: 8),
        TextButton(
          onPressed: scope.increment,
          child: const Text('+1'),
        ),
      ],
    );
  }
}

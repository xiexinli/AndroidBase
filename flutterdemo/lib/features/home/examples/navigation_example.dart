import 'package:flutter/material.dart';

import '../../../shared/widgets/example_block.dart';

/// 路由与导航：push / pop 传值、RouteSettings、PopScope、自定义转场。
class NavigationExample extends StatelessWidget {
  const NavigationExample({super.key});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        ExampleBlock(
          title: 'Navigator.push / pop 带回结果',
          description: 'push 一个 MaterialPageRoute，对方 pop 时把值传回来',
          result: '（点击下面的按钮，看返回结果）',
          child: FilledButton.tonal(
            onPressed: () async {
              final String? picked = await Navigator.of(context).push<String>(
                MaterialPageRoute<String>(
                  builder: (BuildContext context) => const _PickerPage(),
                ),
              );
              if (!context.mounted) {
                return;
              }
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(
                  content: Text('返回结果：${picked ?? 'null（直接返回）'}'),
                  duration: const Duration(seconds: 2),
                ),
              );
            },
            child: const Text('打开选择页并等待结果'),
          ),
        ),
        ExampleBlock(
          title: 'RouteSettings 与命名',
          description:
              '给路由一个名字，目标页用 ModalRoute.of(context)!.settings 读取参数',
          child: FilledButton.tonal(
            onPressed: () => Navigator.of(context).push<void>(
              MaterialPageRoute<void>(
                settings: const RouteSettings(
                  name: '/examples/detail',
                  arguments: <String, Object?>{'id': 42, 'from': 'NavigationExample'},
                ),
                builder: (BuildContext context) => const _ArgsPage(),
              ),
            ),
            child: const Text('带 RouteSettings 打开详情页'),
          ),
        ),
        ExampleBlock(
          title: 'pushReplacement / popUntil',
          description:
              'pushReplacement 替换当前页（旧页被 dispose），popUntil 弹到指定路由为止',
          child: Wrap(
            spacing: 12,
            children: <Widget>[
              FilledButton.tonal(
                onPressed: () => Navigator.of(context).pushReplacement<void, void>(
                  MaterialPageRoute<void>(
                    builder: (BuildContext context) => Scaffold(
                      appBar: AppBar(title: const Text('替换后的页面')),
                      body: const Center(
                        child: Text('这是被 pushReplacement 换上的页面，返回时会回到示例目录'),
                      ),
                    ),
                  ),
                ),
                child: const Text('pushReplacement'),
              ),
              FilledButton.tonal(
                onPressed: () {
                  Navigator.of(context).popUntil(ModalRoute.withName('/'));
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      content: Text('popUntil：已弹回首页（Home Tab）'),
                      duration: Duration(seconds: 2),
                    ),
                  );
                },
                child: const Text('popUntil 回首页'),
              ),
            ],
          ),
        ),
        const ExampleBlock(
          title: 'PopScope 拦截返回',
          description:
              'canPop 控制是否允许返回，onPopInvokedWithResult 里做二次确认；下面这个页面会拦截返回',
          child: _PopScopeDemoEntry(),
        ),
        ExampleBlock(
          title: '自定义转场 PageRouteBuilder',
          description: '自己写 transitionsBuilder，可以做缩放、滑动等任意转场动画',
          child: FilledButton.tonal(
            onPressed: () => Navigator.of(context).push<void>(
              PageRouteBuilder<void>(
                transitionDuration: const Duration(milliseconds: 500),
                pageBuilder: (_, _, _) => const _ScaleInPage(),
                transitionsBuilder: (
                  BuildContext context,
                  Animation<double> animation,
                  Animation<double> secondaryAnimation,
                  Widget child,
                ) {
                  return ScaleTransition(
                    scale: CurvedAnimation(
                      parent: animation,
                      curve: Curves.easeOutBack,
                    ),
                    child: FadeTransition(opacity: animation, child: child),
                  );
                },
              ),
            ),
            child: const Text('缩放转场打开新页面'),
          ),
        ),
      ],
    );
  }
}

class _PickerPage extends StatelessWidget {
  const _PickerPage();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('选择一个选项')),
      body: ListView(
        children: <Widget>[
          for (final String option in <String>['选项 A', '选项 B', '选项 C'])
            ListTile(
              title: Text(option),
              onTap: () => Navigator.of(context).pop(option),
            ),
        ],
      ),
    );
  }
}

class _ArgsPage extends StatelessWidget {
  const _ArgsPage();

  @override
  Widget build(BuildContext context) {
    final ModalRoute<dynamic>? route = ModalRoute.of(context);
    final Object? args = route?.settings.arguments;
    final Map<Object?, Object?>? map = args is Map<Object?, Object?> ? args : null;

    return Scaffold(
      appBar: AppBar(title: const Text('详情页')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text('路由名：${route?.settings.name}'),
            const SizedBox(height: 12),
            Text('参数 id：${map?['id']}'),
            Text('参数 from：${map?['from']}'),
            const SizedBox(height: 24),
            const Text('命名路由 vs RouteSettings：'),
            const SizedBox(height: 8),
            const Text(
              '在 Demo 里为了不把路由表写死在 MaterialApp，'
              '这里用 RouteSettings 手动命名；正式项目一般配合 '
              'onGenerateRoute / go_router 统一维护路由表。',
            ),
          ],
        ),
      ),
    );
  }
}

class _PopScopeDemoEntry extends StatelessWidget {
  const _PopScopeDemoEntry();

  @override
  Widget build(BuildContext context) {
    return FilledButton.tonal(
      onPressed: () => Navigator.of(context).push<void>(
        MaterialPageRoute<void>(
          builder: (BuildContext context) => const _PopScopePage(),
        ),
      ),
      child: const Text('打开会拦截返回的页面'),
    );
  }
}

class _PopScopePage extends StatefulWidget {
  const _PopScopePage();

  @override
  State<_PopScopePage> createState() => _PopScopePageState();
}

class _PopScopePageState extends State<_PopScopePage> {
  @override
  Widget build(BuildContext context) {
    return PopScope<Object?>(
      canPop: false,
      onPopInvokedWithResult: (bool didPop, Object? result) async {
        if (didPop) {
          return;
        }
        final bool? leave = await showDialog<bool>(
          context: context,
          builder: (BuildContext context) => AlertDialog(
            title: const Text('要离开吗？'),
            content: const Text('PopScope 拦截了这次返回，二次确认后才真的退出。'),
            actions: <Widget>[
              TextButton(
                onPressed: () => Navigator.of(context).pop(false),
                child: const Text('留下'),
              ),
              FilledButton(
                onPressed: () => Navigator.of(context).pop(true),
                child: const Text('离开'),
              ),
            ],
          ),
        );
        if (leave == true && context.mounted) {
          Navigator.of(context).pop();
        }
      },
      child: Scaffold(
        appBar: AppBar(title: const Text('PopScope 示例')),
        body: const Center(
          child: Text('按返回键 / 侧滑返回试试'),
        ),
      ),
    );
  }
}

class _ScaleInPage extends StatelessWidget {
  const _ScaleInPage();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('自定义转场')),
      body: const Center(child: Text('我是被 PageRouteBuilder 以缩放 + 淡入方式打开的')),
    );
  }
}

import 'dart:async';

import 'package:flutter/material.dart';

import '../../../shared/widgets/example_block.dart';

/// 异步与流：Future / FutureBuilder / Stream / StreamController / async* / Future.wait。
class AsyncExample extends StatefulWidget {
  const AsyncExample({super.key});

  @override
  State<AsyncExample> createState() => _AsyncExampleState();
}

class _AsyncExampleState extends State<AsyncExample> {
  String _awaitResult = '（尚未执行）';
  Future<String>? _future;
  Stream<int>? _ticker;

  final StreamController<String> _controller =
      StreamController<String>.broadcast();
  int _pushed = 0;

  @override
  void dispose() {
    _controller.close();
    super.dispose();
  }

  Future<String> _fetchName() async {
    await Future<void>.delayed(const Duration(milliseconds: 800));
    return '张三';
  }

  Future<void> _runAwait() async {
    setState(() => _awaitResult = '请求中…');
    try {
      final String name = await _fetchName();
      if (!mounted) {
        return;
      }
      setState(() => _awaitResult = '拿到结果：$name');
    } catch (e) {
      if (!mounted) {
        return;
      }
      setState(() => _awaitResult = '失败：$e');
    }
  }

  Future<int> _failAfterDelay() async {
    await Future<void>.delayed(const Duration(milliseconds: 600));
    throw StateError('模拟一个异步异常');
  }

  Future<String> _fetchSlow() async {
    await Future<void>.delayed(const Duration(milliseconds: 1200));
    return '慢接口';
  }

  Future<String> _fetchFast() async {
    await Future<void>.delayed(const Duration(milliseconds: 400));
    return '快接口';
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        ExampleBlock(
          title: 'async / await / try-catch',
          description: 'await 会挂起当前函数而不阻塞 UI；异常用 try-catch 捕获',
          result: _awaitResult,
          child: Wrap(
            spacing: 12,
            children: <Widget>[
              FilledButton.tonal(
                onPressed: _runAwait,
                child: const Text('await 一个 Future'),
              ),
              FilledButton.tonal(
                onPressed: () async {
                  try {
                    await _failAfterDelay();
                  } catch (e) {
                    if (!mounted) {
                      return;
                    }
                    setState(() => _awaitResult = '捕获异常：$e');
                  }
                },
                child: const Text('捕获异步异常'),
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'FutureBuilder',
          description:
              '根据 snapshot.connectionState / hasError / hasData 渲染三态；注意 future 要在 State 里创建一次',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              FilledButton.tonal(
                onPressed: () {
                  setState(() {
                    _future = _failAfterDelay().then((int _) => '不会走到这里');
                  });
                },
                child: const Text('触发一个会失败的 Future'),
              ),
              const SizedBox(height: 12),
              FutureBuilder<String>(
                future: _future,
                builder: (
                  BuildContext context,
                  AsyncSnapshot<String> snapshot,
                ) {
                  switch (snapshot.connectionState) {
                    case ConnectionState.none:
                      return const Text('尚未发起请求');
                    case ConnectionState.waiting:
                      return const Row(
                        children: <Widget>[
                          SizedBox(
                            width: 16,
                            height: 16,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          ),
                          SizedBox(width: 12),
                          Text('加载中…'),
                        ],
                      );
                    case ConnectionState.done:
                      if (snapshot.hasError) {
                        return Text(
                          '出错了：${snapshot.error}',
                          style: TextStyle(
                            color: Theme.of(context).colorScheme.error,
                          ),
                        );
                      }
                      return Text('成功：${snapshot.data}');
                    default:
                      return Text('连接状态：${snapshot.connectionState}');
                  }
                },
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'Stream / StreamBuilder',
          description:
              'Stream 是一串随时间到达的事件；Stream.periodic 每秒产出一个序号',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              FilledButton.tonal(
                onPressed: () {
                  setState(() {
                    _ticker ??= Stream<int>.periodic(
                      const Duration(seconds: 1),
                      (int i) => i + 1,
                    ).take(10);
                  });
                },
                child: const Text('启动每秒一次的倒计时流'),
              ),
              const SizedBox(height: 12),
              StreamBuilder<int>(
                stream: _ticker,
                builder: (
                  BuildContext context,
                  AsyncSnapshot<int> snapshot,
                ) {
                  if (!snapshot.hasData) {
                    return const Text('流尚未产生数据');
                  }
                  return Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: <Widget>[
                      Text('第 ${snapshot.data} 次事件'),
                      const SizedBox(height: 8),
                      LinearProgressIndicator(value: (snapshot.data ?? 0) / 10),
                    ],
                  );
                },
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'StreamController 手动推流',
          description:
              '自己控制何时 add 事件；broadcast() 允许多个监听者，用完记得 close()',
          result: '已推送 $_pushed 条',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Wrap(
                spacing: 12,
                children: <Widget>[
                  FilledButton.tonal(
                    onPressed: () {
                      _pushed++;
                      _controller.add('第 $_pushed 条消息');
                    },
                    child: const Text('推送一条消息'),
                  ),
                  OutlinedButton(
                    onPressed: () {
                      _controller.addError(StateError('手动推入的错误'));
                    },
                    child: const Text('推送错误'),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              StreamBuilder<String>(
                stream: _controller.stream,
                builder: (
                  BuildContext context,
                  AsyncSnapshot<String> snapshot,
                ) {
                  if (snapshot.hasError) {
                    return Text(
                      '收到错误：${snapshot.error}',
                      style: TextStyle(
                        color: Theme.of(context).colorScheme.error,
                      ),
                    );
                  }
                  return ResultText(
                    snapshot.hasData ? '收到：${snapshot.data}' : '等待事件…',
                  );
                },
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'async* 生成器 + await for',
          description: 'yield 逐个产出事件，消费端用 await for 顺序处理',
          child: FilledButton.tonal(
            onPressed: () async {
              final StringBuffer buffer = StringBuffer();
              await for (final String value
                  in _countdownStream(4).timeout(const Duration(seconds: 5))) {
                buffer.writeln(value);
                if (!mounted) {
                  return;
                }
                setState(() => _awaitResult = buffer.toString().trim());
              }
            },
            child: const Text('消费 async* 流'),
          ),
        ),
        ExampleBlock(
          title: 'Future.wait / Future.any',
          description:
              'wait 等全部完成（总耗时≈最慢的那个），any 取最快返回的结果',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              FilledButton.tonal(
                onPressed: () async {
                  final DateTime start = DateTime.now();
                  final List<String> results = await Future.wait<String>(
                    <Future<String>>[_fetchFast(), _fetchSlow()],
                  );
                  final int cost =
                      DateTime.now().difference(start).inMilliseconds;
                  if (!mounted) {
                    return;
                  }
                  setState(
                    () => _awaitResult =
                        'Future.wait：${results.join('、')}（耗时 ${cost}ms）',
                  );
                },
                child: const Text('Future.wait'),
              ),
              const SizedBox(height: 8),
              FilledButton.tonal(
                onPressed: () async {
                  final String fastest = await Future.any<String>(
                    <Future<String>>[_fetchFast(), _fetchSlow()],
                  );
                  if (!mounted) {
                    return;
                  }
                  setState(() => _awaitResult = 'Future.any：$fastest');
                },
                child: const Text('Future.any'),
              ),
              const SizedBox(height: 12),
              ResultText(_awaitResult),
            ],
          ),
        ),
      ],
    );
  }

  /// async* 生成器：逐个 yield。
  Stream<String> _countdownStream(int from) async* {
    for (int i = from; i > 0; i--) {
      await Future<void>.delayed(const Duration(milliseconds: 300));
      yield '倒计时 $i';
    }
    yield '发射完成';
  }
}

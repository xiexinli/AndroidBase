import 'dart:async';
import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

import '../../../shared/widgets/example_block.dart';

/// 网络请求：GET / POST / JSON 解析 / 加载与错误态 / 超时。
///
/// 用 https://jsonplaceholder.typicode.com 这个公开的假接口做演示。
/// 注意：Android 端需要 `android.permission.INTERNET` 权限（本工程已在
/// AndroidManifest 中声明）。
class NetworkExample extends StatefulWidget {
  const NetworkExample({super.key});

  @override
  State<NetworkExample> createState() => _NetworkExampleState();
}

/// 接口返回的一条 Post，演示 JSON → 模型的映射。
class Post {
  const Post({required this.id, required this.title, required this.body});

  final int id;
  final String title;
  final String body;

  factory Post.fromJson(Map<String, dynamic> json) {
    return Post(
      id: json['id'] as int? ?? 0,
      title: json['title'] as String? ?? '',
      body: json['body'] as String? ?? '',
    );
  }
}

class _NetworkExampleState extends State<NetworkExample> {
  static const String _baseUrl = 'https://jsonplaceholder.typicode.com';

  final List<Post> _posts = <Post>[];
  bool _loading = false;
  String? _error;
  int _page = 1;

  String _postResult = '（尚未提交）';
  String _timeoutResult = '（尚未请求）';

  @override
  void initState() {
    super.initState();
    _loadPosts();
  }

  Future<void> _loadPosts() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final http.Response response = await http
          .get(Uri.parse('$_baseUrl/posts?_limit=8&_page=$_page'))
          .timeout(const Duration(seconds: 10));
      if (response.statusCode != 200) {
        throw http.ClientException('HTTP ${response.statusCode}');
      }
      final List<dynamic> data =
          jsonDecode(utf8.decode(response.bodyBytes)) as List<dynamic>;
      if (!mounted) {
        return;
      }
      setState(() {
        _posts
          ..clear()
          ..addAll(
            data.map((dynamic e) => Post.fromJson(e as Map<String, dynamic>)),
          );
        _loading = false;
      });
    } on TimeoutException {
      _fail('请求超时，请检查网络');
    } on http.ClientException catch (e) {
      _fail('网络异常：${e.message}');
    } catch (e) {
      _fail('解析或未知异常：$e');
    }
  }

  void _fail(String message) {
    if (!mounted) {
      return;
    }
    setState(() {
      _loading = false;
      _error = message;
    });
  }

  Future<void> _createPost() async {
    try {
      final http.Response response = await http
          .post(
            Uri.parse('$_baseUrl/posts'),
            headers: const <String, String>{
              'Content-Type': 'application/json; charset=UTF-8',
            },
            body: jsonEncode(<String, Object>{
              'title': '来自 Flutter 的标题',
              'body': 'POST 示例',
              'userId': 1,
            }),
          )
          .timeout(const Duration(seconds: 10));
      if (!mounted) {
        return;
      }
      setState(() {
        _postResult = '状态码 ${response.statusCode}\n'
            '响应体：${response.body.length > 160 ? '${response.body.substring(0, 160)}…' : response.body}';
      });
    } catch (e) {
      if (!mounted) {
        return;
      }
      setState(() => _postResult = '提交失败：$e');
    }
  }

  Future<void> _requestWithShortTimeout() async {
    setState(() => _timeoutResult = '请求中…');
    try {
      final http.Response response = await http
          .get(Uri.parse('$_baseUrl/posts/1'))
          // 故意用极短的超时演示超时分支。
          .timeout(const Duration(milliseconds: 1));
      if (!mounted) {
        return;
      }
      setState(() => _timeoutResult = '竟然成功了：${response.statusCode}');
    } on TimeoutException {
      if (!mounted) {
        return;
      }
      setState(() => _timeoutResult = '捕获到 TimeoutException（这就是超时分支）');
    } catch (e) {
      if (!mounted) {
        return;
      }
      setState(() => _timeoutResult = '其它异常：$e');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        ExampleBlock(
          title: 'GET + JSON 解析 + 三态 UI',
          description:
              '加载中显示进度条、失败显示错误与重试、成功渲染列表——网络请求页面的标准三态',
          result: _loading ? '状态：加载中' : (_error ?? '状态：加载完成，共 ${_posts.length} 条'),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Row(
                children: <Widget>[
                  FilledButton.tonal(
                    onPressed: _loading ? null : _loadPosts,
                    child: const Text('重新请求'),
                  ),
                  const SizedBox(width: 12),
                  OutlinedButton(
                    onPressed: _loading
                        ? null
                        : () {
                            _page = _page >= 3 ? 1 : _page + 1;
                            _loadPosts();
                          },
                    child: Text('第 $_page 页'),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              if (_loading)
                const Padding(
                  padding: EdgeInsets.symmetric(vertical: 24),
                  child: Center(child: CircularProgressIndicator()),
                )
              else if (_error != null)
                Padding(
                  padding: const EdgeInsets.symmetric(vertical: 24),
                  child: Column(
                    children: <Widget>[
                      Icon(
                        Icons.error_outline,
                        color: Theme.of(context).colorScheme.error,
                      ),
                      const SizedBox(height: 8),
                      Text(_error!),
                      const SizedBox(height: 12),
                      OutlinedButton(
                        onPressed: _loadPosts,
                        child: const Text('重试'),
                      ),
                    ],
                  ),
                )
              else
                Column(
                  children: <Widget>[
                    for (final Post post in _posts)
                      ListTile(
                        contentPadding: EdgeInsets.zero,
                        dense: true,
                        leading: CircleAvatar(
                          radius: 14,
                          child: Text(
                            '${post.id}',
                            style: const TextStyle(fontSize: 11),
                          ),
                        ),
                        title: Text(
                          post.title,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                        subtitle: Text(
                          post.body,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                  ],
                ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'POST 提交 JSON',
          description: '设置 Content-Type 后用 jsonEncode 发送，读取 statusCode 判断结果',
          result: _postResult,
          child: FilledButton.tonal(
            onPressed: _createPost,
            child: const Text('提交一条 Post'),
          ),
        ),
        ExampleBlock(
          title: '耗时计算放到 compute',
          description:
              '重计算放到 isolate，避免阻塞 UI 线程；下面算 1~2,000,000 的和',
          child: const _ComputeDemo(),
        ),
        ExampleBlock(
          title: '超时与异常处理',
          description:
              'http 的请求要自己包 TimeoutException；catch 顺序会影响能否命中具体分支',
          result: _timeoutResult,
          child: FilledButton.tonal(
            onPressed: _requestWithShortTimeout,
            child: const Text('发起一次 1ms 超时的请求'),
          ),
        ),
      ],
    );
  }
}

class _ComputeDemo extends StatefulWidget {
  const _ComputeDemo();

  @override
  State<_ComputeDemo> createState() => _ComputeDemoState();
}

/// 顶层函数，才能作为 compute 的入口。
int _sumUpTo(int n) {
  int sum = 0;
  for (int i = 1; i <= n; i++) {
    sum += i;
  }
  return sum;
}

class _ComputeDemoState extends State<_ComputeDemo> {
  String _result = '（尚未计算）';

  Future<void> _run() async {
    setState(() => _result = '计算中…');
    final int sum = await compute(_sumUpTo, 2000000);
    if (!mounted) {
      return;
    }
    setState(() => _result = '1 + … + 2,000,000 = $sum');
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        FilledButton.tonal(onPressed: _run, child: const Text('开始计算')),
        const SizedBox(height: 12),
        ResultText(_result),
      ],
    );
  }
}

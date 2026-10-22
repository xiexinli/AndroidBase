import 'package:flutter/material.dart';

/// 示例块：一个「控件 / 用法」的完整演示单元。
///
/// 版式与 AndroidBase `:demo` 模块里 Compose 示例的
/// `ExampleBlock` / `ExampleLabel` / `ResultText` 对齐：
/// 标题 + 说明 + 演示区 + 结果输出。
class ExampleBlock extends StatelessWidget {
  const ExampleBlock({
    super.key,
    required this.title,
    required this.child,
    this.description,
    this.result,
  });

  /// 示例名称，例如「Text 文本」。
  final String title;

  /// 一句话说明这个示例演示了什么。
  final String? description;

  /// 演示内容。
  final Widget child;

  /// 需要展示「结果 / 状态」时输出，例如按钮点击次数。
  final String? result;

  @override
  Widget build(BuildContext context) {
    final TextTheme textTheme = Theme.of(context).textTheme;
    final ColorScheme scheme = Theme.of(context).colorScheme;
    final String? description = this.description;

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            ExampleLabel(title),
            if (description != null) ...<Widget>[
              const SizedBox(height: 4),
              Text(
                description,
                style: textTheme.bodySmall?.copyWith(
                  color: scheme.onSurfaceVariant,
                ),
              ),
            ],
            const Divider(height: 24),
            child,
            if (result != null) ...<Widget>[
              const SizedBox(height: 12),
              ResultText(result!),
            ],
          ],
        ),
      ),
    );
  }
}

/// 示例块的小标题。
class ExampleLabel extends StatelessWidget {
  const ExampleLabel(this.text, {super.key});

  final String text;

  @override
  Widget build(BuildContext context) {
    return Text(
      text,
      style: Theme.of(context)
          .textTheme
          .titleSmall
          ?.copyWith(fontWeight: FontWeight.w600),
    );
  }
}

/// 示例的「结果」输出，等宽字体便于对齐观察。
class ResultText extends StatelessWidget {
  const ResultText(this.text, {super.key});

  final String text;

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: scheme.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(8),
      ),
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
}

/// 错误 / 正确写法的对照文本，供「易错点」一类示例复用。
class ContrastNote extends StatelessWidget {
  const ContrastNote({super.key, required this.bad, required this.good});

  final String bad;
  final String good;

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        _line(context, Icons.close, scheme.error, bad),
        const SizedBox(height: 6),
        _line(context, Icons.check, Colors.green, good),
      ],
    );
  }

  Widget _line(BuildContext context, IconData icon, Color color, String text) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Icon(icon, size: 16, color: color),
        const SizedBox(width: 8),
        Expanded(
          child: Text(
            text,
            style: TextStyle(
              fontFamily: 'monospace',
              fontSize: 12,
              height: 1.4,
              color: Theme.of(context).colorScheme.onSurfaceVariant,
            ),
          ),
        ),
      ],
    );
  }
}

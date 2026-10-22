import 'package:flutter/material.dart';

import 'example_category.dart';

/// 二级页：承载单个分类下的全部示例。
///
/// 只负责脚手架（标题栏 + 说明 + 滚动容器），示例内容由
/// [ExampleCategory.builder] 提供。
class ExampleCategoryPage extends StatelessWidget {
  const ExampleCategoryPage({super.key, required this.category});

  final ExampleCategory category;

  @override
  Widget build(BuildContext context) {
    final TextTheme textTheme = Theme.of(context).textTheme;
    final ColorScheme scheme = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(title: Text(category.title)),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 32),
        children: <Widget>[
          Text(
            category.summary,
            style: textTheme.bodyMedium?.copyWith(
              color: scheme.onSurfaceVariant,
              height: 1.5,
            ),
          ),
          const SizedBox(height: 16),
          category.builder(),
        ],
      ),
    );
  }
}

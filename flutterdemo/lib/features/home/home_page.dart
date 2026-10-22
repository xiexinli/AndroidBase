import 'package:flutter/material.dart';

import 'catalog/example_category.dart';
import 'catalog/example_category_page.dart';

/// Home 页：Flutter 控件与用法示例的总入口。
///
/// 结构对齐 AndroidBase `:demo` 模块里 Compose 示例的做法：
/// 一级页是分类目录，点进去是承载单项示例的二级页。
class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;
    final TextTheme textTheme = Theme.of(context).textTheme;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Flutter 示例'),
        actions: <Widget>[
          Padding(
            padding: const EdgeInsets.only(right: 16),
            child: Center(
              child: Text(
                '${exampleCategories.length} 个分类',
                style: textTheme.labelMedium?.copyWith(
                  color: scheme.onSurfaceVariant,
                ),
              ),
            ),
          ),
        ],
      ),
      body: ListView.builder(
        padding: const EdgeInsets.only(bottom: 16),
        itemCount: exampleCategories.length + 1,
        itemBuilder: (BuildContext context, int index) {
          if (index == 0) {
            return _Header(count: exampleCategories.length);
          }
          final ExampleCategory category = exampleCategories[index - 1];
          return _CategoryTile(category: category);
        },
      ),
    );
  }
}

class _Header extends StatelessWidget {
  const _Header({required this.count});

  final int count;

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;
    return Container(
      width: double.infinity,
      margin: const EdgeInsets.all(16),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: scheme.primaryContainer,
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            'Flutter 用法示例',
            style: Theme.of(context)
                .textTheme
                .titleMedium
                ?.copyWith(color: scheme.onPrimaryContainer),
          ),
          const SizedBox(height: 6),
          Text(
            '共 $count 类，覆盖控件、布局、输入、列表、状态、手势、动画、'
            '浮层、主题、路由、网络、异步与常见踩坑。',
            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                  color: scheme.onPrimaryContainer,
                  height: 1.5,
                ),
          ),
        ],
      ),
    );
  }
}

class _CategoryTile extends StatelessWidget {
  const _CategoryTile({required this.category});

  final ExampleCategory category;

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;
    final TextTheme textTheme = Theme.of(context).textTheme;

    return ListTile(
      leading: CircleAvatar(
        backgroundColor: scheme.secondaryContainer,
        foregroundColor: scheme.onSecondaryContainer,
        child: Icon(category.icon, size: 20),
      ),
      title: Text(category.title),
      subtitle: Text(
        category.summary,
        maxLines: 2,
        overflow: TextOverflow.ellipsis,
        style: textTheme.bodySmall?.copyWith(color: scheme.onSurfaceVariant),
      ),
      trailing: const Icon(Icons.chevron_right),
      onTap: () {
        Navigator.of(context).push<void>(
          MaterialPageRoute<void>(
            settings: RouteSettings(name: '/examples/${category.id}'),
            builder: (BuildContext context) =>
                ExampleCategoryPage(category: category),
          ),
        );
      },
    );
  }
}

import 'package:flutter/material.dart';

import '../../../shared/widgets/example_block.dart';

/// 列表与网格：ListView / GridView / 下拉刷新 / 滑动删除 / Sliver。
class ListExample extends StatefulWidget {
  const ListExample({super.key});

  @override
  State<ListExample> createState() => _ListExampleState();
}

class _ListExampleState extends State<ListExample> {
  final ScrollController _controller = ScrollController();
  final List<int> _items = List<int>.generate(200, (int index) => index);
  double _offset = 0;

  @override
  void initState() {
    super.initState();
    _controller.addListener(() {
      setState(() => _offset = _controller.offset);
    });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _refresh() async {
    await Future<void>.delayed(const Duration(milliseconds: 800));
    if (!mounted) {
      return;
    }
    setState(() {
      _items.insert(0, _items.length);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        ExampleBlock(
          title: 'ListView + ListTile',
          description: '内容很少时可以直接把子项写死；ListTile 是列表项的标准骨架',
          child: Column(
            children: <Widget>[
              for (final String title in <String>['收件箱', '已发送', '草稿箱'])
                ListTile(
                  contentPadding: EdgeInsets.zero,
                  leading: const Icon(Icons.folder_outlined),
                  title: Text(title),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () {},
                ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'ListView.builder 按需构建',
          description:
              '长列表必须用 builder（或 separated），否则 200 个 item 会一次性全部创建',
          result: '滚动偏移：${_offset.toStringAsFixed(1)}',
          child: SizedBox(
            height: 240,
            child: ListView.builder(
              controller: _controller,
              itemCount: _items.length,
              itemBuilder: (BuildContext context, int index) {
                final int value = _items[index];
                return ListTile(
                  dense: true,
                  leading: CircleAvatar(
                    radius: 14,
                    child: Text('$value', style: const TextStyle(fontSize: 11)),
                  ),
                  title: Text('列表项 #$value'),
                  subtitle: const Text('itemBuilder 只在即将可见时才被调用'),
                );
              },
            ),
          ),
        ),
        ExampleBlock(
          title: 'ListView.separated 分隔线',
          description: 'separatorBuilder 负责在项之间插入分隔组件，比手写 Divider 更清晰',
          child: SizedBox(
            height: 200,
            child: ListView.separated(
              itemCount: 12,
              separatorBuilder: (BuildContext context, int index) =>
                  const Divider(height: 1),
              itemBuilder: (BuildContext context, int index) =>
                  ListTile(dense: true, title: Text('分隔列表项 $index')),
            ),
          ),
        ),
        ExampleBlock(
          title: 'GridView 网格',
          description: 'GridView.count 固定列数，GridView.builder 按需构建并支持自定义间距',
          child: SizedBox(
            height: 260,
            child: GridView.builder(
              gridDelegate:
                  const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 3,
                mainAxisSpacing: 8,
                crossAxisSpacing: 8,
              ),
              itemCount: 12,
              itemBuilder: (BuildContext context, int index) {
                final ColorScheme scheme = Theme.of(context).colorScheme;
                return Container(
                  alignment: Alignment.center,
                  decoration: BoxDecoration(
                    color: scheme.surfaceContainerHighest,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text('格子 $index'),
                );
              },
            ),
          ),
        ),
        ExampleBlock(
          title: 'Dismissible 滑动删除',
          description: 'key 必须唯一且稳定；背景色用 background / secondaryBackground',
          result: '剩余 ${_items.length} 项',
          child: SizedBox(
            height: 200,
            child: ListView.builder(
              itemCount: _items.length,
              itemBuilder: (BuildContext context, int index) {
                final int value = _items[index];
                return Dismissible(
                  key: ValueKey<int>(value),
                  background: Container(
                    color: Theme.of(context).colorScheme.errorContainer,
                    alignment: Alignment.centerRight,
                    padding: const EdgeInsets.only(right: 16),
                    child: const Icon(Icons.delete),
                  ),
                  onDismissed: (_) {
                    setState(() => _items.removeAt(index));
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(
                        content: Text('已删除 #$value'),
                        duration: const Duration(seconds: 1),
                      ),
                    );
                  },
                  child: ListTile(
                    dense: true,
                    title: Text('左滑删除 #$value'),
                  ),
                );
              },
            ),
          ),
        ),
        ExampleBlock(
          title: 'RefreshIndicator 下拉刷新',
          description: 'onRefresh 返回的 Future 结束时收起指示器；列表必须可滚动',
          child: SizedBox(
            height: 200,
            child: RefreshIndicator(
              onRefresh: _refresh,
              child: ListView.builder(
                itemCount: _items.length,
                itemBuilder: (BuildContext context, int index) =>
                    ListTile(dense: true, title: Text('下拉试试 #${_items[index]}')),
              ),
            ),
          ),
        ),
        ExampleBlock(
          title: 'CustomScrollView + Sliver',
          description:
              'SliverAppBar 随滚动折叠；SliverList / SliverGrid 组合出复杂滚动结构',
          child: SizedBox(
            height: 320,
            child: CustomScrollView(
              slivers: <Widget>[
                SliverAppBar(
                  pinned: true,
                  expandedHeight: 120,
                  flexibleSpace: FlexibleSpaceBar(
                    title: const Text('SliverAppBar'),
                    background: Container(
                      color: Theme.of(context).colorScheme.primaryContainer,
                    ),
                  ),
                ),
                SliverList.builder(
                  itemCount: 8,
                  itemBuilder: (BuildContext context, int index) =>
                      ListTile(dense: true, title: Text('SliverList 项 $index')),
                ),
                SliverGrid.builder(
                  gridDelegate:
                      const SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 3,
                    mainAxisSpacing: 8,
                    crossAxisSpacing: 8,
                  ),
                  itemCount: 6,
                  itemBuilder: (BuildContext context, int index) => Container(
                    alignment: Alignment.center,
                    color: Theme.of(context).colorScheme.secondaryContainer,
                    child: Text('G$index'),
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

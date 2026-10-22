import 'package:flutter/material.dart';

import '../../../shared/widgets/example_block.dart';

/// 弹窗与浮层：Dialog / BottomSheet / SnackBar / Tooltip / PopupMenu / OverlayEntry。
class OverlayExample extends StatefulWidget {
  const OverlayExample({super.key});

  @override
  State<OverlayExample> createState() => _OverlayExampleState();
}

class _OverlayExampleState extends State<OverlayExample> {
  String _result = '尚未操作';
  OverlayEntry? _entry;

  @override
  void dispose() {
    _entry?.remove();
    _entry = null;
    super.dispose();
  }

  Future<void> _showAlert() async {
    final bool? confirmed = await showDialog<bool>(
      context: context,
      builder: (BuildContext context) => AlertDialog(
        icon: const Icon(Icons.help_outline),
        title: const Text('确认操作'),
        content: const Text('showDialog 返回一个 Future，可以通过 pop 带回结果。'),
        actions: <Widget>[
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('取消'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text('确定'),
          ),
        ],
      ),
    );
    if (!mounted) {
      return;
    }
    setState(() => _result = confirmed == true ? 'AlertDialog → 确定' : 'AlertDialog → 取消');
  }

  Future<void> _showSheet() async {
    final String? picked = await showModalBottomSheet<String>(
      context: context,
      showDragHandle: true,
      builder: (BuildContext context) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            ListTile(
              leading: const Icon(Icons.photo),
              title: const Text('从相册选择'),
              onTap: () => Navigator.of(context).pop('相册'),
            ),
            ListTile(
              leading: const Icon(Icons.camera_alt),
              title: const Text('拍照'),
              onTap: () => Navigator.of(context).pop('相机'),
            ),
            ListTile(
              leading: const Icon(Icons.close),
              title: const Text('取消'),
              onTap: () => Navigator.of(context).pop(),
            ),
          ],
        ),
      ),
    );
    if (!mounted) {
      return;
    }
    setState(() => _result = 'BottomSheet → ${picked ?? '已取消'}');
  }

  Future<void> _pickDateTime() async {
    final DateTime? date = await showDatePicker(
      context: context,
      initialDate: DateTime.now(),
      firstDate: DateTime(2020),
      lastDate: DateTime(2030),
    );
    if (!mounted || date == null) {
      return;
    }
    final TimeOfDay? time = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.now(),
    );
    if (!mounted) {
      return;
    }
    setState(() {
      _result = '选择时间：${date.year}-${date.month}-${date.day} '
          '${time?.format(context) ?? ''}';
    });
  }

  void _insertOverlay() {
    _entry?.remove();
    final OverlayState overlay = Overlay.of(context, rootOverlay: true);
    _entry = OverlayEntry(
      builder: (BuildContext context) => Positioned(
        left: 24,
        right: 24,
        bottom: 96,
        child: Material(
          color: Colors.transparent,
          child: Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Theme.of(context).colorScheme.inverseSurface,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Row(
              children: <Widget>[
                Expanded(
                  child: Text(
                    '这是一条用 OverlayEntry 插入的自定义浮层',
                    style: TextStyle(
                      color: Theme.of(context).colorScheme.onInverseSurface,
                    ),
                  ),
                ),
                TextButton(
                  onPressed: () {
                    _entry?.remove();
                    _entry = null;
                    setState(() => _result = 'OverlayEntry 已移除');
                  },
                  child: const Text('移除'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
    overlay.insert(_entry!);
    setState(() => _result = 'OverlayEntry 已插入');
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        ExampleBlock(
          title: 'AlertDialog / showDialog',
          description: 'showDialog 返回 Future，用 Navigator.pop 把结果带回调用方',
          result: _result,
          child: FilledButton.tonal(
            onPressed: _showAlert,
            child: const Text('弹出确认对话框'),
          ),
        ),
        ExampleBlock(
          title: 'showModalBottomSheet',
          description: '底部弹出面板，showDragHandle 控制顶部拖拽条，返回被选项',
          child: FilledButton.tonal(
            onPressed: _showSheet,
            child: const Text('弹出底部面板'),
          ),
        ),
        ExampleBlock(
          title: 'SnackBar / MaterialBanner',
          description: 'ScaffoldMessenger 管理，页面切换时也能正确跟随；Banner 是常驻顶部条',
          child: Wrap(
            spacing: 12,
            children: <Widget>[
              FilledButton.tonal(
                onPressed: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      content: Text('这是一条 SnackBar'),
                      duration: Duration(seconds: 2),
                    ),
                  );
                },
                child: const Text('SnackBar'),
              ),
              FilledButton.tonal(
                onPressed: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: const Text('带操作按钮的 SnackBar'),
                      action: SnackBarAction(
                        label: '撤销',
                        onPressed: () => setState(() => _result = 'SnackBar → 撤销'),
                      ),
                    ),
                  );
                },
                child: const Text('带 Action'),
              ),
              FilledButton.tonal(
                onPressed: () {
                  ScaffoldMessenger.of(context).showMaterialBanner(
                    MaterialBanner(
                      content: const Text('MaterialBanner：常驻在页面顶部'),
                      actions: <Widget>[
                        TextButton(
                          onPressed: () => ScaffoldMessenger.of(context)
                              .hideCurrentMaterialBanner(),
                          child: const Text('关闭'),
                        ),
                      ],
                    ),
                  );
                },
                child: const Text('MaterialBanner'),
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'Tooltip / PopupMenuButton',
          description: 'Tooltip 长按显示提示，PopupMenuButton 弹出锚定菜单',
          child: Row(
            children: <Widget>[
              Tooltip(
                message: '这是一段提示文案',
                child: IconButton(
                  onPressed: () {},
                  icon: const Icon(Icons.info_outline),
                ),
              ),
              const SizedBox(width: 8),
              PopupMenuButton<String>(
                onSelected: (String value) =>
                    setState(() => _result = 'PopupMenu → $value'),
                itemBuilder: (BuildContext context) =>
                    const <PopupMenuEntry<String>>[
                  PopupMenuItem<String>(value: '复制', child: Text('复制')),
                  PopupMenuItem<String>(value: '重命名', child: Text('重命名')),
                  PopupMenuDivider(),
                  PopupMenuItem<String>(value: '删除', child: Text('删除')),
                ],
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'showDatePicker / showTimePicker',
          description: 'Material 内置的日期与时间选择器，无需自己实现',
          child: FilledButton.tonal(
            onPressed: _pickDateTime,
            child: const Text('选择日期与时间'),
          ),
        ),
        ExampleBlock(
          title: 'Overlay / OverlayEntry 自定义浮层',
          description: 'Overlay 是浮层的最终宿主，Dialog / SnackBar 底层都建立在它之上',
          child: FilledButton.tonal(
            onPressed: _insertOverlay,
            child: const Text('插入自定义浮层'),
          ),
        ),
      ],
    );
  }
}

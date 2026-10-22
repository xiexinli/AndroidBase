import 'package:flutter/material.dart';

import '../../../shared/widgets/example_block.dart';

/// 输入与表单：TextField、Form 校验、选择类控件。
class InputExample extends StatefulWidget {
  const InputExample({super.key});

  @override
  State<InputExample> createState() => _InputExampleState();
}

class _InputExampleState extends State<InputExample> {
  final TextEditingController _controller = TextEditingController();
  final GlobalKey<FormState> _formKey = GlobalKey<FormState>();
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();

  String _liveText = '';
  String _submitted = '';
  bool _obscure = true;
  bool _notify = true;
  bool _agree = false;
  int _level = 1;
  double _volume = 0.4;
  RangeValues _priceRange = const RangeValues(20, 80);
  String _city = '北京';

  @override
  void dispose() {
    _controller.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        ExampleBlock(
          title: 'TextField 基础',
          description: 'controller 双向读写、onChanged 实时响应、decoration 定制外观',
          result: '实时输入：${_liveText.isEmpty ? '（空）' : _liveText}',
          child: Column(
            children: <Widget>[
              TextField(
                controller: _controller,
                onChanged: (String value) => setState(() => _liveText = value),
                decoration: InputDecoration(
                  labelText: '昵称',
                  hintText: '请输入昵称',
                  helperText: 'helperText 显示辅助说明',
                  prefixIcon: const Icon(Icons.person_outline),
                  suffixIcon: IconButton(
                    onPressed: () => _controller.clear(),
                    icon: const Icon(Icons.clear),
                  ),
                  border: const OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 12),
              Row(
                children: <Widget>[
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () =>
                          _controller.text = '由代码写入 ${DateTime.now().second}',
                      child: const Text('写入文本'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () {
                        _controller.clear();
                        setState(() => _liveText = '');
                      },
                      child: const Text('清空'),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'Form + TextFormField 校验',
          description:
              'GlobalKey<FormState> + validator；autovalidateMode 控制校验时机',
          result: _submitted.isEmpty ? '尚未提交' : '提交成功：$_submitted',
          child: Form(
            key: _formKey,
            autovalidateMode: AutovalidateMode.onUserInteraction,
            child: Column(
              children: <Widget>[
                TextFormField(
                  controller: _emailController,
                  keyboardType: TextInputType.emailAddress,
                  decoration: const InputDecoration(
                    labelText: '邮箱',
                    border: OutlineInputBorder(),
                  ),
                  validator: (String? value) {
                    final String text = value?.trim() ?? '';
                    if (text.isEmpty) {
                      return '邮箱不能为空';
                    }
                    if (!text.contains('@')) {
                      return '邮箱格式不正确';
                    }
                    return null;
                  },
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _passwordController,
                  obscureText: _obscure,
                  decoration: InputDecoration(
                    labelText: '密码',
                    border: const OutlineInputBorder(),
                    suffixIcon: IconButton(
                      onPressed: () => setState(() => _obscure = !_obscure),
                      icon: Icon(
                        _obscure ? Icons.visibility_off : Icons.visibility,
                      ),
                    ),
                  ),
                  validator: (String? value) =>
                      (value ?? '').length < 6 ? '密码至少 6 位' : null,
                ),
                const SizedBox(height: 12),
                Align(
                  alignment: Alignment.centerRight,
                  child: FilledButton(
                    onPressed: () {
                      if (_formKey.currentState?.validate() ?? false) {
                        setState(() {
                          _submitted = _emailController.text.trim();
                        });
                      }
                    },
                    child: const Text('提交'),
                  ),
                ),
              ],
            ),
          ),
        ),
        ExampleBlock(
          title: 'Checkbox / Switch',
          description: '直接用 Checkbox / Switch，或用 CheckboxListTile / SwitchListTile 套一层列表项',
          result: '订阅通知：$_notify，已同意协议：$_agree',
          child: Column(
            children: <Widget>[
              SwitchListTile(
                contentPadding: EdgeInsets.zero,
                title: const Text('接收推送通知'),
                value: _notify,
                onChanged: (bool value) => setState(() => _notify = value),
              ),
              CheckboxListTile(
                contentPadding: EdgeInsets.zero,
                title: const Text('我已阅读并同意用户协议'),
                value: _agree,
                onChanged: (bool? value) =>
                    setState(() => _agree = value ?? false),
              ),
              const SizedBox(height: 4),
              Row(
                children: <Widget>[
                  const Text('裸控件：'),
                  Checkbox(
                    value: _agree,
                    onChanged: (bool? value) =>
                        setState(() => _agree = value ?? false),
                  ),
                  Switch(
                    value: _notify,
                    onChanged: (bool value) => setState(() => _notify = value),
                  ),
                ],
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'SegmentedButton / DropdownButton',
          description: '单选按钮组与下拉选择；SegmentedButton 是 Material 3 的推荐做法',
          result: '等级：$_level，城市：$_city',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              SegmentedButton<int>(
                segments: const <ButtonSegment<int>>[
                  ButtonSegment<int>(value: 1, label: Text('低')),
                  ButtonSegment<int>(value: 2, label: Text('中')),
                  ButtonSegment<int>(value: 3, label: Text('高')),
                ],
                selected: <int>{_level},
                onSelectionChanged: (Set<int> selection) =>
                    setState(() => _level = selection.first),
              ),
              const SizedBox(height: 16),
              DropdownMenu<String>(
                initialSelection: _city,
                label: const Text('所在城市'),
                expandedInsets: EdgeInsets.zero,
                dropdownMenuEntries: const <DropdownMenuEntry<String>>[
                  DropdownMenuEntry<String>(value: '北京', label: '北京'),
                  DropdownMenuEntry<String>(value: '上海', label: '上海'),
                  DropdownMenuEntry<String>(value: '深圳', label: '深圳'),
                ],
                onSelected: (String? value) {
                  if (value != null) {
                    setState(() => _city = value);
                  }
                },
              ),
            ],
          ),
        ),
        ExampleBlock(
          title: 'Slider / RangeSlider',
          description: '连续值用 Slider，区间值用 RangeSlider；divisions 决定刻度数',
          result:
              '音量：${(_volume * 100).round()}%，价格区间：${_priceRange.start.round()} - ${_priceRange.end.round()}',
          child: Column(
            children: <Widget>[
              Row(
                children: <Widget>[
                  const Icon(Icons.volume_down),
                  Expanded(
                    child: Slider(
                      value: _volume,
                      divisions: 10,
                      label: '${(_volume * 100).round()}%',
                      onChanged: (double value) =>
                          setState(() => _volume = value),
                    ),
                  ),
                  const Icon(Icons.volume_up),
                ],
              ),
              RangeSlider(
                values: _priceRange,
                min: 0,
                max: 100,
                divisions: 20,
                labels: RangeLabels(
                  _priceRange.start.round().toString(),
                  _priceRange.end.round().toString(),
                ),
                onChanged: (RangeValues values) =>
                    setState(() => _priceRange = values),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

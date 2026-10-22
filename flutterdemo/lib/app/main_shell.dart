import 'package:flutter/material.dart';

import '../features/account/account_page.dart';
import '../features/home/home_page.dart';
import '../features/im/im_page.dart';

/// 应用外壳：底部三个 Tab（Home / IM / Account）。
///
/// 用 [IndexedStack] 承载三个页面，切换 Tab 时各页面的状态（滚动位置、
/// 输入框内容等）都会保留，等价于 Android 侧的 `saveState / restoreState`。
class MainShell extends StatefulWidget {
  const MainShell({super.key});

  @override
  State<MainShell> createState() => _MainShellState();
}

class _MainShellState extends State<MainShell> {
  int _index = 0;

  static const List<_TabSpec> _tabs = <_TabSpec>[
    _TabSpec('Home', Icons.home_outlined, Icons.home),
    _TabSpec('IM', Icons.mail_outlined, Icons.mail),
    _TabSpec('Account', Icons.person_outline, Icons.person),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(
        index: _index,
        children: const <Widget>[HomePage(), ImPage(), AccountPage()],
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (int index) => setState(() => _index = index),
        destinations: <Widget>[
          for (final _TabSpec tab in _tabs)
            NavigationDestination(
              icon: Icon(tab.icon),
              selectedIcon: Icon(tab.selectedIcon),
              label: tab.label,
            ),
        ],
      ),
    );
  }
}

/// 一个底部 Tab 的展示配置。
class _TabSpec {
  const _TabSpec(this.label, this.icon, this.selectedIcon);

  final String label;
  final IconData icon;
  final IconData selectedIcon;
}

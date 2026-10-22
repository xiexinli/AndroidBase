import 'package:flutter_test/flutter_test.dart';

import 'package:flutterdemo/app/flutter_demo_app.dart';

void main() {
  group('MainShell 底部导航', () {
    testWidgets('默认展示 Home 页，且三个 Tab 都在', (WidgetTester tester) async {
      await tester.pumpWidget(const FlutterDemoApp());

      // AppBar 标题来自 HomePage。
      expect(find.text('Flutter 示例'), findsOneWidget);

      // 底部三个 Tab。
      expect(find.text('Home'), findsOneWidget);
      expect(find.text('IM'), findsOneWidget);
      expect(find.text('Account'), findsOneWidget);
    });

    testWidgets('切到 Account 页展示设置项', (WidgetTester tester) async {
      await tester.pumpWidget(const FlutterDemoApp());

      await tester.tap(find.text('Account'));
      await tester.pumpAndSettle();

      expect(find.text('跟随系统'), findsOneWidget);
      expect(find.text('浅色'), findsOneWidget);
      expect(find.text('深色'), findsOneWidget);
    });
  });

  group('Home 分类目录', () {
    testWidgets('目录里能看到基础组件分类，点进去是二级示例页', (WidgetTester tester) async {
      await tester.pumpWidget(const FlutterDemoApp());

      expect(find.text('基础组件 Basics'), findsOneWidget);

      await tester.tap(find.text('基础组件 Basics'));
      await tester.pumpAndSettle();

      // 二级页标题栏用同一个名称，内容区出现示例块标题。
      expect(find.text('基础组件 Basics'), findsOneWidget);
      expect(find.text('Text 文本'), findsOneWidget);
    });
  });
}

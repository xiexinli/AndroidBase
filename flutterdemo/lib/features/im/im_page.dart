import 'package:flutter/material.dart';

/// IM 页：会话列表 → 聊天页的示例。
///
/// 用本地假数据演示列表、未读角标与聊天气泡，不依赖任何后端。
class ImPage extends StatelessWidget {
  const ImPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('IM')),
      body: ListView.separated(
        itemCount: _conversations.length,
        separatorBuilder: (BuildContext context, int index) =>
            const Divider(height: 1, indent: 72),
        itemBuilder: (BuildContext context, int index) {
          final _Conversation conversation = _conversations[index];
          return ListTile(
            contentPadding:
                const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            leading: CircleAvatar(
              backgroundColor: conversation.color,
              child: Text(
                conversation.name.substring(0, 1),
                style: const TextStyle(color: Colors.white),
              ),
            ),
            title: Text(conversation.name),
            subtitle: Text(
              conversation.lastMessage,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
            trailing: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.end,
              children: <Widget>[
                Text(
                  conversation.time,
                  style: Theme.of(context).textTheme.labelSmall,
                ),
                const SizedBox(height: 6),
                if (conversation.unread > 0)
                  Badge(label: Text('${conversation.unread}'))
                else
                  const SizedBox(height: 16),
              ],
            ),
            onTap: () {
              Navigator.of(context).push<void>(
                MaterialPageRoute<void>(
                  builder: (BuildContext context) =>
                      _ChatPage(conversation: conversation),
                ),
              );
            },
          );
        },
      ),
    );
  }
}

/// 聊天页：气泡列表 + 输入框，发送后自动回一条。
class _ChatPage extends StatefulWidget {
  const _ChatPage({required this.conversation});

  final _Conversation conversation;

  @override
  State<_ChatPage> createState() => _ChatPageState();
}

class _ChatPageState extends State<_ChatPage> {
  final TextEditingController _controller = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  final List<_Message> _messages = <_Message>[];

  @override
  void initState() {
    super.initState();
    _messages.add(_Message('你好，我是${widget.conversation.name}', false));
  }

  @override
  void dispose() {
    _controller.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  void _send() {
    final String text = _controller.text.trim();
    if (text.isEmpty) {
      return;
    }
    setState(() {
      _messages.add(_Message(text, true));
      _controller.clear();
    });
    _scrollToBottom();
    // 模拟对方回复。
    Future<void>.delayed(const Duration(milliseconds: 600), () {
      if (!mounted) {
        return;
      }
      setState(() => _messages.add(_Message('收到：$text', false)));
      _scrollToBottom();
    });
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!_scrollController.hasClients) {
        return;
      }
      _scrollController.animateTo(
        _scrollController.position.maxScrollExtent,
        duration: const Duration(milliseconds: 200),
        curve: Curves.easeOut,
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.conversation.name)),
      body: Column(
        children: <Widget>[
          Expanded(
            child: ListView.builder(
              controller: _scrollController,
              padding: const EdgeInsets.all(16),
              itemCount: _messages.length,
              itemBuilder: (BuildContext context, int index) =>
                  _Bubble(message: _messages[index]),
            ),
          ),
          SafeArea(
            top: false,
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 16),
              child: Row(
                children: <Widget>[
                  Expanded(
                    child: TextField(
                      controller: _controller,
                      textInputAction: TextInputAction.send,
                      onSubmitted: (_) => _send(),
                      decoration: const InputDecoration(
                        hintText: '说点什么…',
                        border: OutlineInputBorder(),
                        isDense: true,
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  IconButton.filled(
                    onPressed: _send,
                    icon: const Icon(Icons.send),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _Bubble extends StatelessWidget {
  const _Bubble({required this.message});

  final _Message message;

  @override
  Widget build(BuildContext context) {
    final ColorScheme scheme = Theme.of(context).colorScheme;
    return Align(
      alignment:
          message.fromMe ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.only(bottom: 12),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        constraints: const BoxConstraints(maxWidth: 260),
        decoration: BoxDecoration(
          color: message.fromMe
              ? scheme.primaryContainer
              : scheme.surfaceContainerHighest,
          borderRadius: BorderRadius.circular(14),
        ),
        child: Text(
          message.text,
          style: TextStyle(
            color: message.fromMe
                ? scheme.onPrimaryContainer
                : scheme.onSurfaceVariant,
          ),
        ),
      ),
    );
  }
}

@immutable
class _Conversation {
  const _Conversation({
    required this.name,
    required this.lastMessage,
    required this.time,
    required this.unread,
    required this.color,
  });

  final String name;
  final String lastMessage;
  final String time;
  final int unread;
  final Color color;
}

@immutable
class _Message {
  const _Message(this.text, this.fromMe);

  final String text;
  final bool fromMe;
}

const List<_Conversation> _conversations = <_Conversation>[
  _Conversation(
    name: '产品经理',
    lastMessage: '首页三个 Tab 的交互稿我发你了',
    time: '10:24',
    unread: 2,
    color: Color(0xFF3F6AE0),
  ),
  _Conversation(
    name: 'Flutter 交流群',
    lastMessage: '这个 IndexedStack 保留状态还挺好用的',
    time: '09:58',
    unread: 12,
    color: Color(0xFF00897B),
  ),
  _Conversation(
    name: '设计同学',
    lastMessage: '深色模式的对比度再调一版',
    time: '昨天',
    unread: 0,
    color: Color(0xFF8E24AA),
  ),
  _Conversation(
    name: '系统通知',
    lastMessage: '构建成功：flutterdemo 1.0.0+1',
    time: '周三',
    unread: 0,
    color: Color(0xFF607D8B),
  ),
];

import request from '@/utils/request';

// 获取文章信息
export const getAiChatApi = (message) => request.get(`/AiChat/ai/chat`, { params: { message } });

export const fetchStream = async (userMessage, deepThink, onChunk) => {
  const token = localStorage.getItem('token');
  const baseURL = '/api';
  const url = `${baseURL}/AiChat/ai/stream-chat?message=${encodeURIComponent(userMessage)}&deepThink=${deepThink}`;

  const response = await fetch(url, {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    throw new Error(`请求失败: ${response.status}`);
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = ''; // 用于存储未完成的数据

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    // 解码并追加到缓冲区
    buffer += decoder.decode(value, { stream: true });

    // 按行分割，保留最后一个可能不完整的行
    const lines = buffer.split('\n');
    buffer = lines.pop() || '';

    for (const line of lines) {
      const trimmed = line.trim();
      if (!trimmed) continue;

      // 只处理以 "data:" 开头的行
      if (trimmed.startsWith('data:')) {
        // 提取 data: 后面的内容（去除空格）
        let content = trimmed.substring(5).trim();
        if (content === '[DONE]') continue;

        // 🔥 关键：如果 content 本身还包含 "data:"，递归去除
        while (content.startsWith('data:')) {
          content = content.substring(5).trim();
        }

        // 尝试解析 JSON
        let finalText = content;
        if (content.startsWith('"') && content.endsWith('"')) {
          try {
            finalText = JSON.parse(content);
          } catch {
            finalText = content;
          }
        }

        // 确保 finalText 是字符串并且不再包含 "data:"
        if (typeof finalText === 'string') {
          while (finalText.startsWith('data:')) {
            finalText = finalText.substring(5).trim();
          }
        }

        // 传递最终的数据
        if (onChunk) {
          onChunk(finalText);
        }
      }
    }
  }

  // 处理缓冲区中剩余的数据
  if (buffer.trim()) {
    const trimmed = buffer.trim();
    if (trimmed.startsWith('data:')) {
      let content = trimmed.substring(5).trim();
      if (content !== '[DONE]') {
        while (content.startsWith('data:')) {
          content = content.substring(5).trim();
        }
        if (onChunk) {
          onChunk(content);
        }
      }
    }
  }
};

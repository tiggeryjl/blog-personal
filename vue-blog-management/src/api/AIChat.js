import request from '@/utils/request';


// 获取文章信息
export const getAiChatApi = (message) => request.get(`/AiChat/ai/chat`, { params: { message } });

// AI助手流式问答
// export const fetchStream = (userMessage, deepThink) => request.get(`/AiChat/ai/stream-chat?message=${encodeURIComponent(userMessage)}&deepThink=${deepThink}`);
// export const fetchStream = async (userMessage, deepThink, onChunk) => {
//   const token = localStorage.getItem('token')
//   const baseURL = '/api'
//   const url = `${baseURL}/AiChat/ai/stream-chat?message=${encodeURIComponent(userMessage)}&deepThink=${deepThink}`

//   const response = await fetch(url, {
//     headers: {
//       'Authorization': `Bearer ${token}`,
//       'Content-Type': 'application/json',
//     }
//   })

//   if (!response.ok) {
//     throw new Error(`请求失败: ${response.status}`)
//   }

//   const reader = response.body.getReader()
//   const decoder = new TextDecoder()

//   let fullText = ''
//   let chunkIndex = 0

//   while (true) {
//     const { done, value } = await reader.read()
//     chunkIndex++

//     if (done) {
//       break
//     }

//     const chunk = decoder.decode(value)

//     const lines = chunk.split('\n')

//     for (const line of lines) {
//       const trimmed = line.trim()
//       if (!trimmed) continue

//       // 尝试匹配 "data:" 开头的行，不关心后面有没有空格
//       if (trimmed.startsWith('data:')) {
//         // 提取 "data:" 之后的所有内容，并去掉首尾空格
//         const data = trimmed.substring(5).trim()

//         if (data === '[DONE]') {
//           continue
//         }

//         // 尝试解析 JSON，如果 data 是 JSON 字符串
//         let text = data
//         if (data.startsWith('"') && data.endsWith('"')) {
//           try {
//             text = JSON.parse(data)
//           } catch (e) {
//             text = data
//           }
//         }

//         fullText += text

//         if (onChunk) {
//           onChunk(text)
//         }
//       } else {
//       }
//     }
//   }

//   return fullText
// }

// api/AIChat.js
// export const fetchStream = async (userMessage, deepThink, onChunk) => {
//   const token = localStorage.getItem('token')
//   const baseURL = '/api'
//   const url = `${baseURL}/AiChat/ai/stream-chat?message=${encodeURIComponent(userMessage)}&deepThink=${deepThink}`

//   const response = await fetch(url, {
//     headers: {
//       'Authorization': `Bearer ${token}`,
//       'Content-Type': 'application/json',
//     }
//   })

//   if (!response.ok) {
//     throw new Error(`请求失败: ${response.status}`)
//   }

//   const reader = response.body.getReader()
//   const decoder = new TextDecoder()

//   while (true) {
//     const { done, value } = await reader.read()
//     if (done) break

//     const chunk = decoder.decode(value)
//     // 按行分割，保留空行以便正确处理
//     const lines = chunk.split('\n')

//     for (const line of lines) {
//       const trimmed = line.trim()
//       if (!trimmed) continue

//       // 🔥 严格匹配 "data:" 开头，并提取后面的内容
//       if (trimmed.startsWith('data:')) {
//         // 提取 data: 后面的所有内容
//         let data = trimmed.substring(5).trim()

//         // 如果提取到的内容仍然包含 data:，递归清洗（处理嵌套情况）
//         while (data.startsWith('data:')) {
//           data = data.substring(5).trim()
//         }

//         if (data === '[DONE]') continue

//         // 尝试解析 JSON
//         let text = data
//         if (data.startsWith('"') && data.endsWith('"')) {
//           try {
//             text = JSON.parse(data)
//           } catch {
//             text = data
//           }
//         }

//         if (onChunk) {
//           onChunk(text)
//         }
//       }
//     }
//   }
// }

// api/AIChat.js
// api/AIChat.js
// api/AIChat.js
export const fetchStream = async (userMessage, deepThink, onChunk) => {
  const token = localStorage.getItem('token')
  const baseURL = '/api'
  const url = `${baseURL}/AiChat/ai/stream-chat?message=${encodeURIComponent(userMessage)}&deepThink=${deepThink}`

  const response = await fetch(url, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    }
  })

  if (!response.ok) {
    throw new Error(`请求失败: ${response.status}`)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''  // 用于存储未完成的数据

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    // 解码并追加到缓冲区
    buffer += decoder.decode(value, { stream: true })

    // 按行分割，保留最后一个可能不完整的行
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''

    for (const line of lines) {
      const trimmed = line.trim()
      if (!trimmed) continue

      // 只处理以 "data:" 开头的行
      if (trimmed.startsWith('data:')) {
        // 提取 data: 后面的内容（去除空格）
        let content = trimmed.substring(5).trim()
        if (content === '[DONE]') continue

        // 🔥 关键：如果 content 本身还包含 "data:"，递归去除
        while (content.startsWith('data:')) {
          content = content.substring(5).trim()
        }

        // 尝试解析 JSON（如果数据是 JSON 字符串）
        let finalText = content
        if (content.startsWith('"') && content.endsWith('"')) {
          try {
            finalText = JSON.parse(content)
          } catch {
            finalText = content
          }
        }

        // 确保 finalText 是字符串并且不再包含 "data:"
        if (typeof finalText === 'string') {
          while (finalText.startsWith('data:')) {
            finalText = finalText.substring(5).trim()
          }
        }

        // 传递最终干净的数据
        if (onChunk) {
          onChunk(finalText)
        }
      }
      // 其他行（如空行、注释等）直接忽略
    }
  }

  // 处理缓冲区中剩余的数据
  if (buffer.trim()) {
    const trimmed = buffer.trim()
    if (trimmed.startsWith('data:')) {
      let content = trimmed.substring(5).trim()
      if (content !== '[DONE]') {
        while (content.startsWith('data:')) {
          content = content.substring(5).trim()
        }
        if (onChunk) {
          onChunk(content)
        }
      }
    }
  }
}
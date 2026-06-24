// SSE流式对话客户端
export function useSSE() {
  let abortController = null

  const streamChat = (message, sessionId, callbacks, model = 'flash', provider = 'deepseek') => {
    if (abortController) abortController.abort()
    abortController = new AbortController()

    const params = new URLSearchParams({ message, model, provider })
    if (sessionId) params.append('sessionId', sessionId)
    const url = `/api/chat/stream?${params}`

    fetch(url, { signal: abortController.signal })
      .then(response => {
        if (!response.ok) throw new Error('HTTP ' + response.status)
        const reader = response.body.getReader()
        const decoder = new TextDecoder()
        let buffer = ''

        const read = () => {
          reader.read().then(({ done, value }) => {
            if (done) {
              callbacks.onDone && callbacks.onDone()
              return
            }
            buffer += decoder.decode(value, { stream: true })
            // 按双换行分割SSE事件
            const parts = buffer.split('\n\n')
            buffer = parts.pop() || ''
            for (const part of parts) {
              if (!part.trim()) continue
              const lines = part.split('\n')
              let eventType = 'message'
              let data = ''
              for (const line of lines) {
                if (line.startsWith('event:')) {
                  eventType = line.slice(6).trim()
                } else if (line.startsWith('data:')) {
                  data = line.slice(5)  // 完全不trim
                }
              }
              if (data) handleEvent(eventType, data, callbacks)
            }
            read()
          }).catch(err => {
            if (err.name !== 'AbortError') callbacks.onError && callbacks.onError(err.message)
          })
        }
        read()
      })
      .catch(err => {
        if (err.name !== 'AbortError') callbacks.onError && callbacks.onError(err.message)
      })
  }

  const handleEvent = (type, data, callbacks) => {
    switch (type) {
      case 'status':
        callbacks.onStatus && callbacks.onStatus(data)
        break
      case 'chunk':
        callbacks.onChunk && callbacks.onChunk(data)
        break
      case 'cards':
        try {
          callbacks.onCards && callbacks.onCards(JSON.parse(data))
        } catch (e) {
          callbacks.onCards && callbacks.onCards([])
        }
        break
      case 'done':
        callbacks.onDone && callbacks.onDone(data)
        break
      case 'error':
        callbacks.onError && callbacks.onError(data)
        break
    }
  }

  const abort = () => {
    if (abortController) { abortController.abort(); abortController = null }
  }

  return { streamChat, abort }
}

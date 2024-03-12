//const host = process.env.NODE_ENV === 'production' ? window.location.host : 'localhost:8080'

import { connect } from "socket.io-client"

export let send
//const topicResponseUrl = process.env.TOPIC_RESPONSE_URL

export const startWebsocketConnection = () => {
  const ws = new window.WebSocket('ws://localhost:8080/framedesc') || {}

  ws.onopen = () => {
    console.log('opened ws connection')
  }

  ws.onclose = (e) => {
    console.log('close ws connection: ', e.code, e.reason)
    setTimeout(() => startWebsocketConnection(), 10000)
  }

  ws.onmessage = (e) => {
    onMessageCallback && onMessageCallback(e.data)
  }

  send = ws.send.bind(ws)
}

let onMessageCallback
export const registerOnMessageCallback = (fn) => {
  onMessageCallback = fn
}



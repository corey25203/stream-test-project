import React from 'react'
import './css/App.css'

import FramesInfoArea from './FramesInfoArea'
import ParsingFileBar from './ParsingFileBar'
import LoadFileBar from './LoadFileBar'


import { registerOnMessageCallback, send } from './websocket'

export class App extends React.Component {

  state = {
    messages: [],
    sessionName: null
  }

  constructor(props) {
    super(props)
    registerOnMessageCallback(this.onMessageReceived.bind(this))
  }

  onMessageReceived(msg) {

    msg = 'data:image/jpeg;base64,' + msg;
    this.setState({
      messages: this.state.messages.concat(msg)
    })
  }

  setSessionName(sessionName) {
    this.setState({
      sessionName: sessionName
    })
  }


  render() {

    return (
      <div className='container'>
        <div className='container-title'>Frames data parsing results</div>

        <LoadFileBar sessionName={this.state.sessionName} />
        <ParsingFileBar sessionName={this.state.sessionName} />
        <FramesInfoArea messages={this.state.messages} sessionName={this.state.sessionName} />

      </div>
    )
  }
}

export default App

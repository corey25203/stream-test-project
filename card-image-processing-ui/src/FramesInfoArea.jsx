import React from 'react'
import './css/FramesInfoArea.css'

const Message = ({ baseencodedimg, sessionName, self }) => (
  <div className={'message' + (self ? ' message-self' : '')}>
    <img src={baseencodedimg}></img>
  </div>
)

export default class FramesInfoArea extends React.Component {
  constructor(props) {
    super(props)
    this.messageWindow = React.createRef()
  }
  componentDidUpdate() {
    const messageWindow = this.messageWindow.current
    messageWindow.scrollTop = messageWindow.scrollHeight - messageWindow.clientHeight
  }

  render() {
    const { messages = [], sessionName } = this.props
    return (
      <div className='message-window' ref={this.messageWindow}>
        {messages.splice(0, 5).map((msg, i) => {
          return <Message key={i} baseencodedimg={msg} />
        })}
        <div>&nbsp;</div>
      </div>
    )
  }
}

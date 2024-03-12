import React, { Component } from 'react'
import './css/LoadFileBar.css'

const fileuploadURL = 'http://localhost:8080/uploadFile';

const errorHanlder = (e) => console.log(e);

export default class LoadFileBar extends Component {

  constructor(props) {
    super(props);
    this.state = {

      testDataResponse: null,

    };
    this.input = React.createRef();
    this.selectedFile = null;
  }

  onFileChange(event) {

    this.setState({
      selectedFile: event.target.files[0],
    });
    console.log('onFileChange->' + JSON.stringify(this.state))
  };

  onFileUpload(event) {

    if (!this.state.selectedFile) {
      alert('Select file');
      return;
    }

    console.log(event.text);
    var formData = new FormData();
    formData.append("file", this.state.selectedFile, this.state.selectedFile.name);
    fetch(fileuploadURL, {
      method: 'POST',
      body: formData,
      headers: {
        'Access-Control-Allow-Headers': '*'
      }


    }).then(response => response.json())
      .then(success => console.log(success))
      .catch(error => {
        console.log(error);
        alert('ERR:' + error);
      });
  };

  render() {

    const onFileUpload = this.onFileUpload.bind(this)
    const onFileChange = this.onFileChange.bind(this)

    return (
      <div className='loadfilebar'>
        <div className='actionlabel'>LoadFile Bar</div>
        <div className='message-window' ref={this.messageWindow}>
          <input className='file-input' type="file" ref={this.input} onChange={onFileChange} required />
          <button className='file-send' onClick={event => this.onFileUpload(event.target.value)}>
            Upload
          </button>
        </div>
      </div>
    )
  }
}

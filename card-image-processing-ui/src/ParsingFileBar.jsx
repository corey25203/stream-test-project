import React, { Component } from 'react'
import './css/ParsingFileBar.css'

const filesListLoadURL = 'http://localhost:8080/files';
//const filesListLoadURL = process.env.FILES_LIST_URL

const fileParseURL = 'http://localhost:8080/parseFile';
//const fileParseURL = process.env.FILE_PARSE_URL

class FileInfoData {

  name = null;
  type = null;
  size = null;
  encodedData = null;
  storageURL = null;

}

export default class ParsingFileBar extends Component {
  constructor(props) {
    super(props);
    this.state = {
      testDataResponse: [],
      selectedForParsingFile: null,
    };

    this.input = React.createRef();
  }

  componentDidMount() {
    console.log('componentDidMount call');
    this.initFilesList();
  }

  initFilesList() {

    console.log('initFilesList');

    fetch(filesListLoadURL, {
      method: 'GET',
      //body: formData,
      headers: {
        'Access-Control-Allow-Headers': '*'
      }

    }).then(response => response.json())
      .then(success => {
        console.log(success);
        this.setFIlesLIst(success)
      })
      .catch(error => {
        console.log(error);
        alert('ERR:' + error);
      });

  }

  setFIlesLIst(filesListLoadResponse) {

    this.setState({
      testDataResponse: filesListLoadResponse

    });
    console.log("filesListLoadResponse state->" + JSON.stringify(this.state.testDataResponse));
  }

  setSelectedForParsingFile(selectedForParsingFile) {
    this.setState({
      selectedForParsingFile: selectedForParsingFile

    });
    console.log("selectedForParsingFile->" + JSON.stringify(selectedForParsingFile));
  }

  onFileParse() {

    if (!this.state.selectedForParsingFile) {
      alert('Select file');
      return;
    }


    console.log('onFileParse0->' + this.state.selectedForParsingFile);

    var fileInstanceParseURL = fileParseURL + '/' + this.state.selectedForParsingFile;


    var fileinfo = new FileInfoData();
    fileinfo.name = this.state.selectedForParsingFile;


    console.log("onFileParse ->" + fileInstanceParseURL);
    fetch(fileInstanceParseURL, {
      method: 'GET',
      //  body: null,
      headers: {
        'Access-Control-Allow-Headers': '*',
        'Access-Control-Allow-Origin': '*'

      }


    }).then(response => response.json())
      .then(success => {
        console.log(success);
      })
      .catch(error => {
        console.log(error);
        alert('ERR:' + error);
      });


  }

  render() {

    return (
      <div className='parsingfilebar'>
        <div className='actionlabel'>Parsing Bar</div>
        <div className='message-window' ref={this.messageWindow}>
          <select value={this.selectedForParsingFile}
            onChange={event => this.setSelectedForParsingFile(event.target.value)}>

            <option value="">select a file</option>
            {this.state.testDataResponse.map((fileItem) => {

              return (<option key={fileItem.fileName} value={fileItem.fileName} >{fileItem.fileName}</option>);
            })}
          </select>
          <button onClick={event => this.onFileParse(event.target.value)}>
            Parse file
          </button>
        </div>
      </div>
    )

  }
}

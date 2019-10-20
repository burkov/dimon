import React, { Component } from 'react';
import './DispatcherMonitor.css';


type DispatcherMonitorState = {
  dataFromServer: Object
}

type DispatcherMonitorProps = {

}

export default class DispatcherMonitor extends Component<{}, DispatcherMonitorState> {
  constructor(props: DispatcherMonitorProps) {
    super(props);
    this.state = { dataFromServer: {} }
  }

  componentDidMount() {
    const ws = new WebSocket('ws://demos.kaazing.com/echo');
    ws.addEventListener('message', event => {
      this.setState({ dataFromServer: JSON.parse(event.data) });
    });
    ws.addEventListener('open', event => {
      ws.send('{"str": "hello world" }');
    })
  }

  render() {
    return <div className="App" >
      {JSON.stringify(this.state.dataFromServer)}
    </div>;
  }
}
import React, { Component } from 'react';
import './DispatcherMonitor.css';

import RSocketWebSocketClient from 'rsocket-websocket-client';
import {
  RSocketClient,
  JsonSerializers,
  Utf8Encoders
} from 'rsocket-core';


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

  async componentDidMount() {
    const client = new RSocketClient({
      serializers: JsonSerializers,
      setup: {
        keepAlive: 60000,
        lifetime: 180000,
        dataMimeType: 'application/json',
        metadataMimeType: 'application/json',
      },
      transport: new RSocketWebSocketClient({
        url: 'ws://localhost:8080/rsocket',
        debug: true,
      }, Utf8Encoders),
    });
    const socket = await client.connect();

    console.log(`done await`);
  }

  render() {
    return <div className="App" >
      {JSON.stringify(this.state.dataFromServer)}
    </div>;
  }
}
import React, { Component } from 'react';
import './DispatcherMonitor.css';

import RSocketWebSocketClient from 'rsocket-websocket-client';
import {
  RSocketClient,
  JsonSerializer,
  IdentitySerializer
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
      serializers: {
        data: JsonSerializer,
        metadata: IdentitySerializer
      },
      setup: {
        keepAlive: 60000,
        lifetime: 180000,
        dataMimeType: 'application/json',
        metadataMimeType: 'message/x.rsocket.routing.v0',
      },
      transport: new RSocketWebSocketClient({
        url: 'ws://localhost:8080/rsocket',
        debug: true,
      }),
    });
    client.connect().subscribe({
      onComplete: socket => {
        socket.requestStream({
          metadata: `${String.fromCharCode('jobs-stream'.length)}jobs-stream`
        }).subscribe({
          onComplete: () => console.log('complete'),
          onError: error => {
            console.log(`error: ${error}`)
          },
          onNext: payload => {
            const data = payload.data;
            const type = data.type;
            switch (type) {
              case 'insert':
                console.log(`INS: ${JSON.stringify(data.job)}`);
                break;
              case 'delete':
                console.log(`DEL: ${data.jobId}`);
                break;
              case 'update':
                console.log(`UPD: ${JSON.stringify(data.job)}`);
                break;
              default:
                console.error(`Unkown message: ${JSON.stringify(data)}`);
                break;
            }
          },
          onSubscribe: subscription => {
            subscription.request(50);
          }
        });
      },
      onError: error => {
        console.log('error')
      },
      onSubscribe: cancel => {
        console.log(JSON.stringify(cancel))
      }
    })

  }

  render() {
    return <div className="App" >
      {JSON.stringify(this.state.dataFromServer)}
    </div>;
  }
}